package de.test.antennapod.homepage;

import android.test.ActivityInstrumentationTestCase2;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.Model.SectionDataModel;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.fragment.DiscoveryFragment;
import de.danoeh.antennapod.fragment.HomeFragment;

/**
 * Created by franc on 2018-04-17.
 */

    public class HomeTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private HomeFragment homeFragment;
    private PodDBAdapter adapter;
    // Constructor
    public HomeTest() {
        super("de.danoeh.antennapod.activity", MainActivity.class);
    }

    /**
     * Starts the Main activity
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        homeFragment = new HomeFragment();
        getActivity().getSupportFragmentManager().beginTransaction().add(homeFragment, DiscoveryFragment.class.getSimpleName()).commit();
        getInstrumentation().waitForIdleSync();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        PodDBAdapter.deleteDatabase();
    }

    /**
     * Adds a feed item to favorites
     * Asserts that there is 2 items in the data list
     * One for Newest and one for favorites
     */
    public void testLoadFavorites() {
        Feed feed = new Feed();
        feed.setTitle("title");

        FeedItem feedItem = new FeedItem();
        feedItem.setTitle("title");
        feedItem.setFeed(feed);
        feedItem.setPubDate(new java.util.Date());

        List<FeedItem> feedItems = new ArrayList<>();
        feedItems.add(feedItem);

        adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.setFeed(feed);
        adapter.setFeedItemlist(feedItems);
        adapter.setFavorites(feedItems);

        List<SectionDataModel> data = homeFragment.loadData();

        // Should have 2 items in the data list
        // For Newest and for Favorites
        assertEquals(2, data.size());
        for(int i = 0; i < data.size(); i++){
            assertTrue(data.get(i).getTitle().contains("Favorites") || data.get(i).getTitle().contains("Newest"));
        }
    }

    /**
     * Creates a folder
     * Asserts that there is 1 item in the data list with the title Folders
     */
    public void testLoadFolder() {

        Folder folder = new Folder("Folder name", null);

        adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.addFolder(folder);

        List<SectionDataModel> data = homeFragment.loadData();

        //Asserts that there is 1 item in the data list with the title Folders
        assertEquals(1, data.size());
        assertTrue(data.get(0).getTitle().contains("Folders"));
    }
}
