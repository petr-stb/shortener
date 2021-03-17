package ru.kontur.intern.shortener.logic;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kontur.intern.shortener.data.IgniteWorker;
import ru.kontur.intern.shortener.models.Link;
import ru.kontur.intern.shortener.models.ShortLink;

@Component
public class Links {

    private final IgniteWorker igniteWorker;
    private final Map<String, String> originalsMap = new HashMap<>();
    private final Map<String, Integer> clicksMap = new HashMap<>();
    private List<Link> sortedLinksList;
    private boolean isSorted;

    @Autowired
    public Links(final IgniteWorker igniteWorker) {
        this.igniteWorker = igniteWorker;
    }

    public ShortLink getShortLinkByOriginal(final String original) {
        final String link = originalsMap.get(original);
        if (link != null) {
            return new ShortLink(link);
        }
        return null;
    }

    public ShortLink findOrAdd(final String original) {
        final ShortLink shortLink = getShortLinkByOriginal(original);
        if (shortLink != null) {
            return shortLink;
        }
        Link link = null;
        boolean shortLinkIsUnique = false;
        while (!shortLinkIsUnique) {
            link = new Link(original);
            shortLinkIsUnique = !igniteWorker.containsRecord(link.getLink());
        }
        add(link);
        return new ShortLink(link.getLink());
    }

    public synchronized void add(final Link link) {
        isSorted = false;
        igniteWorker.putRecord(link.getLink(), link);
        originalsMap.put(link.getOriginal(), link.getLink());
    }

    public Link getLinkByShortLink(final String shortLink) {
        return igniteWorker.getRecord(shortLink);
    }

    public synchronized void saveClick(final String shortLink) {
        isSorted = false;
        Integer count = clicksMap.get(shortLink);
        if (count == null) {
            count = 0;
        }
        clicksMap.put(shortLink, count + 1);
    }

    public Link sortAndReturnThisLink(final String shortLink) {
        if (!isSorted) {
            sort();
        }
        for (final Link link : sortedLinksList) {
            if (link.getLink().equals(shortLink)) {
                return link;
            }
        }
        return null;
    }

    public List<Link> sortAndGetRatingList(final int page, final int count) {
        if (!isSorted) {
            sort();
        }
        final List<Link> ratingList = new ArrayList<>();
        for (int i = (page - 1) * count; i < page * count && i < sortedLinksList.size(); i++) {
            final Link link = sortedLinksList.get(i);
            ratingList.add(link);
        }
        return ratingList;
    }

    public synchronized void sort() {
        uploadClicksToDb();
        sortedLinksList = igniteWorker.getAllRecords().values().stream()
                .sorted(new LinkComparator())
                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < sortedLinksList.size(); i++) {
            sortedLinksList.get(i).setRank(i + 1);
        }
        isSorted = true;
    }

    private void uploadClicksToDb() {
        for(Map.Entry<String, Integer> entry : clicksMap.entrySet()) {
            final Link currentLink = igniteWorker.getRecord(entry.getKey());
            if (currentLink != null) {
                int countFromDb = currentLink.getCount();
                currentLink.setCount(countFromDb + entry.getValue());
                igniteWorker.putRecord(currentLink.getLink(), currentLink);
            }
        }
        clicksMap.clear();
    }

    private void fillOriginalMap() {
        final Map<String, Link> map = igniteWorker.getAllRecords();
        for (Map.Entry<String, Link> entry : map.entrySet()) {
            originalsMap.put(entry.getValue().getOriginal(), entry.getKey());
        }
    }

    @PostConstruct
    public synchronized void start() throws UnknownHostException {
        if (!igniteWorker.isStarted()) {
            igniteWorker.startNode();
            igniteWorker.setStarted(true);
        }
        fillOriginalMap();
        sort();
    }

    @PreDestroy
    private synchronized void stop() {
        uploadClicksToDb();
        igniteWorker.stopNode();
        igniteWorker.setStarted(false);
    }

    public synchronized void clear() {
        igniteWorker.clear();
        originalsMap.clear();
        clicksMap.clear();
        sortedLinksList.clear();
    }
}