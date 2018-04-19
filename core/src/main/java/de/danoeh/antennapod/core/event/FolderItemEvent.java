package de.danoeh.antennapod.core.event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.danoeh.antennapod.core.feed.FeedItem;

public class FolderItemEvent {

    public enum Action {
        REMOVED
    }

    public final Action action;
    public final FeedItem item;

    private FolderItemEvent(Action action, FeedItem item) {
        this.action = action;
        this.item = item;
    }


    public static FolderItemEvent removed(FeedItem item) {
        return new FolderItemEvent(Action.REMOVED, item);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("action", action)
                .append("item", item)
                .toString();
    }

}
