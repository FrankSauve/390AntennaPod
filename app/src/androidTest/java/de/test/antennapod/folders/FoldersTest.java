package de.test.antennapod.folders;

import android.test.ActivityInstrumentationTestCase2;

import java.util.LinkedList;
import java.util.List;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.fragment.FoldersFragment;

/**
 * Created by William on 2018-03-16.
 */

public class FoldersTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private FoldersFragment foldersFragment;

    // Constructor
    public FoldersTest(){
        super("de.danoeh.antennapod.activity", MainActivity.class);
    }

    /**
     * Starts the Main activity
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        foldersFragment = new FoldersFragment();
        getActivity().getSupportFragmentManager().beginTransaction().add(foldersFragment, FoldersFragment.class.getSimpleName()).commit();
        getInstrumentation().waitForIdleSync();
    }

    //Test folder's creation
    public void testAddFolder() throws InterruptedException {

        Folder firstFolder = new Folder("First", null);
        Folder secondFolder = new Folder("Second", null);
        PodDBAdapter podDBAdapter = PodDBAdapter.getInstance();
        podDBAdapter.open();
        long firstId = podDBAdapter.addFolder(firstFolder);
        long secondId = podDBAdapter.addFolder(secondFolder);
        FeedItem item = new FeedItem();
        item.setId(1);
        System.out.println(podDBAdapter.isItemInFavorites(item));
        podDBAdapter.close();
        /*DBWriter.addFolder(firstFolder);
        DBWriter.addFolder(secondFolder);
        DBWriter.addFolder(firstFolder);
        foldersFragment.loadFolders();

        List<Folder> folders;
        folders = DBReader.getFolderList();*/

        System.out.println(firstId);
        System.out.println(secondId);

        //assertEquals(2, folders);
    }

}
