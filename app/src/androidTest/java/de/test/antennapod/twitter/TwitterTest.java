package de.test.antennapod.twitter;

import android.test.ActivityInstrumentationTestCase2;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.service.TwitterService;

/**
 * Created by franc on 2018-04-14.
 */

public class TwitterTest extends ActivityInstrumentationTestCase2<MainActivity> {

    //Constructor
    public TwitterTest(){
        super("de.danoeh.antennapod.activity", MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getInstrumentation().waitForIdleSync();
    }

    /**
     * This test is ignored by circleCI.
     * I was not able to mock any objects from the twitter sdk, therefore I have to use
     * real twitter sessions. (See issue #89 from explanation)
     * This test will only work on devices with twitter installed and if you are logged
     * in on the twitter app.
     */
    public void testTweet() throws InterruptedException {
        Twitter.initialize(getActivity());
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        TwitterService twitterService = TwitterService.getInstance();
        twitterService.setTwitterSession(session);

        twitterService.tweet("HELLO WORLD");

        Thread.sleep(5000);

        assertEquals("HELLO WORLD", twitterService.getResultString());
    }

}
