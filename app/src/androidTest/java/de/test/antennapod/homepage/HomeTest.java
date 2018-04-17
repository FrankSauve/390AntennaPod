package de.test.antennapod.homepage;

import android.test.ActivityInstrumentationTestCase2;

import java.util.List;

import de.danoeh.antennapod.Model.SectionDataModel;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
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

    public void testLoadFavorites() throws InterruptedException {
        FeedItem feedItem = new FeedItem();
        feedItem.setTitle("title");

        adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.addFavoriteItem(feedItem);
        adapter.close();

        List<SectionDataModel> data = homeFragment.loadData();

        assertEquals(1, data.size());
    }
}
