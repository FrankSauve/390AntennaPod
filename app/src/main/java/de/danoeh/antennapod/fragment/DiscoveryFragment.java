package de.danoeh.antennapod.fragment;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.SubscriptionsAdapter;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.core.storage.DBReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by RaphaelleG on 18-03-19.
 */

public class DiscoveryFragment extends ItunesSearchFragment {

    public static final String TAG = "DiscoveryFragment";

    private static List<Integer> Ids = new ArrayList<>();

    private static List<Integer> CategoryId = new ArrayList<>();
    private static List<String> queryList = new ArrayList<>();

    private Subscription subscription;

    private DBReader.NavDrawerData navDrawerData;


    /**
     * Constructor
     */
    public DiscoveryFragment() {
        // Required empty public constructor
    }


    public static List<Integer> getIds(){
        return Ids;
    }

    @Override
    public void onStart() {
        super.onStart();

        List<Integer> discoveryCategories = UserPreferences.getDiscoveryCategoriesButtons();

        // If automatic recommendation is selected
        if(discoveryCategories.contains(0)){
            loadSubscriptions();
        }
        else{
            if (Ids != discoveryCategories) {
                loadCategories(discoveryCategories);
                Ids = discoveryCategories;
            }
        }

    }

    @Override
    public void loadToplist() {
        List<Integer> discoveryCategories = UserPreferences.getDiscoveryCategoriesButtons();
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
                    case 1:
                        discoveryIds.add(ARTS_GENRE_ID);
                        break;
                    case 2:
                        discoveryIds.add(COMEDY_GENRE_ID);
                        break;
                    case 3:
                        discoveryIds.add(EDUCATION_GENRE_ID);
                        break;
                    case 4:
                        discoveryIds.add(KIDS_AND_FAMILY_GENRE_ID);
                        break;
                    case 5:
                        discoveryIds.add(HEALTH_GENRE_ID);
                        break;
                    case 6:
                        discoveryIds.add(TV_AND_FILM_GENRE_ID);
                        break;
                    case 7:
                        discoveryIds.add(MUSIC_GENRE_ID);
                        break;
                    case 8:
                        discoveryIds.add(NEWS_AND_POLITICS_GENRE_ID);
                        break;
                    case 9:
                        discoveryIds.add(RELIGION_AND_SPIRITUALITY_GENRE_ID);
                        break;
                    case 10:
                        discoveryIds.add(SCIENCE_AND_MEDECINE_GENRE_ID);
                        break;
                    case 11:
                        discoveryIds.add(SPORTS_AND_RECREATION_GENRE_ID);
                        break;
                    case 12:
                        discoveryIds.add(TECHNOLOGY_GENRE_ID);
                        break;
                    case 13:
                        discoveryIds.add(BUSINESS_GENRE_ID);
                        break;
                    case 14:
                        discoveryIds.add(GAMES_AND_HOBBIES_GENRE_ID);
                        break;
                    case 15:
                        discoveryIds.add(SOCIETY_AND_CULTURE_GENRE_ID);
                        break;
                    case 16:
                        discoveryIds.add(GOVERNMENT_AND_ORGANIZATION_GENRE_ID);
                        break;
                }
            }

            CategoryId = discoveryIds;
            List<ItunesAdapter.Podcast> results = new ArrayList<>();



                subscription = Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
                    for(int b = 0; b < discoveryIds.size(); b++) {

                        int getID = discoveryIds.get(b);

                        String lang = Locale.getDefault().getLanguage();
                        String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=25/genre=" + getID + "/json";
                        OkHttpClient client = AntennapodHttpClient.getHttpClient();
                        Request.Builder httpReq = new Request.Builder()
                                .url(url)
                                .header("User-Agent", ClientConfig.USER_AGENT);
                        try {
                            Response response = client.newCall(httpReq.build()).execute();
                            if (!response.isSuccessful()) {
                                // toplist for language does not exist, fall back to united states
                                url = "https://itunes.apple.com/us/rss/toppodcasts/limit=25/genre=" + getID + "/json";
                                httpReq = new Request.Builder()
                                        .url(url)
                                        .header("User-Agent", ClientConfig.USER_AGENT);
                                response = client.newCall(httpReq.build()).execute();
                            }
                            if (response.isSuccessful()) {
                                String resultString = response.body().string();
                                //System.out.println(resultString);
                                JSONObject result = new JSONObject(resultString);
                                JSONObject feed = result.getJSONObject("feed");
                                JSONArray entries = feed.getJSONArray("entry");

                                for (int i = 0; i < 10; i++) {
                                    JSONObject json = entries.getJSONObject(i);
                                    ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromToplist(json);
                                    results.add(podcast);
                                }
                            } else {
                                String prefix = getString(R.string.error_msg_prefix);
                                subscriber.onError(new IOException(prefix + response));
                            }
                        } catch (IOException | JSONException e) {
                            subscriber.onError(e);
                        }

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

    private void loadSubscriptions() {
        if(subscription != null) {
            subscription.unsubscribe();
        }
        subscription = Observable.fromCallable(DBReader::getNavDrawerData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    navDrawerData = result;
                    //subscriptionAdapter.notifyDataSetChanged();
                    findAutomaticRecommendations();
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    /**
     * Algorithm to automatically suggest podcast based on the subscribed podcast
     */
    public void findAutomaticRecommendations() {
        List<Feed> feeds = navDrawerData.feeds;

        super.setIsInDisvoryTab(true);

        //Generate random int
        int min = 0;
        int max = feeds.size() - 1;
        int randomFeed = min + (int)(Math.random() * ((max - min) + 1));
        String author = feeds.get(randomFeed).getAuthor();
        String title = feeds.get(randomFeed).getTitle();
        List<ItunesAdapter.Podcast> podcastSuggestions;

        //Set action bar title to "Like ...."
        ((MainActivity)getActivity()).setActionBarTitle("Similar To:  " + title);

        //Search similar artists
//        MenuItem menuItem = getMenu().findItem(R.id.itunes_search_artist);
//        super.search(author, menuItem);
//        podcastSuggestions = super.getSearchResults();

        //Search for podcasts with similar titles
//        menuItem = getMenu().findItem(R.id.itunes_search_title);
        ArrayList<String> keywords = titleKeywordExtractor(title);
        String fullTitle="";
        for(int i=0;i<keywords.size();i++){
            if(i<keywords.size()-1){
                fullTitle += keywords.get(i)+" ";
            }
            else{
                fullTitle += keywords.get(i);
            }
        }
        //If there are valid keywords, search and add them to the list.
        String query="";
        if(keywords.size()>0){

            if(keywords.size()==1)
                query = keywords.get(0);
            else if (keywords.size()==2)
                query =keywords.get(0)+" "+keywords.get(1);
            else
                query=keywords.get(0)+" "+keywords.get(1)+" "+keywords.get(2);

//            super.search(query,menuItem);
//            podcastSuggestions.addAll(super.getSearchResults());
        }
//        //remove the suggested podcast from the results
//        podcastSuggestions.remove(randomFeed);
//
//        //remove all duplications
//        Set<ItunesAdapter.Podcast> tempSet = new HashSet<>();
//        tempSet.addAll(podcastSuggestions);
//        podcastSuggestions.clear();
//        podcastSuggestions.addAll(tempSet);
//
//        //Podcast suggestions are added to the page with duplicates being removed
//        super.appendData(podcastSuggestions);
        loadRecommended(author, query, fullTitle);
    }

    //method used to extract important keywords from a title by removing common articles from podcast titles
    private ArrayList<String> titleKeywordExtractor(String title){
        //Remove all non-lettered characters, convert to lowercase and store individual words in an ArrayList.
        ArrayList<String> splitTitle = new ArrayList(Arrays.asList(title.toLowerCase().replaceAll("[^a-zA-Z0-9 -]","").split(" ")));
        ArrayList<String> keywords = new ArrayList<String>();
        //iterate through the array, only adding non-determiners
        for(String s : splitTitle){
            switch (s){
                case "the": //Definite article
                case "a":case "an": //Indefinite articles
                case "this":case "that":case "these":case "those": //Demonstrators
                case "my":case "your":case "his":case "her":case "its":case "our":case "their": //Pronouns and Possessive determiners
                case "few":case "little":case "much":case "many":case "most":case "some":case "any":case "enough": //Quantifiers
                case "all":case "both":case "half":case "either":case "neither":case "each":case "every": //Distributives
                case "other":case "another": //Difference words
                case "such":case "what":case "rather":case "quite": //Pre-determiners
                    break;
                default:
                    keywords.add(s);
                    break;
            }
        }
        keywords.trimToSize();
        return keywords;
    }

    public void loadRecommended(String queryArtist, String queryTitle, String queryFullTitle){

        List<String> queries = new ArrayList<String>();
        queries.add(queryArtist);
        queries.add(queryTitle);

        if (subscription != null) {
            subscription.unsubscribe();
        }

        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        queryList = queries;
        List<ItunesAdapter.Podcast> results = new ArrayList<>();

        subscription = Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
            for(int b = 0; b < queries.size(); b++) {

                String query = queries.get(b);

                String lang = Locale.getDefault().getLanguage();
                String url ="";
                if(b==0){
                    url = String.format(API_URL_ARTIST_SEARCH, query.toLowerCase()).replace(' ', '+');
                }
                else{
                    url = String.format(API_URL_TITLE_SEARCH, query.toLowerCase()).replace(' ', '+');
                }
                OkHttpClient client = AntennapodHttpClient.getHttpClient();
                Request.Builder httpReq = new Request.Builder()
                        .url(url)
                        .header("User-Agent", ClientConfig.USER_AGENT);
                try {
                    Response response = client.newCall(httpReq.build()).execute();

                    if (response.isSuccessful()) {
                        String resultString = response.body().string();
                        //System.out.println(resultString);
                        JSONObject result = new JSONObject(resultString);
                        JSONArray j = result.getJSONArray("results");

                        int limit = 10;
                        for (int i = 0; i < limit && i<j.length(); i++) {
                            JSONObject json = j.getJSONObject(i);
                            ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromSearch(json);
                            String temp1="";
                            ArrayList<String> temp2 = titleKeywordExtractor(podcast.title);
                            for(int count = 0; count<temp2.size();count++){
                                if(count!=temp2.size()-1){
                                    temp1 += temp2.get(count)+" ";
                                }
                                else{
                                    temp1 += temp2.get(count);
                                }

                            }
                            if(!temp1.equals(queryFullTitle)){ // excluding the same podcast
                                results.add(podcast);
                            }
                            else{
                                limit++;
                            }
                        }
                    } else {
                        String prefix = getString(R.string.error_msg_prefix);
                        subscriber.onError(new IOException(prefix + response));
                    }
                } catch (IOException | JSONException e) {
                    subscriber.onError(e);
                }

            }
            subscriber.onNext(results);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcasts -> {
                    progressBar.setVisibility(View.GONE);

                    appendData(podcasts);

                    searchResults = podcasts;
                    updateData(searchResults);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> loadRecommended(queryArtist,queryTitle, queryFullTitle));
                    butRetry.setVisibility(View.VISIBLE);
                });
    }

    public static List<Integer> getCategoryId() { return CategoryId; }

    private SubscriptionsAdapter.ItemAccess itemAccess = new SubscriptionsAdapter.ItemAccess() {
        @Override
        public int getCount() {
            if (navDrawerData != null) {
                return navDrawerData.feeds.size();
            } else {
                return 0;
            }
        }

        @Override
        public Feed getItem(int position) {
            if (navDrawerData != null && 0 <= position && position < navDrawerData.feeds.size()) {
                return navDrawerData.feeds.get(position);
            } else {
                return null;
            }
        }

        @Override
        public int getFeedCounter(long feedId) {
            return navDrawerData != null ? navDrawerData.feedCounters.get(feedId) : 0;
        }
    };


}
