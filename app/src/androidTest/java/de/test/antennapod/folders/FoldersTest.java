package de.test.antennapod.folders;

import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.fragment.FoldersFragment;

import static de.danoeh.antennapod.core.storage.PodDBAdapter.deleteAllFolders;
import static de.danoeh.antennapod.core.storage.PodDBAdapter.deleteFoldersTable;

/**
 * Created by William on 2018-03-16.
 */

public class FoldersTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private FoldersFragment foldersFragment;
    List<Folder> folders;

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

    public String randomAlphabet(){
        return UUID.randomUUID().toString().substring(0, 8);
    }

    //Test folder's creation
    public void testAddFolder() throws InterruptedException {

        //Original number of folders in foldersFragment
        foldersFragment.loadFolders();
        int originalNumOfFolders = foldersFragment.getFolders().size();

        //Assign random name
        String firstFolderName = randomAlphabet();
        String secondFolderName = randomAlphabet();

        //Array of folders' names
        List<String> foldersName = new LinkedList<>();

        //Creating folders containing no episodes
        Folder firstFolder = new Folder(firstFolderName, null);
        Folder secondFolder = new Folder(secondFolderName, null);

        //Add them to database
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.addFolder(firstFolder);
        adapter.addFolder(secondFolder);
        adapter.addFolder(new Folder(firstFolderName, null)); //Should not add this folder as the name already exists
        adapter.close();

        //Update fragment and load folders into list of folders
        foldersFragment.loadFolders();
        folders = foldersFragment.getFolders();

        //Update list of folders name
        for(Folder folder : folders){
            foldersName.add(folder.getName());
        }

        //Assertions
        assertEquals(foldersName.size(), folders.size()); //list of folders name should be same size as folders list in fragment
        assertEquals(2 + originalNumOfFolders , folders.size()); //2 folders should have been added to DB

        //Verifying that names correspond to folders created
        for(int i = 0; i < folders.size(); i++){
            System.out.println(folders.get(i).getName());
            System.out.println(foldersName.get(i));
            assertEquals(foldersName.get(i), folders.get(i).getName());
        }

        //You might want to delete all folders from database from time to time
        /*deleteAllFolders();
        folders = DBReader.getFolderList();
        assertEquals(0, folders.size());*/
    }

}
