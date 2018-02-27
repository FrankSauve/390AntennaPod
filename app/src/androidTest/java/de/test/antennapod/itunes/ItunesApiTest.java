package de.test.antennapod.itunes;


import android.test.ActivityInstrumentationTestCase2;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.fragment.TrendingFragment;

/**
 * Created by franc on 2018-02-26.
 */

public class ItunesApiTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private TrendingFragment trendingFragment;

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
        getActivity().getSupportFragmentManager().beginTransaction().add(trendingFragment, TrendingFragment.class.getSimpleName()).commit();
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

        //Assertions
        assertNotNull(trendingFragment.getTopList());
        assertEquals( 25,trendingFragment.getTopList().size());
    }
}
