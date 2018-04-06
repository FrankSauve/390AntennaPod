package de.danoeh.antennapod.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
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
import de.danoeh.antennapod.activity.MainActivity;
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

    //itunes api url to search podcasts by title name
    private static final String API_URL_TITLE_SEARCH = "https://itunes.apple.com/search?entity=podcast&attribute=titleTerm&term=%s";

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

    protected List<Integer> subgenreIds = new ArrayList<Integer>();

    //Arts subgenres
    public static final int FOOD_GENRE_ID = 1306;
    public static final int LITERATURE_GENRE_ID = 1401;
    public static final int DESIGN_GENRE_ID = 1402;
    public static final int PERFORMING_ARTS_GENRE_ID = 1405;
    public static final int VISUAL_ARTS_GENRE_ID = 1406;
    public static final int FASHION_AND_BEAUTY_GENRE_ID = 1459;

    //Education subgenres
    public static final int K12_GENRE_ID = 1415;
    public static final int HIGHER_EDUCATION_GENRE_ID = 1416;
    public static final int EDUCATIONAL_TECHNOLOGY_GENRE_ID = 1468;
    public static final int LANGUAGE_COURSES_GENRE_ID = 1469;
    public static final int TRAINING_GENRE_ID = 1470;

    //Health subgenres
    public static final int FITNESS_AND_NUTRITION_GENRE_ID = 1417;
    public static final int SELFHELP_GENRE_ID = 1420;
    public static final int SEXUALITY_GENRE_ID = 1421;
    public static final int ALTERNATIVE_HEALTH_GENRE_ID = 1481;

    //Religion & spirituality subgenres
    public static final int BUDDHISM_GENRE_ID = 1438;
    public static final int CHRISTIANITY_GENRE_ID = 1439;
    public static final int ISLAM_GENRE_ID = 1440;
    public static final int JUDAISM_GENRE_ID = 1441;
    public static final int SPIRITUALITY_GENRE_ID = 1444;
    public static final int HINDUISM_GENRE_ID = 1463;
    public static final int OTHER_GENRE_ID = 1464;

    //Science & medicine subgenres
    public static final int NATURAL_SCIENCES_GENRE_ID = 1477;
    public static final int MEDECINE_GENRE_ID = 1478;
    public static final int SOCIAL_SCIENCES_GENRE_ID = 1479;

    //Sports & recreation subgenres
    public static final int OUTDOOR_GENRE_ID = 1456;
    public static final int PROFESSIONAL_GENRE_ID = 1465;
    public static final int COLLEGE_AND_HIGHSCHOOL_GENRE_ID = 1466;
    public static final int AMATEUR_GENRE_ID = 1467;

    //Technology subgenres
    public static final int GADGETS_GENRE_ID = 1446;
    public static final int TECHNEWS_GENRE_ID = 1448;
    public static final int PODCASTING_GENRE_ID = 1450;
    public static final int SOFTWARE_HOW_TO_GENRE_ID = 1480;

    //Business subgenres
    public static final int CAREERS_GENRE_ID = 1410;
    public static final int INVESTING_GENRE_ID = 1412;
    public static final int MANAGEMENT_AND_MARKETING_GENRE_ID = 1413;
    public static final int BUSINESS_NEWS_GENRE_ID = 1471;
    public static final int SHOPPING_GENRE_ID = 1472;

    //Games & hobbies subgenres
    public static final int VIDEOGAMES_GENRE_ID = 1404;
    public static final int AUTOMOTIVE_GENRE_ID = 1454;
    public static final int AVIATION_GENRE_ID = 1455;
    public static final int HOBBIES_GENRE_ID = 1460;
    public static final int OTHER_GAMES_GENRE_ID = 1461;

    //Society & culture subgenres
    public static final int PERSONAL_JOURNALS_GENRE_ID = 1302;
    public static final int PLACES_AND_TRAVEL_GENRE_ID = 1320;
    public static final int PHILOSOPHY_GENRE_ID = 1443;
    public static final int HISTORY_GENRE_ID = 1462;

    //Government & organizations subgenres
    public static final int NATIONAL_GENRE_ID = 1473;
    public static final int REGIONAL_GENRE_ID = 1474;
    public static final int LOCAL_GENRE_ID = 1475;
    public static final int NON_PROFIT_GENRE_ID = 1476;


    /**
     * Adapter responsible with the search results
     */
    protected ItunesAdapter adapter;
    protected GridView gridView;
    protected ProgressBar progressBar;
    protected TextView txtvError;
    protected Button butRetry;
    protected TextView txtvEmpty;

    /**
     * List of podcasts retreived from the search
     */
    protected List<Podcast> searchResults;
    protected List<Podcast> categorySearchResults;
    private List<Podcast> subCategorySearchResults;
    private List<Podcast> languageSearchResults;
    private List<Podcast> topList;
    protected Subscription subscription;
    private SearchView sv;
    private SearchView svSelect;
    private JSONArray autocompleteSuggestions;
    private Menu menu;
    private boolean isInDiscoveryTab = false;

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
     * Append adapter data with provided search results from SearchTask.
     * @param result List of Podcast objects containing search results
     */
    void appendData(List<Podcast> result) {
        if(this.searchResults != null){
            this.searchResults.addAll(result);
        }
        else{
            this.searchResults = result;
        }

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
        this.menu = menu;
        MenuItem searchItem = menu.findItem(R.id.action_search);
        sv= (SearchView) MenuItemCompat.getActionView(searchItem);
        MenuItemUtils.adjustTextColor(getActivity(), sv);
        sv.setQueryHint(getString(R.string.search_itunes_label));

        sv.setSuggestionsAdapter(new SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_1, null, new String[] {SearchManager.SUGGEST_COLUMN_TEXT_1}, new int[] {android.R.id.text1}));

        sv.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {

                Cursor cursor = (Cursor) sv.getSuggestionsAdapter().getItem(position);
                String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                cursor.close();
                sv.setQuery(term, true);
                search(term, searchItem);

                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {

                return onSuggestionSelect(position);
            }
        });

        sv.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                sv.clearFocus();
                search(query, searchItem);
                sv.getSuggestionsAdapter().changeCursor(null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                new AutocompleAsyncTask().execute(query, Integer.toString(searchItem.getItemId()));
                return true;
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

    public boolean onOptionsItemSelected(MenuItem item)  {
        if (!super.onOptionsItemSelected(item)) {

            subgenreIds.clear();

            svSelect = (SearchView) MenuItemCompat.getActionView(item);

            //Collapses the artist, title and regular searchviews so they don't overlap
            menu.findItem(R.id.itunes_search_artist).collapseActionView();
            menu.findItem(R.id.itunes_search_title).collapseActionView();

            switch (item.getItemId()) {
                //Artist item
                case R.id.itunes_search_artist:
                    MenuItemUtils.adjustTextColor(getActivity(), svSelect);
                    svSelect.setQueryHint(getString(R.string.artist_search));
                    svSelect.setSuggestionsAdapter(new SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_1, null, new String[] {SearchManager.SUGGEST_COLUMN_TEXT_1}, new int[] {android.R.id.text1}));

                    svSelect.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

                        @Override
                        public boolean onSuggestionSelect(int position) {

                            Cursor cursor = (Cursor) sv.getSuggestionsAdapter().getItem(position);
                            String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                            cursor.close();
                            svSelect.setQuery(term, true);
                            search(term, item);

                            return true;
                        }

                        @Override
                        public boolean onSuggestionClick(int position) {

                            return onSuggestionSelect(position);
                        }
                    });

                    svSelect.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            svSelect.clearFocus();
                            search(query, item);
                            svSelect.getSuggestionsAdapter().changeCursor(null);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String query) {
                            new AutocompleAsyncTask().execute(query, Integer.toString(item.getItemId()));
                            return true;
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
                //Title item
                case R.id.itunes_search_title:

                    MenuItemUtils.adjustTextColor(getActivity(), svSelect);
                    svSelect.setQueryHint(getString(R.string.title_search));
                    svSelect.setSuggestionsAdapter(new SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_1, null, new String[] {SearchManager.SUGGEST_COLUMN_TEXT_1}, new int[] {android.R.id.text1}));

                    svSelect.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

                        @Override
                        public boolean onSuggestionSelect(int position) {

                            Cursor cursor = (Cursor) sv.getSuggestionsAdapter().getItem(position);
                            String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                            cursor.close();
                            svSelect.setQuery(term, true);
                            search(term, item);

                            return true;
                        }

                        @Override
                        public boolean onSuggestionClick(int position) {

                            return onSuggestionSelect(position);
                        }
                    });

                    svSelect.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            svSelect.clearFocus();
                            search(query, item);
                            svSelect.getSuggestionsAdapter().changeCursor(null);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String query) {
                            new AutocompleAsyncTask().execute(query, Integer.toString(item.getItemId()));
                            return true;
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
                //Category items
                case R.id.search_arts:
                    ((MainActivity)getActivity()).setActionBarTitle("Arts");
                    subgenreIds.add(FOOD_GENRE_ID);
                    subgenreIds.add(LITERATURE_GENRE_ID);
                    subgenreIds.add(DESIGN_GENRE_ID);
                    subgenreIds.add(PERFORMING_ARTS_GENRE_ID);
                    subgenreIds.add(VISUAL_ARTS_GENRE_ID);
                    subgenreIds.add(FASHION_AND_BEAUTY_GENRE_ID);
                    ((MainActivity)getActivity()).setActionBarTitle("Arts");
                    loadCategory(ARTS_GENRE_ID);
                    return true;
                case R.id.search_food:
                    ((MainActivity)getActivity()).setActionBarTitle("Food");
                    loadCategory(FOOD_GENRE_ID);
                    return true;
                case R.id.search_literature:
                    ((MainActivity)getActivity()).setActionBarTitle("Literature");
                    loadCategory(LITERATURE_GENRE_ID);
                    return true;
                case R.id.search_design:
                    ((MainActivity)getActivity()).setActionBarTitle("Design");
                    loadCategory(DESIGN_GENRE_ID);
                    return true;
                case R.id.search_performing_arts:
                    ((MainActivity)getActivity()).setActionBarTitle("Performing Arts");
                    loadCategory(PERFORMING_ARTS_GENRE_ID);
                    return true;
                case R.id.search_visual_arts:
                    ((MainActivity)getActivity()).setActionBarTitle("Visual Arts");
                    loadCategory(VISUAL_ARTS_GENRE_ID);
                    return true;
                case R.id.search_fashion_and_beauty:
                    ((MainActivity)getActivity()).setActionBarTitle("Fashion & Beauty");
                    loadCategory(FASHION_AND_BEAUTY_GENRE_ID);
                    return true;

                case R.id.search_comedy:
                    ((MainActivity)getActivity()).setActionBarTitle("Comedy");
                    loadCategory(COMEDY_GENRE_ID);
                    return true;

                case R.id.search_news_politics:
                    ((MainActivity)getActivity()).setActionBarTitle("News & Politics");
                    loadCategory(NEWS_AND_POLITICS_GENRE_ID);
                    return true;

                case R.id.search_kids_family:
                    ((MainActivity)getActivity()).setActionBarTitle("Kids & Family");
                    loadCategory(KIDS_AND_FAMILY_GENRE_ID);
                    return true;

                case R.id.search_games_hobbies:
                    ((MainActivity)getActivity()).setActionBarTitle("Games & Hobbies");
                    subgenreIds.add(VIDEOGAMES_GENRE_ID);
                    subgenreIds.add(AUTOMOTIVE_GENRE_ID);
                    subgenreIds.add(AVIATION_GENRE_ID);
                    subgenreIds.add(HOBBIES_GENRE_ID);
                    subgenreIds.add(OTHER_GAMES_GENRE_ID);
                    loadCategory(GAMES_AND_HOBBIES_GENRE_ID);
                    return true;
                case R.id.search_videogames:
                    ((MainActivity)getActivity()).setActionBarTitle("Videogames");
                    loadCategory(VIDEOGAMES_GENRE_ID);
                    return true;
                case R.id.search_automotive:
                    ((MainActivity)getActivity()).setActionBarTitle("Automotive");
                    loadCategory(AUTOMOTIVE_GENRE_ID);
                    return true;
                case R.id.search_aviation:
                    ((MainActivity)getActivity()).setActionBarTitle("Aviation");
                    loadCategory(AVIATION_GENRE_ID);
                    return true;
                case R.id.search_hobbies:
                    ((MainActivity)getActivity()).setActionBarTitle("Hobbies");
                    loadCategory(HOBBIES_GENRE_ID);
                    return true;
                case R.id.search_other_games:
                    ((MainActivity)getActivity()).setActionBarTitle("Other Games");
                    loadCategory(OTHER_GAMES_GENRE_ID);
                    return true;

                case R.id.search_government_organization:
                    ((MainActivity)getActivity()).setActionBarTitle("Governments & Organizations");
                    subgenreIds.add(NATIONAL_GENRE_ID);
                    subgenreIds.add(REGIONAL_GENRE_ID);
                    subgenreIds.add(LOCAL_GENRE_ID);
                    subgenreIds.add(NON_PROFIT_GENRE_ID);
                    loadCategory(GOVERNMENT_AND_ORGANIZATION_GENRE_ID);
                    return true;
                case R.id.search_national:
                    ((MainActivity)getActivity()).setActionBarTitle("National");
                    loadCategory(NATIONAL_GENRE_ID);
                    return true;
                case R.id.search_regional:
                    ((MainActivity)getActivity()).setActionBarTitle("Regional");
                    loadCategory(REGIONAL_GENRE_ID);
                    return true;
                case R.id.search_local:
                    ((MainActivity)getActivity()).setActionBarTitle("Local");
                    loadCategory(LOCAL_GENRE_ID);
                    return true;
                case R.id.search_non_profit:
                    ((MainActivity)getActivity()).setActionBarTitle("Non-Profit");
                    loadCategory(NON_PROFIT_GENRE_ID);
                    return true;

                case R.id.search_technology:
                    ((MainActivity)getActivity()).setActionBarTitle("Technology");
                    subgenreIds.add(GADGETS_GENRE_ID);
                    subgenreIds.add(TECHNEWS_GENRE_ID);
                    subgenreIds.add(PODCASTING_GENRE_ID);
                    subgenreIds.add(SOFTWARE_HOW_TO_GENRE_ID);
                    loadCategory(TECHNOLOGY_GENRE_ID);
                    return true;
                case R.id.search_gadgets:
                    ((MainActivity)getActivity()).setActionBarTitle("Gadgets");
                    loadCategory(GADGETS_GENRE_ID);
                    return true;
                case R.id.search_technews:
                    ((MainActivity)getActivity()).setActionBarTitle("Tech News");
                    loadCategory(TECHNEWS_GENRE_ID);
                    return true;
                case R.id.search_podcasting:
                    ((MainActivity)getActivity()).setActionBarTitle("Podcasting");
                    loadCategory(PODCASTING_GENRE_ID);
                    return true;
                case R.id.search_software_how_to:
                    ((MainActivity)getActivity()).setActionBarTitle("Software How-To");
                    loadCategory(SOFTWARE_HOW_TO_GENRE_ID);
                    return true;

                case R.id.search_tv_film:
                    ((MainActivity)getActivity()).setActionBarTitle("TV & Film");
                    loadCategory(TV_AND_FILM_GENRE_ID);
                    return true;

                case R.id.search_education:
                    ((MainActivity)getActivity()).setActionBarTitle("Education");
                    subgenreIds.add(K12_GENRE_ID);
                    subgenreIds.add(HIGHER_EDUCATION_GENRE_ID);
                    subgenreIds.add(EDUCATIONAL_TECHNOLOGY_GENRE_ID);
                    subgenreIds.add(LANGUAGE_COURSES_GENRE_ID);
                    subgenreIds.add(TRAINING_GENRE_ID);
                    loadCategory(EDUCATION_GENRE_ID);
                    return true;
                case R.id.search_k12:
                    ((MainActivity)getActivity()).setActionBarTitle("K-12");
                    loadCategory(K12_GENRE_ID);
                    return true;
                case R.id.search_higher_education:
                    ((MainActivity)getActivity()).setActionBarTitle("Higher Education");
                    loadCategory(HIGHER_EDUCATION_GENRE_ID);
                    return true;
                case R.id.search_educational_technology:
                    ((MainActivity)getActivity()).setActionBarTitle("Educational Technology");
                    loadCategory(EDUCATIONAL_TECHNOLOGY_GENRE_ID);
                    return true;
                case R.id.search_language_courses:
                    ((MainActivity)getActivity()).setActionBarTitle("Language Courses");
                    loadCategory(LANGUAGE_COURSES_GENRE_ID);
                    return true;
                case R.id.search_training:
                    ((MainActivity)getActivity()).setActionBarTitle("Training");
                    loadCategory(TRAINING_GENRE_ID);
                    return true;

                case R.id.search_health:
                    ((MainActivity)getActivity()).setActionBarTitle("Health");
                    subgenreIds.add(FITNESS_AND_NUTRITION_GENRE_ID);
                    subgenreIds.add(SELFHELP_GENRE_ID);
                    subgenreIds.add(SEXUALITY_GENRE_ID);
                    subgenreIds.add(ALTERNATIVE_HEALTH_GENRE_ID);
                    loadCategory(HEALTH_GENRE_ID);
                    return true;
                case R.id.search_fitness_and_nutrition:
                    ((MainActivity)getActivity()).setActionBarTitle("Fitness & Nutrition");
                    loadCategory(FITNESS_AND_NUTRITION_GENRE_ID);
                    return true;
                case R.id.search_selfhelp:
                    ((MainActivity)getActivity()).setActionBarTitle("Self-Help");
                    loadCategory(SELFHELP_GENRE_ID);
                    return true;
                case R.id.search_sexuality:
                    ((MainActivity)getActivity()).setActionBarTitle("Sexuality");
                    loadCategory(SEXUALITY_GENRE_ID);
                    return true;
                case R.id.search_alternative_health:
                    ((MainActivity)getActivity()).setActionBarTitle("Alternative health");
                    loadCategory(ALTERNATIVE_HEALTH_GENRE_ID);
                    return true;

                case R.id.search_science_medecine:
                    ((MainActivity)getActivity()).setActionBarTitle("Science & Medicine");
                    subgenreIds.add(NATURAL_SCIENCES_GENRE_ID);
                    subgenreIds.add(MEDECINE_GENRE_ID);
                    subgenreIds.add(SOCIAL_SCIENCES_GENRE_ID);
                    loadCategory(SCIENCE_AND_MEDECINE_GENRE_ID);
                    return true;
                case R.id.search_natural_sciences:
                    ((MainActivity)getActivity()).setActionBarTitle("Natural Sciences");
                    loadCategory(NATURAL_SCIENCES_GENRE_ID);
                    return true;
                case R.id.search_medicine:
                    ((MainActivity)getActivity()).setActionBarTitle("Medicine");
                    loadCategory(MEDECINE_GENRE_ID);
                    return true;
                case R.id.search_social_sciences:
                    ((MainActivity)getActivity()).setActionBarTitle("Social Sciences");
                    loadCategory(SOCIAL_SCIENCES_GENRE_ID);
                    return true;

                case R.id.search_society_culture:
                    ((MainActivity)getActivity()).setActionBarTitle("Society & Culture");
                    subgenreIds.add(PERSONAL_JOURNALS_GENRE_ID);
                    subgenreIds.add(PLACES_AND_TRAVEL_GENRE_ID);
                    subgenreIds.add(PHILOSOPHY_GENRE_ID);
                    subgenreIds.add(HISTORY_GENRE_ID);
                    loadCategory(SOCIETY_AND_CULTURE_GENRE_ID);
                    return true;
                case R.id.search_personal_journals:
                    ((MainActivity)getActivity()).setActionBarTitle("Personal Journals");
                    loadCategory(PERSONAL_JOURNALS_GENRE_ID);
                    return true;
                case R.id.search_places_and_travel:
                    ((MainActivity)getActivity()).setActionBarTitle("Places & Travel");
                    loadCategory(PLACES_AND_TRAVEL_GENRE_ID);
                    return true;
                case R.id.search_philosophy:
                    ((MainActivity)getActivity()).setActionBarTitle("Philosophy");
                    loadCategory(PHILOSOPHY_GENRE_ID);
                    return true;
                case R.id.search_history:
                    ((MainActivity)getActivity()).setActionBarTitle("History");
                    loadCategory(HISTORY_GENRE_ID);
                    return true;

                case R.id.search_music:
                    ((MainActivity)getActivity()).setActionBarTitle("Music");
                    loadCategory(MUSIC_GENRE_ID);
                    return true;

                case R.id.search_religion_spirituality:
                    ((MainActivity)getActivity()).setActionBarTitle("Religion & Spirituality");
                    subgenreIds.add(BUDDHISM_GENRE_ID);
                    subgenreIds.add(CHRISTIANITY_GENRE_ID);
                    subgenreIds.add(ISLAM_GENRE_ID);
                    subgenreIds.add(JUDAISM_GENRE_ID);
                    subgenreIds.add(SPIRITUALITY_GENRE_ID);
                    subgenreIds.add(HINDUISM_GENRE_ID);
                    subgenreIds.add(OTHER_GENRE_ID);
                    loadCategory(RELIGION_AND_SPIRITUALITY_GENRE_ID);
                    return true;
                case R.id.search_buddhism:
                    ((MainActivity)getActivity()).setActionBarTitle("Buddhism");
                    loadCategory(BUDDHISM_GENRE_ID);
                    return true;
                case R.id.search_christianity:
                    ((MainActivity)getActivity()).setActionBarTitle("Christianity");
                    loadCategory(CHRISTIANITY_GENRE_ID);
                    return true;
                case R.id.search_islam:
                    ((MainActivity)getActivity()).setActionBarTitle("Islam");
                    loadCategory(ISLAM_GENRE_ID);
                    return true;
                case R.id.search_judaism:
                    ((MainActivity)getActivity()).setActionBarTitle("Judaism");
                    loadCategory(JUDAISM_GENRE_ID);
                    return true;
                case R.id.search_spirituality:
                    ((MainActivity)getActivity()).setActionBarTitle("Spirituality");
                    loadCategory(SPIRITUALITY_GENRE_ID);
                    return true;
                case R.id.search_hinduism:
                    ((MainActivity)getActivity()).setActionBarTitle("Hinduism");
                    loadCategory(HINDUISM_GENRE_ID);
                    return true;
                case R.id.search_other:
                    ((MainActivity)getActivity()).setActionBarTitle("Other Religions & Spirituality");
                    loadCategory(OTHER_GENRE_ID);
                    return true;

                case R.id.search_sports_recreation:
                    ((MainActivity)getActivity()).setActionBarTitle("Sports & Recreation");
                    subgenreIds.add(OUTDOOR_GENRE_ID);
                    subgenreIds.add(PROFESSIONAL_GENRE_ID);
                    subgenreIds.add(COLLEGE_AND_HIGHSCHOOL_GENRE_ID);
                    subgenreIds.add(AMATEUR_GENRE_ID);
                    loadCategory(SPORTS_AND_RECREATION_GENRE_ID);
                    return true;
                case R.id.search_outdoor:
                    ((MainActivity)getActivity()).setActionBarTitle("Outdoor");
                    loadCategory(OUTDOOR_GENRE_ID);
                    return true;
                case R.id.search_professional:
                    ((MainActivity)getActivity()).setActionBarTitle("Professional");
                    loadCategory(PROFESSIONAL_GENRE_ID);
                    return true;
                case R.id.search_college_and_highschool:
                    ((MainActivity)getActivity()).setActionBarTitle("College & Highschool");
                    loadCategory(COLLEGE_AND_HIGHSCHOOL_GENRE_ID);
                    return true;
                case R.id.search_amateur:
                    ((MainActivity)getActivity()).setActionBarTitle("Amateur");
                    loadCategory(AMATEUR_GENRE_ID);
                    return true;

                case R.id.search_business:
                    ((MainActivity)getActivity()).setActionBarTitle("Business");
                    subgenreIds.add(CAREERS_GENRE_ID);
                    subgenreIds.add(INVESTING_GENRE_ID);
                    subgenreIds.add(MANAGEMENT_AND_MARKETING_GENRE_ID);
                    subgenreIds.add(BUSINESS_NEWS_GENRE_ID);
                    subgenreIds.add(SHOPPING_GENRE_ID);
                    loadCategory(BUSINESS_GENRE_ID);
                    return true;
                case R.id.search_careers:
                    ((MainActivity)getActivity()).setActionBarTitle("Careers");
                    loadCategory(CAREERS_GENRE_ID);
                    return true;
                case R.id.search_investing:
                    ((MainActivity)getActivity()).setActionBarTitle("Investing");
                    loadCategory(INVESTING_GENRE_ID);
                    return true;
                case R.id.search_management_and_marketing:
                    ((MainActivity)getActivity()).setActionBarTitle("Management & Marketing");
                    loadCategory(MANAGEMENT_AND_MARKETING_GENRE_ID);
                    return true;
                case R.id.search_business_news:
                    ((MainActivity)getActivity()).setActionBarTitle("Business News");
                    loadCategory(BUSINESS_NEWS_GENRE_ID);
                    return true;
                case R.id.search_shopping:
                    ((MainActivity)getActivity()).setActionBarTitle("Shopping");
                    loadCategory(SHOPPING_GENRE_ID);
                    return true;


                //Language items
                case R.id.search_fr:
                    ((MainActivity)getActivity()).setActionBarTitle("French Podcasts");
                    loadByLanguage("fr");
                    return true;
                case R.id.search_es:
                    ((MainActivity)getActivity()).setActionBarTitle("Spanish Podcasts");
                    loadByLanguage("es");
                    return true;
                case R.id.search_us:
                    ((MainActivity)getActivity()).setActionBarTitle("USA Podcasts");
                    loadByLanguage("us");
                    return true;
                case R.id.search_ca:
                    ((MainActivity)getActivity()).setActionBarTitle("Canadian Podcasts");
                    loadByLanguage("ca");
                    return true;
                case R.id.search_ar:
                    ((MainActivity)getActivity()).setActionBarTitle("Arabic Podcasts");
                    loadByLanguage("ar");
                    return true;
                case R.id.search_de:
                    ((MainActivity)getActivity()).setActionBarTitle("German Podcasts");
                    loadByLanguage("de");
                    return true;
                case R.id.search_nl:
                    ((MainActivity)getActivity()).setActionBarTitle("Netherlands Podcasts");
                    loadByLanguage("nl");
                    return true;
                case R.id.search_pt:
                    ((MainActivity)getActivity()).setActionBarTitle("Portuguese Podcasts");
                    loadByLanguage("pt");
                    return true;
                case R.id.search_it:
                    ((MainActivity)getActivity()).setActionBarTitle("Italian Podcasts");
                    loadByLanguage("it");
                    return true;
                case R.id.search_ru:
                    ((MainActivity)getActivity()).setActionBarTitle("Russian Podcasts");
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

    //query has to be lowercase to only get podcast objects in the search
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
                        encodedQuery = query.toLowerCase(); // failsafe
                    }

                    //If it is an artist search we use the API url for artist search
                    if(item.getTitle().equals("Artist")){
                        //Spaces in the query need to be replaced with '+' character.
                        //query has to be lowercase to only get podcasts objects
                        formattedUrl = String.format(API_URL_ARTIST_SEARCH, query.toLowerCase()).replace(' ', '+');
                    }
                    //If it is a title search we use the API url for title search
                    else if(item.getTitle().equals("Title")){
                        //Spaces in the query need to be replaced with '+' character.
                        formattedUrl = String.format(API_URL_TITLE_SEARCH, query.toLowerCase()).replace(' ', '+');
                    }
                    //Else use standard API search url
                    else{
                        formattedUrl = String.format(API_URL, query.toLowerCase()).replace(' ', '+');
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
                    //Append data if in discovery tab
                    if(isInDiscoveryTab){
                        appendData(podcasts);
                    }
                    else{
                        updateData(podcasts);
                    }
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
    public void loadCategory(int genreId) {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if(subgenreIds.isEmpty()){
            subscription = Observable.create((Observable.OnSubscribe<List<Podcast>>) subscriber -> {
                String lang = Locale.getDefault().getLanguage();
                String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=25/genre=" + genreId + "/json";
                OkHttpClient client = AntennapodHttpClient.getHttpClient();
                Request.Builder httpReq = new Request.Builder()
                        .url(url)
                        .header("User-Agent", ClientConfig.USER_AGENT);
                List<Podcast> results = new ArrayList<>();
                try {
                    Response response = client.newCall(httpReq.build()).execute();
                    if(!response.isSuccessful()) {
                        // toplist for language does not exist, fall back to united states
                        url = "https://itunes.apple.com/us/rss/toppodcasts/limit=25/genre=" + genreId + "/json";
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
        else{
            for(int i = 0; i < subgenreIds.size(); i++){
                //If first subcategory, clear the search results, else append the results
                if(i == 0) {
                    if(this.searchResults != null){
                        this.searchResults.clear();
                    }
                    adapter.clear();
                }
                loadSubCategories(subgenreIds.get(i));
            }

        }
    }

    public void loadSubCategories(int subgenreId){

        subscription = Observable.create((Observable.OnSubscribe<List<Podcast>>) subscriber -> {
            String lang = Locale.getDefault().getLanguage();
            String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=10/genre=" + subgenreId + "/json";
            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(url)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            List<Podcast> results = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();
                if (!response.isSuccessful()) {
                    // toplist for language does not exist, fall back to united states
                    url = "https://itunes.apple.com/us/rss/toppodcasts/limit=10/genre=" + subgenreId + "/json";
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

                    for (int i = 0; i < entries.length(); i++) {
                        JSONObject json = entries.getJSONObject(i);
                        Podcast podcast = Podcast.fromToplist(json);
                        results.add(podcast);
                    }

                } else {
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
                    subCategorySearchResults = podcasts;
                    appendData(subCategorySearchResults);
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

    public void autocomplete(String query, String itemId){
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = Observable.create((Observable.OnSubscribe<JSONArray>) subscriber -> {

            String encodedQuery = null;
            String formattedUrl = null;
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // this won't ever be thrown
            }
            if (encodedQuery == null) {
                encodedQuery = query.toLowerCase(); // failsafe
            }

            //If it is an artist search we use the API url for artist search
            if(Integer.parseInt(itemId) == R.id.itunes_search_artist){
                //Spaces in the query need to be replaced with '+' character.
                //query has to be lowercase to only get podcasts objects
                formattedUrl = String.format(API_URL_ARTIST_SEARCH, query.toLowerCase()).replace(' ', '+');
            }
            //If it is a title search we use the API url for title search
            else if(Integer.parseInt(itemId) == R.id.itunes_search_title){
                //Spaces in the query need to be replaced with '+' character.
                formattedUrl = String.format(API_URL_TITLE_SEARCH, query.toLowerCase()).replace(' ', '+');
            }
            //Else use standard API search url
            else{
                formattedUrl = String.format(API_URL, query.toLowerCase()).replace(' ', '+');
            }

            JSONArray j = null;

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
                    j = result.getJSONArray("results");

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
            subscriber.onNext(j);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(j -> {
                   autocompleteSuggestions = j;
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));

                });
    }


    class AutocompleAsyncTask extends AsyncTask<String, Void, Cursor>{

        private final String[] sAutocompleteColNames = new String[] {
                BaseColumns._ID,                         // necessary for adapter
                SearchManager.SUGGEST_COLUMN_TEXT_1      // the full search term
        };

        @Override
        protected Cursor doInBackground(String... params) {

            MatrixCursor cursor = new MatrixCursor(sAutocompleteColNames);

            // get your search terms from the server here, ex:
            System.out.print(params[0]);
            // parse your search terms into the MatrixCursor
            if(!params[0].equals("")){

                autocomplete(params[0], params[1]);
                try {
                    Thread.sleep(500); //TODO : This is a bad way of waiting for a request
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONArray podcasts = autocompleteSuggestions;
                for (int i = 0; i < podcasts.length(); i++) {
                    JSONObject podcastJson = null;
                    Podcast podcast = null;
                    try {
                        podcastJson = podcasts.getJSONObject(i);
                        podcast = Podcast.fromSearch(podcastJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(Integer.parseInt(params[1]) == R.id.itunes_search_artist){
                        Object[] row = new Object[] { i, podcast.artist };
                        cursor.addRow(row);
                    }
                    else{
                        Object[] row = new Object[] { i, podcast.title };
                        cursor.addRow(row);
                    }
                }
            }
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor result){
            sv.getSuggestionsAdapter().changeCursor(result);
            svSelect.getSuggestionsAdapter().changeCursor(result);
        }
    }


    /**
     * Getters and setters for testing purposes
     */

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

    public JSONArray getAutocompleteResults(){ return this.autocompleteSuggestions; }

    public Menu getMenu() {return this.menu;}

    public void setSubgenreIds(List<Integer> subgenreIds){
        this.subgenreIds = subgenreIds;
    }

    public void setIsInDisvoryTab(boolean b){
        this.isInDiscoveryTab = b;
    }

}
