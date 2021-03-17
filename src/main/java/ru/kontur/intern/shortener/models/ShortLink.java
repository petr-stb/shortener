package ru.kontur.intern.shortener.models;

public class ShortLink {

    private String link;

    public ShortLink(final String link) {
        this.link = link;
    }

    public ShortLink() {
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }
}
