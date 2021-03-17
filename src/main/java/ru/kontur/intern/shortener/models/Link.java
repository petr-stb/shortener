package ru.kontur.intern.shortener.models;

import java.util.Objects;

import ru.kontur.intern.shortener.logic.Pref;
import ru.kontur.intern.shortener.logic.ShortCode;

public class Link {

    private String link;
    private String original;
    private int rank;
    private int count;

    public Link(final String original) {
        this.original = original;
        this.link = Pref.L_PREFIX + (new ShortCode()).toString();
        this.count = 0;
    }

    public Link() {
    }

    public String getLink() {
        return link;
    }

    public String getOriginal() {
        return original;
    }

    public int getRank() {
        return rank;
    }

    public int getCount() {
        return count;
    }

    public void setRank(final int rank) {
        this.rank = rank;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Link link1 = (Link) o;
        return rank == link1.rank
                && count == link1.count
                && Objects.equals(link, link1.link)
                && Objects.equals(original, link1.original);
    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }
}
