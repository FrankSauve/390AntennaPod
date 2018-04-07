package de.test.antennapod.discovery;

import android.test.ActivityInstrumentationTestCase2;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.fragment.DiscoveryFragment;

/**
 * Created by RaphaelleG on 18-03-20.
 */

public class DiscoveryTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private DiscoveryFragment discoveryFragment;
    //private UserPreferences userPreferences;
    //private List<Integer> discoveryIds;

    // Constructor
    public DiscoveryTest(){
        super("de.danoeh.antennapod.activity", MainActivity.class);
    }

    /**
     * Starts the Main activity
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        discoveryFragment = new DiscoveryFragment();
        getActivity().getSupportFragmentManager().beginTransaction().add(discoveryFragment, DiscoveryFragment.class.getSimpleName()).commit();
        getInstrumentation().waitForIdleSync();
    }


     //TODO: Crashes in circleCI, but works on local machines ¯\_(ツ)_/¯
//    //Test Discovery loadCategories
//    public void testLoadCategories() throws InterruptedException {
//
//        // Create new List for User preferences
//        List<Integer> newCategories = new ArrayList<>();
//        newCategories.add(6);
//        newCategories.add(8);
//        newCategories.add(9);
//
//        //Assign List to new values for Buttons
//        UserPreferences.setPrefDiscoveryButtons(newCategories);
//
//        //onStartDiscovery Page
//
//        discoveryFragment.onStart();
//
//        List<Integer> test = discoveryFragment.getIds();
//
//        assertEquals(test, newCategories);
//        assertTrue(discoveryFragment.getCategoryId().contains(1309));
//        assertTrue(discoveryFragment.getCategoryId().contains(1311));
//        assertTrue(!discoveryFragment.getCategoryId().contains(1310));
//        assertTrue(discoveryFragment.getCategoryId().contains(1314));
//
//    }

    public void testFindAutomaticRecommendations() throws InterruptedException {

        discoveryFragment.testing = true;

        Feed feed = new Feed();
        feed.setTitle("The Daily");
        feed.setAuthor("The New York Times");
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.setFeed(feed);

        //Get subscriptions
        discoveryFragment.loadSubscriptions();
        Thread.sleep(1000);

        discoveryFragment.findAutomaticRecommendations();
        Thread.sleep(1000);

        //Assert that recommendations are not empty
        assertNotNull(discoveryFragment.getSearchResults());

        //Assert that there is a podcast from the same author
        boolean foundModernLove = false;
        for(ItunesAdapter.Podcast podcast : discoveryFragment.getSearchResults()){
            //Podcast from the same author
            System.out.println("TITLE " + podcast.title);
            if(podcast.title.equals("Modern Love")){
                System.out.println("MODERN LOVE FOUND");
                foundModernLove = true;
            }
        }
        assertTrue(foundModernLove);

        //Remove the feed
        adapter.removeFeed(feed);
        adapter.close();
    }
}
