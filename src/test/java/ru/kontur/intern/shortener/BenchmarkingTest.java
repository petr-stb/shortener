package ru.kontur.intern.shortener;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import ru.kontur.intern.shortener.data.IgniteWorker;
import ru.kontur.intern.shortener.logic.Links;
import ru.kontur.intern.shortener.models.Link;

public class BenchmarkingTest {

    private static final int COUNT = 100;
    private static final int BIG_COUNT = 15000;
    private static String shortLink1;
    private static String shortLink2;
    private static String shortLink3;
    private static final String original = "https://yandexxxxxxx.ru?p=0";
    private static final Links links = new Links(new IgniteWorker());

    static {
        try {
            links.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        links.clear();
        for (int i = 0; i < BIG_COUNT; i++) {
            final String originalFull = original + i;
            links.findOrAdd(originalFull);
            if (i == 0) {
                shortLink1 = links.getShortLinkByOriginal(originalFull).getLink();
            }
            if (i == BIG_COUNT / 2) {
                shortLink2 = links.getShortLinkByOriginal(originalFull).getLink();
            }
            if (i == BIG_COUNT - 1) {
                shortLink3 = links.getShortLinkByOriginal(originalFull).getLink();
            }
        }
        links.sort();
    }

   @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 5, time = 5)
    public void getLinkAndIncrementCountTest() {
        for (int i = 0; i < COUNT; i++) {
            Link currentLink = links.getLinkByShortLink(shortLink1);
            links.saveClick(currentLink.getLink());
            currentLink = links.getLinkByShortLink(shortLink2);
            links.saveClick(currentLink.getLink());
            currentLink = links.getLinkByShortLink(shortLink3);
            links.saveClick(currentLink.getLink());
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 5, time = 5)
    public void addTest() {
        for (int i = 0; i < COUNT; i++) {
            final String newOriginal = original + "?r=" + i;
            Link link = new Link(newOriginal);
            links.add(link);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 5, time = 5)
    public void sortTest() {
        for (int i = 0; i < COUNT; i++) {
            links.sort();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 5, time = 5)
    public void getSortedListTest() {
        for (int i = 0; i < COUNT; i++) {
            links.sortAndGetRatingList(1, BIG_COUNT);
        }
    }
}
