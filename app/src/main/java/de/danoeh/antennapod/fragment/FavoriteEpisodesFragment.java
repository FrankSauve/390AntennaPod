package de.danoeh.antennapod.fragment;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.AllEpisodesRecycleAdapter;
import de.danoeh.antennapod.core.event.FavoritesEvent;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.service.download.DownloadService;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBTasks;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.DownloadRequester;
import de.danoeh.antennapod.dialog.EpisodesApplyActionFragment;
import de.danoeh.antennapod.dialog.FavoritesApplyActionFragment;
import de.danoeh.antennapod.menuhandler.MenuItemUtils;


/**
 * Like 'EpisodesFragment' except that it only shows favorite episodes and
 * supports swiping to remove from favorites.
 */

public class FavoriteEpisodesFragment extends AllEpisodesFragment {

    public static final String TAG = "FavoriteEpisodesFrag";

    private static final String PREF_NAME = "PrefFavoriteEpisodesFragment";

    private boolean isUpdatingFeeds;

    @Override
    protected boolean showOnlyNewEpisodes() { return true; }

    @Override
    protected String getPrefName() { return PREF_NAME; }

    public void onEvent(FavoritesEvent event) {
        Log.d(TAG, "onEvent() called with: " + "event = [" + event + "]");
        loadItems();
    }

    @Override
    protected void resetViewState() {
        super.resetViewState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateViewHelper(inflater, container, savedInstanceState,
                R.layout.all_episodes_fragment);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                AllEpisodesRecycleAdapter.Holder holder = (AllEpisodesRecycleAdapter.Holder)viewHolder;
                Log.d(TAG, "remove(" + holder.getItemId() + ")");

                if (subscription != null) {
                    subscription.unsubscribe();
                }
                FeedItem item = holder.getFeedItem();
                if (item != null) {
                    DBWriter.removeFavoriteItem(item);

                    Snackbar snackbar = Snackbar.make(root, getString(R.string.removed_item),
                            Snackbar.LENGTH_LONG);
                    snackbar.setAction(getString(R.string.undo), v -> DBWriter.addFavoriteItem(item));
                    snackbar.show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return root;
    }

    protected final MenuItemUtils.UpdateRefreshMenuItemChecker updateRefreshMenuItemChecker =
            () -> DownloadService.isRunning && DownloadRequester.getInstance().isDownloadingFeeds();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!isAdded()) {
            return;
        }
        //super.onCreateOptionsMenu(menu, inflater);
        if (itemsLoaded) {
            inflater.inflate(R.menu.favorites, menu);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            MenuItem episodeActionsFavorites = menu.findItem(R.id.favorites_actions);

            final SearchView sv = (SearchView) MenuItemCompat.getActionView(searchItem);
            MenuItemUtils.adjustTextColor(getActivity(), sv);
            sv.setQueryHint(getString(R.string.search_hint));
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    sv.clearFocus();
                    ((MainActivity) getActivity()).loadChildFragment(SearchFragment.newInstance(s));
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

            if(episodes.size() > 0) {
                int[] attrs = {R.attr.action_bar_icon_color};
                TypedArray ta = getActivity().obtainStyledAttributes(UserPreferences.getTheme(), attrs);
                int textColor = ta.getColor(0, Color.GRAY);
                ta.recycle();
                episodeActionsFavorites.setIcon(new IconDrawable(getActivity(),
                        FontAwesomeIcons.fa_gears).color(textColor).actionBarSize());
                episodeActionsFavorites.setVisible(true);
            } else {
                episodeActionsFavorites.setVisible(false);
            }

            isUpdatingFeeds = MenuItemUtils.updateRefreshMenuItem(menu, R.id.refresh_item, updateRefreshMenuItemChecker);

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                case R.id.favorites_actions:
                    FavoritesApplyActionFragment fragment = FavoritesApplyActionFragment
                            .newInstance(episodes, FavoritesApplyActionFragment.ACTION_ALL);
                    ((MainActivity) getActivity()).loadChildFragment(fragment);
                    return true;
                default:
                    return false;
            }
        } else {
            return true;
        }

    }



    @Override
    protected List<FeedItem> loadData() {
        return DBReader.getFavoriteItemsList();
    }
}
