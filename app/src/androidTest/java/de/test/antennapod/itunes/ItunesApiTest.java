package de.test.antennapod.itunes;


import android.test.ActivityInstrumentationTestCase2;

import java.util.LinkedList;
import java.util.List;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.fragment.ItunesSearchFragment;
import de.danoeh.antennapod.fragment.TrendingFragment;

/**
 * Created by franc on 2018-02-26.
 */

public class ItunesApiTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private TrendingFragment trendingFragment;
    private ItunesSearchFragment itunesSearchFragment;
    private List<ItunesAdapter.Podcast> topList;
    private List<ItunesAdapter.Podcast> searchResults;

    // Constructor
    public ItunesApiTest(){
        super("de.danoeh.antennapod.activity", MainActivity.class);
    }

    /**
     * Starts the Main activity
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        trendingFragment = new TrendingFragment();
        itunesSearchFragment = new ItunesSearchFragment();
        getActivity().getSupportFragmentManager().beginTransaction().add(trendingFragment, TrendingFragment.class.getSimpleName()).commit();
        getActivity().getSupportFragmentManager().beginTransaction().add(itunesSearchFragment, ItunesSearchFragment.class.getSimpleName()).commit();
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests if the loadTopList function returns a list of 25 podcasts
     * @throws InterruptedException
     */
    public void testLoadTopList() throws InterruptedException {
        //Request to itunes API
        trendingFragment.loadToplist();

        //Wait for request
        Thread.sleep(5000);

        topList = trendingFragment.getTopList();

        for(int i = 0; i < topList.size(); i++){
            System.out.println(topList.get(i).getPodcastInfo());
        }

        //Assertions
        assertNotNull(topList);
        assertEquals( 25, topList.size());
    }

    public void testItunesSearch() throws InterruptedException {
        //Request to itunes API
        itunesSearchFragment.search("a");

        //Wait for request
        Thread.sleep(5000);

        searchResults = itunesSearchFragment.getSearchResults();

        for(int i = 0; i < searchResults.size(); i++){
            System.out.println(searchResults.get(i).getPodcastInfo());
        }

        //Assertions
        assertNotNull(searchResults);
    }

}
