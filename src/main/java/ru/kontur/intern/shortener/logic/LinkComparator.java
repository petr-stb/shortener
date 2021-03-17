package ru.kontur.intern.shortener.logic;

import java.util.Comparator;
import ru.kontur.intern.shortener.models.Link;

public class LinkComparator implements Comparator<Link> {

    @Override
    public int compare(Link o1, Link o2) {
        int value = Integer.compare(o2.getCount(), o1.getCount());
        if (value == 0) {
            value = o2.getLink().compareTo(o1.getLink());
        }
        return value;
    }
}
