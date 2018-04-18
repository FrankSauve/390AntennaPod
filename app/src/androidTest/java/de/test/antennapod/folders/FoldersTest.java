package de.test.antennapod.folders;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.fragment.FoldersFragment;
import de.test.antennapod.storage.DBTestUtils;

import static de.danoeh.antennapod.core.storage.PodDBAdapter.deleteAllFolders;
import static de.danoeh.antennapod.core.storage.PodDBAdapter.deleteFoldersTable;

/**
 * Created by William on 2018-03-16.
 */

public class FoldersTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String TAG = "FoldersTest";
    private FoldersFragment foldersFragment;
    List<Folder> folders;
    PodDBAdapter adapter;

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

        //You can delete this whole code once your local devices have the tables and are set up properly
        try{
            setUpTables(); //Creates folders and itemsfolders tables in DB for local devices in case they do not have these tables already
        }
        catch(android.database.sqlite.SQLiteException e){
            //Catch error: means you already have one of the tables (probably folders table so let's create itemsfolder table)
            Log.e(TAG, "e: " + e.getMessage());
            try{
                PodDBAdapter.createItemsFoldersTable(); //Creates itemsfolders table in DB for local devices in case they do not have this table already
            }
            catch(android.database.sqlite.SQLiteException e1){
                //Catch this error: means you already have this table
                Log.e(TAG, "e1: " + e1.getMessage());
                try{
                    PodDBAdapter.addFolderNameColumnToFeedItemsTable(); //Add folder_name column to feeditems table in DB for local devices in case it is not added yet
                }
                catch(android.database.sqlite.SQLiteException e2){
                    //Catch this error: last catch
                    Log.e(TAG, "e2: " + e2.getMessage());
                    //Do nothing in this case the device should be set up properly in this case
                }
            }
        }

        foldersFragment = new FoldersFragment();
        getActivity().getSupportFragmentManager().beginTransaction().add(foldersFragment, FoldersFragment.class.getSimpleName()).commit();
        getInstrumentation().waitForIdleSync();
    }

    private void setUpTables(){
        PodDBAdapter.createFoldersTable();
        PodDBAdapter.createItemsFoldersTable();
        PodDBAdapter.addFolderNameColumnToFeedItemsTable();
    }

    public String randomAlphabet(){
        return UUID.randomUUID().toString().substring(0, 8);
    }

    //Test folder's creation
    public void testAddFolder() throws InterruptedException {

        //Original number of folders in foldersFragment
        foldersFragment.loadFolders();
        folders = foldersFragment.getFolders();
        int originalNumOfFolders = folders.size();

        //Assign random name
        String firstFolderName = randomAlphabet();
        String secondFolderName = randomAlphabet();

        //Array of folders' names
        List<String> foldersName = new LinkedList<>();

        //Creating folders containing no episodes
        Folder firstFolder = new Folder(firstFolderName, null);
        Folder secondFolder = new Folder(secondFolderName, null);

        //Create folders
        createFolder(firstFolder);
        createFolder(secondFolder);
        createFolder(new Folder(firstFolderName, null)); //Should not add this folder as the name already exists

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
            assertEquals(foldersName.get(i), folders.get(i).getName());
        }

        //Clear database
        deleteFolder(firstFolder);
        deleteFolder(secondFolder);
    }

    private long createFolder(Folder folder){
        adapter = PodDBAdapter.getInstance();
        adapter.open();
        long id = adapter.addFolder(folder);
        adapter.close();

        return id;
    }

    private void deleteAllFolders(){
        folders = DBReader.getFolderList();

        for(Folder folder : folders){
            deleteFolder(folder);
        }
    }

    private void deleteFolder(Folder folder){
        adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.removeFolder(folder);
        adapter.close();
    }

    //Test folder's deletion
    public void testRemoveFolder() throws InterruptedException {

        //Original number of folders in foldersFragment
        foldersFragment.loadFolders();
        folders = foldersFragment.getFolders();
        int originalNumOfFolders = folders.size();

        //If there is no folder then create a random one
        if(originalNumOfFolders == 0){
            String newFolderName = randomAlphabet();
            Folder newFolder = new Folder(newFolderName, null);
            createFolder(newFolder);
            originalNumOfFolders++;
        }

        //Update fragment and load folders into list of folders
        foldersFragment.loadFolders();
        folders = foldersFragment.getFolders();

        //delete the last folder created
        deleteFolder(folders.get(originalNumOfFolders - 1));

        //Assertion
        folders = DBReader.getFolderList();
        assertEquals(originalNumOfFolders - 1, folders.size());
    }

    private void addFeedItemToFolder(Folder folder, FeedItem item){
        adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.addFolderItem(folder,item);
        adapter.close();
    }

    private void addFeedItemsToFolder(Folder folder, List<FeedItem> items){
        for(FeedItem item : items){
            addFeedItemToFolder(folder, item);
        }
    }

    public void testAddItemsToFolder() throws Exception {
        //Loading 1 Feed
        List<Feed> feeds = DBTestUtils.saveFeedlist(1, 4, false, false, 0);

        //List of FeedItems for the folders
        List<FeedItem> firstFolderItems = new ArrayList<>();
        List<FeedItem> secondFolderItems = new ArrayList<>();

        //Create FeedItems from the loaded Feed
        FeedItem item1 = feeds.get(0).getItems().get(0);
        FeedItem item2 = feeds.get(0).getItems().get(1);
        FeedItem item3 = feeds.get(0).getItems().get(2);
        FeedItem item4 = feeds.get(0).getItems().get(3);

        //Add FeedItems to ArrayLists
        firstFolderItems.add(item1);
        firstFolderItems.add(item2);
        secondFolderItems.add(item3);
        secondFolderItems.add(item4);

        //Assign random name
        String firstFolderName = randomAlphabet();
        String secondFolderName = randomAlphabet();

        //Creating folders containing no episodes
        Folder firstFolder = new Folder(firstFolderName, null);
        Folder secondFolder = new Folder(secondFolderName, null);

        //Create folders
        long firstFolderId = createFolder(firstFolder);
        long secondFolderId = createFolder(secondFolder);

        //Add items to the folders(2 in firstFolder and 2 in secondFolder)
        addFeedItemsToFolder(firstFolder, firstFolderItems);
        addFeedItemsToFolder(secondFolder, secondFolderItems);

        //Updating folders and and loading episodes inside folders
        firstFolder = DBReader.getFolder(firstFolderId);
        assertNotNull(firstFolder.getEpisodes());
        secondFolder = DBReader.getFolder(secondFolderId);
        assertNotNull(secondFolder.getEpisodes());

        //Assertions
        assertEquals(2, firstFolder.getEpisodesNum());
        assertEquals(2, secondFolder.getEpisodesNum());

        //Clear database
        deleteFolder(firstFolder);
        deleteFolder(secondFolder);
        removeFeed(feeds.get(0));
    }

    //Delete feed from database
    public void removeFeed(Feed feed){
        adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.removeFeed(feed);
        adapter.close();
    }

    //Test renaming folder
    public void testRenameFolder() throws InterruptedException {
        adapter = PodDBAdapter.getInstance();
        String folderName = "Old Name";

        //Creating a folder with name above
        Folder folder = new Folder(folderName, null);
        createFolder(folder);
        assertEquals(folderName, folder.getName());

        //New name for folder
        String newFolderName = "New Name";
        assertFalse(newFolderName.equals(folder.getName()));

        adapter.renameFolder(folder.getId(), folder.getName(), newFolderName);
        assertEquals(newFolderName, folder.getName());

    }

}
