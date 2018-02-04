package de.danoeh.antennapod.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.SubscriptionsAdapter;
import de.danoeh.antennapod.adapter.TrendingAdapter;
import de.danoeh.antennapod.core.asynctask.FeedRemover;
import de.danoeh.antennapod.core.dialog.ConfirmationDialog;
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.preferences.PlaybackPreferences;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.dialog.RenameFeedDialog;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Fragment for displaying trending podcasts
 */
public class TrendingFragment extends Fragment {

    public static final String TAG = "TrendingFragment";

    private static final int EVENTS = EventDistributor.FEED_LIST_UPDATE
            | EventDistributor.UNREAD_ITEMS_UPDATE;

    private GridView subscriptionGridLayout;
    private DBReader.NavDrawerData navDrawerData;
    private TrendingAdapter trendingAdapter;

    private int mPosition = -1;

    private Subscription subscription;

    public TrendingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // So, we certainly *don't* have an options menu,
        // but unless we say we do, old options menus sometimes
        // persist.  mfietz thinks this causes the ActionBar to be invalidated
        setHasOptionsMenu(true);
    }

    @Override
    //To Change
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        subscriptionGridLayout = (GridView) root.findViewById(R.id.subscriptions_grid);
        registerForContextMenu(subscriptionGridLayout);
        return root;
    }

    @Override
    //To Change
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trendingAdapter = new TrendingAdapter((MainActivity)getActivity(), itemAccess);

        subscriptionGridLayout.setAdapter(trendingAdapter);

        subscriptionGridLayout.setOnItemClickListener(trendingAdapter);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.trending_label);
        }

        EventDistributor.getInstance().register(contentUpdate);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = adapterInfo.position;

        Object selectedObject = trendingAdapter.getItem(position);
        if (selectedObject.equals(SubscriptionsAdapter.ADD_ITEM_OBJ)) {
            mPosition = position;
            return;
        }

        Feed feed = (Feed)selectedObject;

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.nav_feed_context, menu);

        menu.setHeaderTitle(feed.getTitle());

        mPosition = position;
    }

    @Override
    //To Change
    public boolean onContextItemSelected(MenuItem item) {

        final int position = mPosition;
        mPosition = -1; // reset
        if(position < 0) {
            return false;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    //Might have to change
    public void onResume() {
        super.onResume();
    }

    //Might have to change
    private EventDistributor.EventListener contentUpdate = new EventDistributor.EventListener() {
        @Override
        public void update(EventDistributor eventDistributor, Integer arg) {
            if ((EVENTS & arg) != 0) {
                Log.d(TAG, "Received contentUpdate Intent.");
            }
        }
    };

    //To Change 
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