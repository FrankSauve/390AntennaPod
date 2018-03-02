package de.danoeh.antennapod.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.menuhandler.MenuItemUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static de.danoeh.antennapod.adapter.itunes.ItunesAdapter.Podcast;

//Searches iTunes store for given string and displays results in a list
public class ItunesSearchFragment extends Fragment {

    private static final String TAG = "ItunesSearchFragment";

    private static final String API_URL = "https://itunes.apple.com/search?media=podcast&term=%s";

    //itunes api url to search podcasts by artist name
    private static final String API_URL_ARTIST_SEARCH  = "https://itunes.apple.com/search?entity=podcast&attribute=artistTerm&term=%s";

    //itunes api genre ids to search podcasts by category
    public static final int ARTS_GENRE_ID = 1301;
    public static final int COMEDY_GENRE_ID = 1303;
    public static final int EDUCATION_GENRE_ID = 1304;
    public static final int KIDS_AND_FAMILY_GENRE_ID = 1305;
    public static final int HEALTH_GENRE_ID = 1307;
    public static final int TV_AND_FILM_GENRE_ID = 1309;
    public static final int MUSIC_GENRE_ID = 1310;
    public static final int NEWS_AND_POLITICS_GENRE_ID = 1311;
    public static final int RELIGION_AND_SPIRITUALITY_GENRE_ID = 1314;
    public static final int SCIENCE_AND_MEDECINE_GENRE_ID = 1315;
    public static final int SPORTS_AND_RECREATION_GENRE_ID = 1316;
    public static final int TECHNOLOGY_GENRE_ID = 1318;
    public static final int BUSINESS_GENRE_ID = 1321;
    public static final int GAMES_AND_HOBBIES_GENRE_ID = 1323;
    public static final int SOCIETY_AND_CULTURE_GENRE_ID = 1324;
    public static final int GOVERNMENT_AND_ORGANIZATION_GENRE_ID = 1325;


    /**
     * Adapter responsible with the search results
     */
    private ItunesAdapter adapter;
    private GridView gridView;
    private ProgressBar progressBar;
    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;

    /**
     * List of podcasts retreived from the search
     */
    private List<Podcast> searchResults;
    private List<Podcast> categorySearchResults;
    private List<Podcast> languageSearchResults;
    private List<Podcast> topList;
    private Subscription subscription;

    /**
     * Replace adapter data with provided search results from SearchTask.
     * @param result List of Podcast objects containing search results
     */
    void updateData(List<Podcast> result) {
        this.searchResults = result;
        adapter.clear();
        if (result != null && result.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
            txtvEmpty.setVisibility(View.GONE);
            for (Podcast p : result) {
                adapter.add(p);
            }
            adapter.notifyDataSetInvalidated();
        } else {
            gridView.setVisibility(View.GONE);
            txtvEmpty.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Constructor
     */
    public ItunesSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_itunes_search, container, false);
        gridView = (GridView) root.findViewById(R.id.gridView);
        adapter = new ItunesAdapter(getActivity(), new ArrayList<>());
        gridView.setAdapter(adapter);

        //Show information about the podcast when the list item is clicked
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Podcast podcast = searchResults.get(position);
            if(podcast.feedUrl == null) {
                return;
            }
            if (!podcast.feedUrl.contains("itunes.apple.com")) {
                Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
                intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, podcast.feedUrl);
                intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
                startActivity(intent);
            } else {
                gridView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                subscription = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                            OkHttpClient client = AntennapodHttpClient.getHttpClient();
                            Request.Builder httpReq = new Request.Builder()
                                    .url(podcast.feedUrl)
                                    .header("User-Agent", ClientConfig.USER_AGENT);
                            try {
                                Response response = client.newCall(httpReq.build()).execute();
                                if (response.isSuccessful()) {
                                    String resultString = response.body().string();
                                    JSONObject result = new JSONObject(resultString);
                                    JSONObject results = result.getJSONArray("results").getJSONObject(0);
                                    String feedUrl = results.getString("feedUrl");
                                    subscriber.onNext(feedUrl);
                                } else {
                                    String prefix = getString(R.string.error_msg_prefix);
                                    subscriber.onError(new IOException(prefix + response));
                                }
                            } catch (IOException | JSONException e) {
                                subscriber.onError(e);
                            }
                            subscriber.onCompleted();
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(feedUrl -> {
                            progressBar.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
                            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, feedUrl);
                            intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
                            startActivity(intent);
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                            progressBar.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);
                            String prefix = getString(R.string.error_msg_prefix);
                            new MaterialDialog.Builder(getActivity())
                                    .content(prefix + " " + error.getMessage())
                                    .neutralText(android.R.string.ok)
                                    .show();
                        });
            }
        });
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        txtvError = (TextView) root.findViewById(R.id.txtvError);
        butRetry = (Button) root.findViewById(R.id.butRetry);
        txtvEmpty = (TextView) root.findViewById(android.R.id.empty);

        loadToplist();

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        adapter = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.itunes_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        //MenuItem advancedSearchItem = menu.findItem(R.id.itunes_advanced_search);
        final SearchView sv = (SearchView) MenuItemCompat.getActionView(searchItem);
        //MenuItemCompat.setActionView(advancedSearchItem, View.GONE);
        MenuItemUtils.adjustTextColor(getActivity(), sv);
        sv.setQueryHint(getString(R.string.search_itunes_label));
        sv.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                sv.clearFocus();
                search(s, searchItem);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if(searchResults != null) {
                    searchResults = null;
                    updateData(topList);
                }
                return true;
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                //Artist item
                case R.id.itunes_search_artist:
                    final SearchView sv = (SearchView) MenuItemCompat.getActionView(item);
                    MenuItemUtils.adjustTextColor(getActivity(), sv);
                    sv.setQueryHint(getString(R.string.artist_search));
                    sv.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {
                            sv.clearFocus();
                            search(s, item);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String s) {
                            return false;
                        }
                    });
                    MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            if(searchResults != null) {
                                searchResults = null;
                                updateData(topList);
                            }
                            return true;
                        }
                    });
                    return true;
                //Categoy items
                case R.id.search_arts:
                    loadCategory(ARTS_GENRE_ID);
                    return true;
                case R.id.search_comedy:
                    loadCategory(COMEDY_GENRE_ID);
                    return true;
                case R.id.search_news_politics:
                    loadCategory(NEWS_AND_POLITICS_GENRE_ID);
                    return true;
                case R.id.search_kids_family:
                    loadCategory(KIDS_AND_FAMILY_GENRE_ID);
                    return true;
                case R.id.search_games_hobbies:
                    loadCategory(GAMES_AND_HOBBIES_GENRE_ID);
                    return true;
                case R.id.search_government_organization:
                    loadCategory(GOVERNMENT_AND_ORGANIZATION_GENRE_ID);
                    return true;
                case R.id.search_technology:
                    loadCategory(TECHNOLOGY_GENRE_ID);
                    return true;
                case R.id.search_tv_film:
                    loadCategory(TV_AND_FILM_GENRE_ID);
                    return true;
                case R.id.search_education:
                    loadCategory(EDUCATION_GENRE_ID);
                    return true;
                case R.id.search_health:
                    loadCategory(HEALTH_GENRE_ID);
                    return true;
                case R.id.search_science_medecine:
                    loadCategory(SCIENCE_AND_MEDECINE_GENRE_ID);
                    return true;
                case R.id.search_society_culture:
                    loadCategory(SOCIETY_AND_CULTURE_GENRE_ID);
                    return true;
                case R.id.search_music:
                    loadCategory(MUSIC_GENRE_ID);
                    return true;
                case R.id.search_religion_spirituality:
                    loadCategory(RELIGION_AND_SPIRITUALITY_GENRE_ID);
                    return true;
                case R.id.search_sports_recreation:
                    loadCategory(SPORTS_AND_RECREATION_GENRE_ID);
                    return true;
                case R.id.search_business:
                    loadCategory(BUSINESS_GENRE_ID);
                    return true;
                //Language items
                case R.id.search_fr:
                    loadByLanguage("fr");
                    return true;
                case R.id.search_es:
                    loadByLanguage("es");
                    return true;
                case R.id.search_us:
                    loadByLanguage("us");
                    return true;
                case R.id.search_ca:
                    loadByLanguage("ca");
                    return true;
                case R.id.search_ar:
                    loadByLanguage("ar");
                    return true;
                case R.id.search_de:
                    loadByLanguage("de");
                    return true;
                case R.id.search_nl:
                    loadByLanguage("nl");
                    return true;
                case R.id.search_pt:
                    loadByLanguage("pt");
                    return true;
                case R.id.search_it:
                    loadByLanguage("it");
                    return true;
                case R.id.search_ru:
                    loadByLanguage("ru");
                    return true;
                default:
                    return false;
            }
        } else {
            return true;
        }
    }

    public void loadToplist() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        subscription = Observable.create((Observable.OnSubscribe<List<Podcast>>) subscriber -> {
                    String lang = Locale.getDefault().getLanguage();
                    String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=25/explicit=true/json";
                    OkHttpClient client = AntennapodHttpClient.getHttpClient();
                    Request.Builder httpReq = new Request.Builder()
                            .url(url)
                            .header("User-Agent", ClientConfig.USER_AGENT);
                    List<Podcast> results = new ArrayList<>();
                    try {
                        Response response = client.newCall(httpReq.build()).execute();
                        if(!response.isSuccessful()) {
                            // toplist for language does not exist, fall back to united states
                            url = "https://itunes.apple.com/us/rss/toppodcasts/limit=25/explicit=true/json";
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

                            for(int i=0; i < entries.length(); i++) {
                                JSONObject json = entries.getJSONObject(i);
                                Podcast podcast = Podcast.fromToplist(json);
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
                    topList = podcasts;
                    updateData(topList);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> loadToplist());
                    butRetry.setVisibility(View.VISIBLE);
                });
    }

    public void search(String query, MenuItem item) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        subscription = rx.Observable.create((Observable.OnSubscribe<List<Podcast>>) subscriber -> {
                    String encodedQuery = null;
                    String formattedUrl = null;
                    try {
                        encodedQuery = URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // this won't ever be thrown
                    }
                    if (encodedQuery == null) {
                        encodedQuery = query; // failsafe
                    }

                    //If it is an artist search we use the API url for artist search
                    if(item.getTitle().equals("Artist")){
                        //Spaces in the query need to be replaced with '+' character.
                        formattedUrl = String.format(API_URL_ARTIST_SEARCH, query).replace(' ', '+');
                    }
                    else{
                        //Spaces in the query need to be replaced with '+' character.
                        formattedUrl = String.format(API_URL, query).replace(' ', '+');
                    }


                    OkHttpClient client = AntennapodHttpClient.getHttpClient();
                    Request.Builder httpReq = new Request.Builder()
                            .url(formattedUrl)
                            .header("User-Agent", ClientConfig.USER_AGENT);
                    List<Podcast> podcasts = new ArrayList<>();
                    try {
                        Response response = client.newCall(httpReq.build()).execute();

                        if(response.isSuccessful()) {
                            String resultString = response.body().string();
                            JSONObject result = new JSONObject(resultString);
                            JSONArray j = result.getJSONArray("results");

                            for (int i = 0; i < j.length(); i++) {
                                JSONObject podcastJson = j.getJSONObject(i);
                                Podcast podcast = Podcast.fromSearch(podcastJson);
                                podcasts.add(podcast);
                            }
                        }
                        else {
                            String prefix = getString(R.string.error_msg_prefix);
                            subscriber.onError(new IOException(prefix + response));
                        }
                    } catch (IOException | JSONException e) {
                        subscriber.onError(e);
                    }
                    subscriber.onNext(podcasts);
                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcasts -> {
                    progressBar.setVisibility(View.GONE);
                    updateData(podcasts);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> search(query, item));
                    butRetry.setVisibility(View.VISIBLE);
                });
    }

    //Load top 100 podcasts by the category corresponding to the genreId passed in
    public void loadCategory(int genreId){
        if (subscription != null) {
            subscription.unsubscribe();
        }
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        subscription = Observable.create((Observable.OnSubscribe<List<Podcast>>) subscriber -> {
            String lang = Locale.getDefault().getLanguage();
            String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=100/genre=" + genreId + "/json";
            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(url)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            List<Podcast> results = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();
                if(!response.isSuccessful()) {
                    // toplist for language does not exist, fall back to united states
                    url = "https://itunes.apple.com/us/rss/toppodcasts/limit=100/genre=" + genreId + "/json";
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

                    for(int i=0; i < entries.length(); i++) {
                        JSONObject json = entries.getJSONObject(i);
                        Podcast podcast = Podcast.fromToplist(json);
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

    //Load top 100 podcasts in the language passed in
    public void loadByLanguage(String lang){
        if (subscription != null) {
            subscription.unsubscribe();
        }
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        subscription = Observable.create((Observable.OnSubscribe<List<Podcast>>) subscriber -> {
            String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=100/json";
            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(url)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            List<Podcast> results = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();
                if(response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONObject feed = result.getJSONObject("feed");
                    JSONArray entries = feed.getJSONArray("entry");

                    for(int i=0; i < entries.length(); i++) {
                        JSONObject json = entries.getJSONObject(i);
                        Podcast podcast = Podcast.fromToplist(json);
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
                    languageSearchResults = podcasts;
                    updateData(languageSearchResults);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> loadToplist());
                    butRetry.setVisibility(View.VISIBLE);
                });

    }

    public List<Podcast> getTopList(){
        return this.topList;
    }

    public List<Podcast> getSearchResults(){
        return this.searchResults;
    }

    public List<Podcast> getCategorySearchResults(){
        return this.categorySearchResults;
    }

    public List<Podcast> getLanguageSearchResults(){
        return this.languageSearchResults;
    }
}
