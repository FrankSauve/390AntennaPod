package de.danoeh.antennapod.Model;

import java.util.List;

import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.folders.Folder;

public class SectionDataModel {

    private String title;
    private List<FeedItem> feedItem;
    private List<Folder> folders;

    public SectionDataModel() {

    }
    public SectionDataModel(String title, List<FeedItem> feedItem, List<Folder> folders) {
        this.title = title;
        this.feedItem = feedItem;
        this.folders = folders;
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


    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }
}
