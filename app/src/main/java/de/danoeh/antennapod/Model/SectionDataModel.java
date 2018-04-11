package de.danoeh.antennapod.Model;

import java.util.List;

import de.danoeh.antennapod.core.feed.FeedItem;

public class SectionDataModel {

    private String headerTitle;
    private List<FeedItem> allItemsInSection;

    public SectionDataModel() {

    }

    public SectionDataModel(String headerTitle, List<FeedItem> allItemsInSection) {
        this.headerTitle = headerTitle;
        this.allItemsInSection = allItemsInSection;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public List<FeedItem> getAllItemsInSection() {
        return allItemsInSection;
    }

    public void setAllItemsInSection(List<FeedItem> allItemsInSection) {
        this.allItemsInSection = allItemsInSection;
    }
}
