package de.danoeh.antennapod.service;


import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import okhttp3.OkHttpClient;
import retrofit2.Call;

/**
 * Created by FrankSauve on 2018-04-09.
 */

public class TwitterService {

    private static TwitterService instance = null;
    private TwitterSession session;

    public static TwitterService getInstance(){
        if(instance == null){
            instance = new TwitterService();
        }
        return instance;
    }

    public void tweet(String tweetMsg){

        OkHttpClient client = AntennapodHttpClient.getHttpClient();
        TwitterApiClient twitterApiClient = new TwitterApiClient(session, client);
        TwitterCore.getInstance().addApiClient(session, twitterApiClient);
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> call = statusesService.update(tweetMsg, null, null, null, null, null, null, null, null);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
            }
            @Override
            public void failure(TwitterException e) {
                System.out.println(e);
            }
        });

    }

    public void setTwitterSession(TwitterSession ts){
        this.session = ts;
    }

    public TwitterSession getTwitterSession(){
        return this.session;
    }
}
