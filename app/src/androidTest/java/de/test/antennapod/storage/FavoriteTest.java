package de.test.antennapod.storage;

import android.content.Context;
import android.database.Cursor;
import android.test.InstrumentationTestCase;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.PodDBAdapter;

/**
 * Created by sarb on 2018-02-20.
 */

public class FavoriteTest extends InstrumentationTestCase{

    private static final String TEST_FOLDER = "testDBWriter";

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        assertTrue(PodDBAdapter.deleteDatabase());

        final Context context = getInstrumentation().getTargetContext();
        File testDir = context.getExternalFilesDir(TEST_FOLDER);
        assertNotNull(testDir);
        for (File f : testDir.listFiles()) {
            f.delete();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create new database
        PodDBAdapter.init(getInstrumentation().getTargetContext());
        PodDBAdapter.deleteDatabase();
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.close();
    }

    public void testAddToFavorites() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        File destFolder = getInstrumentation().getTargetContext().getExternalFilesDir(TEST_FOLDER);
        assertNotNull(destFolder);

        Feed feed = new Feed("url", null, "title");
        feed.setItems(new ArrayList<>());

        //feed.setImage(null);

        List<File> itemFiles = new ArrayList<File>();
        // create items with downloaded media files
        for (int i = 0; i < 10; i++) {
            FeedItem item = new FeedItem(0, "Item " + i, "Item" + i, "url", new Date(), FeedItem.UNPLAYED, feed);
            feed.getItems().add(item);

            File enc = new File(destFolder, "file " + i);
            assertTrue(enc.createNewFile());

            itemFiles.add(enc);
            FeedMedia media = new FeedMedia(0, item, 1, 1, 1, "mime_type", enc.getAbsolutePath(), "download_url", true, null, 0, 0);
            item.setMedia(media);
        }

        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.setCompleteFeed(feed);
        adapter.close();

        assertTrue(feed.getId() != 0);
        //Re implementation of method found in EpisodesApplyActionFragment
        for (FeedItem item : feed.getItems()) {
            assertTrue(item.getId() != 0);
            assertTrue(item.getMedia().getId() != 0);
            if(item.hasMedia()){
                DBWriter.addFavoriteItemById(item.getMedia().getId());
            }
        }



        // check if files still exist
        for (File f : itemFiles) {
            assertTrue(f.exists());
        }

        adapter = PodDBAdapter.getInstance();
        adapter.open();
        Cursor c = adapter.getFeedCursor(feed.getId());
        assertTrue(c.getCount() != 0);
        c.close();
        for (FeedItem item : feed.getItems()) {
            c = adapter.getFeedItemCursor(String.valueOf(item.getId()));
            assertTrue(c.getCount() != 0);
            c.close();
            c = adapter.getSingleFeedMediaCursor(item.getMedia().getId());
            assertTrue(c.getCount() != 0);
            c.close();
        }
        adapter.close();
    }

}
