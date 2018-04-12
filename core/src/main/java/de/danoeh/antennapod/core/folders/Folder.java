package de.danoeh.antennapod.core.folders;

import android.database.Cursor;

import java.util.List;

import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedPreferences;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.core.util.flattr.FlattrStatus;

/**
 * Created by William on 2018-03-15.
 */

public class Folder {

    //id
    private long id;

    //name of the folder
    public String name;

    //list of episodes inside folder
    public List<FeedItem> episodes;

    //When folder is retrieved from database
    public Folder(long id, String name, List<FeedItem> episodes) {
        this.id = id;
        this.name = name;
        this.episodes = episodes;
    }

    public Folder(String name, List<FeedItem> episodes){
        this.name = name;
        this.episodes = episodes;
    }

    public static Folder fromCursor(Cursor cursor) {
        int indexId = cursor.getColumnIndex(PodDBAdapter.KEY_ID);
        int indexFolderName = cursor.getColumnIndex(PodDBAdapter.KEY_FOLDER_NAME);

        Folder folder = new Folder(
                cursor.getLong(indexId),
                cursor.getString(indexFolderName),null
        );

        return folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<FeedItem> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<FeedItem> episodes) {
        this.episodes = episodes;
    }

    public int getEpisodesNum(){
        return this.episodes.size();
    }

    public FeedItem getItemAtIndex(int position) {
        return episodes.get(position);
    }
}
