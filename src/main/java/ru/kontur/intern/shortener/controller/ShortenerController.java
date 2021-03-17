package ru.kontur.intern.shortener.controller;

import static ru.kontur.intern.shortener.logic.Pref.L_PREFIX;
import static ru.kontur.intern.shortener.logic.Pref.STATS_PREFIX;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kontur.intern.shortener.logic.Links;
import ru.kontur.intern.shortener.models.Link;
import ru.kontur.intern.shortener.models.OriginalLink;
import ru.kontur.intern.shortener.models.ShortLink;

@RestController
public class ShortenerController {

    private final Links links;

    @Autowired
    public ShortenerController(final Links links) {
        this.links = links;
    }

    @PostMapping(value = "/generate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generate(@RequestBody final OriginalLink originalLink) {
        final String original = originalLink.getOriginal();
        if (original == null || original.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        try {
            new URI(original);
        } catch (NullPointerException | URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        ShortLink shortLink = links.findOrAdd(original);
        return ResponseEntity.ok(shortLink);
    }

    @GetMapping(value = L_PREFIX + "{shortLinkName}")
    public ResponseEntity<?> redirect(@PathVariable final String shortLinkName) {
        final URI uri;
        try {
            final Link link = links.getLinkByShortLink(L_PREFIX + shortLinkName);
            links.saveClick(link.getLink());
            uri = new URI(link.getOriginal());
        } catch (NullPointerException | URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);
        return new ResponseEntity(headers, HttpStatus.FOUND);
    }

    @GetMapping(value = STATS_PREFIX + "/{shortLinkName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLinkStatistics(@PathVariable final String shortLinkName) {
        final Link link = links.sortAndReturnThisLink(L_PREFIX + shortLinkName);
        if (link == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(link);
    }

    @GetMapping(value = STATS_PREFIX,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRating(@RequestParam final int page, @RequestParam final int count) {
        final int maxCount = 100;
        if (page <= 0 || count <= 0 || count > maxCount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok(links.sortAndGetRatingList(page, count));
    }
}