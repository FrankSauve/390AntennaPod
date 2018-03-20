package de.danoeh.antennapod.fragment;


import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by RaphaelleG on 18-03-19.
 */

public class DiscoveryFragment extends ItunesSearchFragment {

    public static final String TAG = "DiscoveryFragment";


    /**
     * Constructor
     */
    public DiscoveryFragment() {
        // Required empty public constructor
    }


    @Override
    public void loadToplist() {
        final List<Integer> discoveryCategories = UserPreferences.getDiscoveryCategoriesButtons();

        if (discoveryCategories != null) {
            loadCategories(discoveryCategories);
        }

    }


    public void loadCategories(List<Integer> discoveryCategoriesId){

        List<Integer> discoveryIds = new ArrayList<>();

            if (subscription != null) {
                subscription.unsubscribe();
            }

            gridView.setVisibility(View.GONE);
            txtvError.setVisibility(View.GONE);
            butRetry.setVisibility(View.GONE);
            txtvEmpty.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            for(int c = 0; c < discoveryCategoriesId.size(); c++){
                switch(discoveryCategoriesId.get(c)){
                    case 0:
                        discoveryIds.add(ARTS_GENRE_ID);
                        break;
                    case 1:
                        discoveryIds.add(COMEDY_GENRE_ID);
                        break;

                }
            }

            for(int b = 0; b < discoveryIds.size(); b++){

                int getID = discoveryIds.get(b);

                subscription = Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
                    String lang = Locale.getDefault().getLanguage();
                    String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=25/genre=" + getID + "/json";
                    OkHttpClient client = AntennapodHttpClient.getHttpClient();
                    Request.Builder httpReq = new Request.Builder()
                            .url(url)
                            .header("User-Agent", ClientConfig.USER_AGENT);
                    List<ItunesAdapter.Podcast> results = new ArrayList<>();
                    try {
                        Response response = client.newCall(httpReq.build()).execute();
                        if(!response.isSuccessful()) {
                            // toplist for language does not exist, fall back to united states
                            url = "https://itunes.apple.com/us/rss/toppodcasts/limit=25/genre=" + getID + "/json";
                            httpReq = new Request.Builder()
                                    .url(url)
                                    .header("User-Agent", ClientConfig.USER_AGENT);
                            response = client.newCall(httpReq.build()).execute();
                        }
                        if(response.isSuccessful()) {
                            String resultString = response.body().string();
                            //System.out.println(resultString);
                            JSONObject result = new JSONObject(resultString);
                            JSONObject feed = result.getJSONObject("feed");
                            JSONArray entries = feed.getJSONArray("entry");

                            for(int i = 0; i < entries.length(); i++) {
                                JSONObject json = entries.getJSONObject(i);
                                ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromToplist(json);
                                results.add(podcast);
                            }
                        }
                        else {
                            String prefix = getString(R.string.error_msg_prefix);
                            subscriber.onError(new IOException(prefix + response));
                        }
                    } catch (IOException | JSONException e) {
                        subscriber.onError(e);
                    }
                    subscriber.onNext(results);
                    subscriber.onCompleted();
                })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(podcasts -> {
                            progressBar.setVisibility(View.GONE);
                            categorySearchResults = podcasts;
                            updateData(categorySearchResults);
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                            progressBar.setVisibility(View.GONE);
                            txtvError.setText(error.toString());
                            txtvError.setVisibility(View.VISIBLE);
                            butRetry.setOnClickListener(v -> loadToplist());
                            butRetry.setVisibility(View.VISIBLE);
                        });
            }




    }


}
