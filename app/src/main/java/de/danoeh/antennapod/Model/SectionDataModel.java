package de.danoeh.antennapod.Model;

import java.util.List;

import de.danoeh.antennapod.core.feed.FeedItem;

public class SectionDataModel {

    private String title;
    private List<FeedItem> feedItem;

    public SectionDataModel() {

    }
    public SectionDataModel(String title, List<FeedItem> feedItem) {
        this.title = title;
        this.feedItem = feedItem;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<FeedItem> getFeedItem() {
        return feedItem;
    }

    public void setFeedItem(List<FeedItem> feedItem) {
        this.feedItem = feedItem;
    }
}
