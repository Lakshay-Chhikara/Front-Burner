package com.apprevelations.frontburner;

/**
 * A representation of an rss item from the list.
 *
 * @author Veaceslav Grec
 */
public class XmlItem {

    private final String title;
    private final String link;
    private final String hash;
    private final String date;

    public XmlItem(String title, String link, String hash, String date) {
        this.title = title;
        this.link = link;
        this.hash = hash;
        this.date = date;

    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getHash() { return hash; }

    public String getDate() {
        return date;
    }

}
