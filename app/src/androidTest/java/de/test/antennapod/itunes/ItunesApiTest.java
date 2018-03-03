package de.test.antennapod.itunes;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.fragment.ItunesSearchFragment;
import de.danoeh.antennapod.fragment.TrendingFragment;

/**
 * Created by franc on 2018-02-26.
 */

public class ItunesApiTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private TrendingFragment trendingFragment;
    private ItunesSearchFragment itunesSearchFragment;
    private List<ItunesAdapter.Podcast> topList;
    private List<ItunesAdapter.Podcast> searchResults;
    private List<ItunesAdapter.Podcast> categorySearchResults;
    private List<ItunesAdapter.Podcast> languageSearchResults;

    MenuItem searchItem = new MenuItem() {
        public CharSequence title;

        @Override
        public int getItemId() {
            return 0;
        }

        @Override
        public int getGroupId() {
            return 0;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public MenuItem setTitle(CharSequence title) {
            this.title = title;
            return null;
        }

        @Override
        public MenuItem setTitle(int i) {
            return null;
        }

        @Override
        public CharSequence getTitle() {
            return this.title;
        }

        @Override
        public MenuItem setTitleCondensed(CharSequence charSequence) {
            return null;
        }

        @Override
        public CharSequence getTitleCondensed() {
            return null;
        }

        @Override
        public MenuItem setIcon(Drawable drawable) {
            return null;
        }

        @Override
        public MenuItem setIcon(int i) {
            return null;
        }

        @Override
        public Drawable getIcon() {
            return null;
        }

        @Override
        public MenuItem setIntent(Intent intent) {
            return null;
        }

        @Override
        public Intent getIntent() {
            return null;
        }

        @Override
        public MenuItem setShortcut(char c, char c1) {
            return null;
        }

        @Override
        public MenuItem setNumericShortcut(char c) {
            return null;
        }

        @Override
        public char getNumericShortcut() {
            return 0;
        }

        @Override
        public MenuItem setAlphabeticShortcut(char c) {
            return null;
        }

        @Override
        public char getAlphabeticShortcut() {
            return 0;
        }

        @Override
        public MenuItem setCheckable(boolean b) {
            return null;
        }

        @Override
        public boolean isCheckable() {
            return false;
        }

        @Override
        public MenuItem setChecked(boolean b) {
            return null;
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public MenuItem setVisible(boolean b) {
            return null;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public MenuItem setEnabled(boolean b) {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean hasSubMenu() {
            return false;
        }

        @Override
        public SubMenu getSubMenu() {
            return null;
        }

        @Override
        public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
            return null;
        }

        @Override
        public ContextMenu.ContextMenuInfo getMenuInfo() {
            return null;
        }

        @Override
        public void setShowAsAction(int i) {

        }

        @Override
        public MenuItem setShowAsActionFlags(int i) {
            return null;
        }

        @Override
        public MenuItem setActionView(View view) {
            return null;
        }

        @Override
        public MenuItem setActionView(int i) {
            return null;
        }

        @Override
        public View getActionView() {
            return null;
        }

        @Override
        public MenuItem setActionProvider(ActionProvider actionProvider) {
            return null;
        }

        @Override
        public ActionProvider getActionProvider() {
            return null;
        }

        @Override
        public boolean expandActionView() {
            return false;
        }

        @Override
        public boolean collapseActionView() {
            return false;
        }

        @Override
        public boolean isActionViewExpanded() {
            return false;
        }

        @Override
        public MenuItem setOnActionExpandListener(OnActionExpandListener onActionExpandListener) {
            return null;
        }
    };

    // Constructor
    public ItunesApiTest(){
        super("de.danoeh.antennapod.activity", MainActivity.class);
    }

    /**
     * Starts the Main activity
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        trendingFragment = new TrendingFragment();
        itunesSearchFragment = new ItunesSearchFragment();
        getActivity().getSupportFragmentManager().beginTransaction().add(trendingFragment, TrendingFragment.class.getSimpleName()).commit();
        getActivity().getSupportFragmentManager().beginTransaction().add(itunesSearchFragment, ItunesSearchFragment.class.getSimpleName()).commit();
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests if the loadTopList function returns a list of 25 podcasts
     * @throws InterruptedException
     */
    public void testLoadTopList() throws InterruptedException {
        //Request to itunes API
        trendingFragment.loadToplist();

        //Wait for request
        Thread.sleep(5000);

        topList = trendingFragment.getTopList();

        for(int i = 0; i < topList.size(); i++){
            System.out.println(topList.get(i).getPodcastInfo());
        }

        //Assertions
        assertNotNull(topList);
        assertEquals( 25, topList.size());
    }

    public void testItunesTitleSearch() throws InterruptedException {
        //Request to itunes API
        searchItem.setTitle("Title");
        String query = "Laura";
        itunesSearchFragment.search(query, searchItem);

        //Wait for request
        Thread.sleep(5000);

        searchResults = itunesSearchFragment.getSearchResults();

        //Assertions
        assertNotNull(searchResults);
        for(int i = 0; i < searchResults.size(); i++){
            //Assert that podcast object variable artist contains the query searched
            assertTrue(searchResults.get(i).title.contains(query));
        }
    }

    public void testItunesArtistSearch() throws InterruptedException {
        //Request to itunes API
        searchItem.setTitle("Artist");
        String query = "Laura";
        itunesSearchFragment.search(query, searchItem);

        //Wait for request
        Thread.sleep(5000);

        searchResults = itunesSearchFragment.getSearchResults();

        //Assertions
        assertNotNull(searchResults);
        for(int i = 0; i < searchResults.size(); i++){
            //Assert that podcast object variable artist contains the query searched
            assertTrue(searchResults.get(i).artist.contains(query));
        }
    }

    public void testItunesCategorySearch() throws InterruptedException {
        //Request to itunes API
        itunesSearchFragment.loadCategory(itunesSearchFragment.ARTS_GENRE_ID); //Sub-categories = Food, Literature, Design, Performing Arts, Visual Arts, Fashion & Beauty

        //Wait for request
        Thread.sleep(5000);

        categorySearchResults = itunesSearchFragment.getCategorySearchResults();

        //Assertions
        assertNotNull(categorySearchResults);
        assertEquals(100, categorySearchResults.size()); //limit = 100 but this does mean that 100 podcasts will be returned (For some categories there can be less than 100 podcasts returned. Assrertion works with Arts category as it contains at least 100)
        for(int i = 0; i < categorySearchResults.size(); i++){
            //Assert that podcast object variable category contains the category and sub-categories searched
            String category = categorySearchResults.get(i).category;
            assertTrue(category.contains("Arts") || category.contains("Food") || category.contains("Literature")
                    || category.contains("Design") || category.contains("Performing Arts") || category.contains("Visual Arts")
                    || category.contains("Fashion & Beauty"));
        }
    }

    public void testItunesLanguageSearch() throws InterruptedException {
        //Request to itunes API
        String lang = "fr"; //french
        itunesSearchFragment.loadByLanguage(lang);

        //Wait for request
        Thread.sleep(5000);

        languageSearchResults = itunesSearchFragment.getLanguageSearchResults();

        //Assertions
        assertNotNull(languageSearchResults);
        assertEquals(100, languageSearchResults.size()); //limit = 100
        for(int i = 0; i < languageSearchResults.size(); i++){
            //Assert that podcast object variable category contains the category and sub-categories searched
            String language = languageSearchResults.get(i).lang;
            assertTrue(language.contains(lang));
        }
    }

}
