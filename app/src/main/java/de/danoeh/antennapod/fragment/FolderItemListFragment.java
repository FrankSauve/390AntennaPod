package de.danoeh.antennapod.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.apache.commons.lang3.Validate;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.AllEpisodesRecycleAdapter;
import de.danoeh.antennapod.adapter.DefaultActionButtonCallback;
import de.danoeh.antennapod.core.asynctask.FolderRemover;
import de.danoeh.antennapod.core.dialog.ConfirmationDialog;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.preferences.PlaybackPreferences;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.service.download.Downloader;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DownloadRequestException;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.core.util.LongList;
import de.danoeh.antennapod.dialog.EpisodesApplyActionFragment;
import de.danoeh.antennapod.dialog.RenameFeedDialog;
import de.danoeh.antennapod.menuhandler.FeedItemMenuHandler;
import de.danoeh.antennapod.menuhandler.FeedMenuHandler;

/**
 * Created by William on 2018-03-24.
 */

public class FolderItemListFragment extends Fragment {

    public static final String TAG = "FolderItemListFragment";
    public static final String ARGUMENT_FOLDER_ID = "argument.de.danoeh.antennapod.folder_id";

    private Folder folder;
    protected List<FeedItem> episodes;
    private long folderID;

    protected AllEpisodesRecycleAdapter adapter;
    private ContextMenu contextMenu;
    private AdapterView.AdapterContextMenuInfo lastMenuInfo = null;

    private boolean itemsLoaded = false;
    private boolean viewsCreated = false;

    private List<Downloader> downloaderList;

    private LinearLayoutManager layoutManager;
    private ProgressBar progLoading;

    private RecyclerView recyclerView;


    public static FolderItemListFragment newInstance(long folderId) {
        FolderItemListFragment i = new FolderItemListFragment();
        Bundle b = new Bundle();
        b.putLong(ARGUMENT_FOLDER_ID, folderId);
        i.setArguments(b);
        return i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        Validate.notNull(args);
        folderID = args.getLong(ARGUMENT_FOLDER_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewsCreated && itemsLoaded) {
            onFragmentLoaded();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems();
        registerForContextMenu(recyclerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterForContextMenu(recyclerView);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(folder != null) {
            folder = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateViewHelper(inflater, container, savedInstanceState,
                R.layout.all_episodes_fragment);
    }

    protected View onCreateViewHelper(LayoutInflater inflater,
                                      ViewGroup container,
                                      Bundle savedInstanceState,
                                      int fragmentResource) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(fragmentResource, container, false);

        recyclerView = (RecyclerView) root.findViewById(android.R.id.list);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());

        progLoading = (ProgressBar) root.findViewById(R.id.progLoading);

        if (!itemsLoaded) {
            progLoading.setVisibility(View.VISIBLE);
        }

        viewsCreated = true;

        if (itemsLoaded) {
            onFragmentLoaded();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetViewState();
    }

    private void resetViewState() {
        adapter = null;
        viewsCreated = false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!isAdded()) {
            return;
        }
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.folderlist, menu);

        int[] attrs = { R.attr.action_bar_icon_color };
        TypedArray ta = getActivity().obtainStyledAttributes(UserPreferences.getTheme(), attrs);
        int textColor = ta.getColor(0, Color.GRAY);
        ta.recycle();

        menu.findItem(R.id.episode_actions).setIcon(new IconDrawable(getActivity(),
                FontAwesomeIcons.fa_gears).color(textColor).actionBarSize());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                case R.id.episode_actions:
                    EpisodesApplyActionFragment fragment = EpisodesApplyActionFragment
                            .newInstance(folder.getEpisodes());
                    ((MainActivity)getActivity()).loadChildFragment(fragment);
                    return true;
                case R.id.rename_item:
                    new RenameFeedDialog(getActivity(), folder).show();
                    return true;
                case R.id.remove_item:
                    final FolderRemover remover = new FolderRemover(getContext(), folder) {
                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            ((MainActivity) getActivity()).loadFragment(FoldersFragment.TAG, null);
                        }
                    };
                    ConfirmationDialog conDialog = new ConfirmationDialog(getContext(),
                            R.string.remove_folder_label,
                            getString(R.string.folder_delete_confirmation_msg, folder.getName())) {
                        @Override
                        public void onConfirmButtonPressed(
                                DialogInterface dialog) {
                            dialog.dismiss();
                            long mediaId = PlaybackPreferences.getCurrentlyPlayingFeedMediaId();
                            if (mediaId > 0 &&
                                    FeedItemUtil.indexOfItemWithMediaId(folder.getEpisodes(), mediaId) >= 0) {
                                Log.d(TAG, "Currently playing episode is about to be deleted, skipping");
                                remover.skipOnCompletion = true;
                                int playerStatus = PlaybackPreferences.getCurrentPlayerStatus();
                                if(playerStatus == PlaybackPreferences.PLAYER_STATUS_PLAYING) {
                                    getActivity().sendBroadcast(new Intent(
                                            PlaybackService.ACTION_PAUSE_PLAY_CURRENT_EPISODE));
                                }
                            }
                            remover.executeAsync();
                        }
                    };
                    conDialog.createNewDialog().show();
                    return true;
                default:
                    return false;

            }
        }
        else {
            return true;
        }
    }

    private final FeedItemMenuHandler.MenuInterface contextMenuInterface = new FeedItemMenuHandler.MenuInterface() {
        @Override
        public void setItemVisibility(int id, boolean visible) {
            if(contextMenu == null) {
                return;
            }
            MenuItem item = contextMenu.findItem(id);
            if (item != null) {
                item.setVisible(visible);
            }
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        FeedItem item = itemAccess.getItem(adapterInfo.position);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.feeditemlist_context, menu);

        if (item != null) {
            menu.setHeaderTitle(item.getTitle());
        }

        contextMenu = menu;
        lastMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        FeedItemMenuHandler.onPrepareMenu(contextMenuInterface, item, true, null);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(menuInfo == null) {
            menuInfo = lastMenuInfo;
        }
        // because of addHeaderView(), positions are increased by 1!
        FeedItem selectedItem = itemAccess.getItem(menuInfo.position);

        if (selectedItem == null) {
            Log.i(TAG, "Selected item at position " + menuInfo.position + " was null, ignoring selection");
            return super.onContextItemSelected(item);
        }

        try {
            return FeedItemMenuHandler.onMenuItemClicked(getActivity(), item.getItemId(), selectedItem);
        } catch (DownloadRequestException e) {
            // context menu doesn't contain download functionality
            return true;
        }
    }

    /*@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(adapter == null) {
            return;
        }
        MainActivity activity = (MainActivity) getActivity();
        long[] ids = FeedItemUtil.getIds(folder.getEpisodes());
        activity.loadChildFragment(ItemFragment.newInstance(ids, position));
        activity.getSupportActionBar().setTitle(folder.getName());
    }*/

    private void onFragmentLoaded() {
        Log.d(TAG, "onFragmentLoaded() called");
        if(adapter == null) {
            MainActivity mainActivity = (MainActivity) getActivity();
            adapter = new AllEpisodesRecycleAdapter(mainActivity, itemAccess,
                    new DefaultActionButtonCallback(mainActivity), false);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();

    }

    protected AllEpisodesRecycleAdapter.ItemAccess itemAccess = new AllEpisodesRecycleAdapter.ItemAccess() {

        @Override
        public int getCount() {
            if (episodes != null) {
                return episodes.size();
            }
            return 0;
        }

        @Override
        public FeedItem getItem(int position) {
            if (episodes != null && 0 <= position && position < episodes.size()) {
                return episodes.get(position);
            }
            return null;
        }

        @Override
        public LongList getItemsIds() {
            if(episodes == null) {
                return new LongList(0);
            }
            LongList ids = new LongList(episodes.size());
            for(FeedItem episode : episodes) {
                ids.add(episode.getId());
            }
            return ids;
        }

        @Override
        public int getItemDownloadProgressPercent(FeedItem item) {
            if (downloaderList != null) {
                for (Downloader downloader : downloaderList) {
                    if (downloader.getDownloadRequest().getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA
                            && downloader.getDownloadRequest().getFeedfileId() == item.getMedia().getId()) {
                        return downloader.getDownloadRequest().getProgressPercent();
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean isInQueue(FeedItem item) {
            return item != null && item.isTagged(FeedItem.TAG_QUEUE);
        }

        @Override
        public LongList getQueueIds() {
            LongList queueIds = new LongList();
            if(episodes == null) {
                return queueIds;
            }
            for(FeedItem item : episodes) {
                if(item.isTagged(FeedItem.TAG_QUEUE)) {
                    queueIds.add(item.getId());
                }
            }
            return queueIds;
        }

    };

    protected void loadItems() {
        if (viewsCreated && !itemsLoaded) {
            recyclerView.setVisibility(View.GONE);
            progLoading.setVisibility(View.VISIBLE);
        }
        folder = loadFolder();
        episodes = folder.getEpisodes();
        if(episodes != null){
            recyclerView.setVisibility(View.VISIBLE);
            progLoading.setVisibility(View.GONE);
            itemsLoaded = true;
            if (viewsCreated) {
                onFragmentLoaded();
            }
        }
    }

    private Folder loadFolder(){

        Folder folder = DBReader.getFolder(folderID);
        DBReader.loadFolderDataOfFolderItemList(folder.getEpisodes());
        DBReader.loadFeedDataOfFeedItemList(folder.getEpisodes());

        return folder;
    }

}