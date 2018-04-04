package de.test.antennapod.discovery;

import android.test.ActivityInstrumentationTestCase2;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.preferences.UserPreferences;
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


    //Test Discovery loadCategories
    public void testLoadCategories() throws InterruptedException {

        // Create new List for User preferences
        List<Integer> newCategories = new ArrayList<>();
        newCategories.add(6);
        newCategories.add(8);
        newCategories.add(9);

        //Assign List to new values for Buttons
        UserPreferences.setPrefDiscoveryButtons(newCategories);

        //onStartDiscovery Page

        discoveryFragment.onStart();

        List<Integer> test = discoveryFragment.getIds();

        assertEquals(test, newCategories);
        assertTrue(discoveryFragment.getCategoryId().contains(1309));
        assertTrue(discoveryFragment.getCategoryId().contains(1311));
        assertTrue(!discoveryFragment.getCategoryId().contains(1310));
        assertTrue(discoveryFragment.getCategoryId().contains(1314));

    }
}
