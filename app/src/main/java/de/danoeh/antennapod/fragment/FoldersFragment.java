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

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.FoldersAdapter;
import de.danoeh.antennapod.core.asynctask.FolderRemover;
import de.danoeh.antennapod.core.dialog.ConfirmationDialog;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.preferences.PlaybackPreferences;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.dialog.RenameFeedDialog;

/**
 * Fragment for displaying folders created and add podcasts to these folders
 */
public class FoldersFragment extends Fragment {

    public static final String TAG = "FoldersFragment";

    private GridView foldersGridLayout;
    private FoldersAdapter foldersAdapter;

    private static final int EVENTS = EventDistributor.FOLDER_LIST_UPDATE;

    private List<Folder> folders;

    private int mPosition = -1;


    public FoldersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //You can delete this whole code once your local devices have the tables and are set up properly
        try{
            setUpTables(); //Creates folders and itemsfolders tables in DB for local devices in case they do not have these tables already
        }
        catch(android.database.sqlite.SQLiteException e) {
            //Catch error: means you already have one of the tables (probably folders table so let's create itemsfolder table)
            Log.e(TAG, "e: " + e.getMessage());
            try {
                PodDBAdapter.createItemsFoldersTable(); //Creates itemsfolders table in DB for local devices in case they do not have this table already
            } catch (android.database.sqlite.SQLiteException e1) {
                //Catch this error: means you already have this table
                Log.e(TAG, "e1: " + e1.getMessage());
                try {
                    PodDBAdapter.addFolderNameColumnToFeedItemsTable(); //Add folder_name column to feeditems table in DB for local devices in case it is not added yet
                } catch (android.database.sqlite.SQLiteException e2) {
                    //Catch this error: last catch
                    Log.e(TAG, "e2: " + e2.getMessage());
                    //Do nothing in this case the device should be set up properly in this case
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFolders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_folders, container, false);
        foldersGridLayout = (GridView) root.findViewById(R.id.folders_grid);
        registerForContextMenu(foldersGridLayout);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        foldersAdapter = new FoldersAdapter((MainActivity)getActivity(), itemAccess);

        foldersGridLayout.setAdapter(foldersAdapter);

        loadFolders();

        foldersGridLayout.setOnItemClickListener(foldersAdapter);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.folders_label);
        }

        EventDistributor.getInstance().register(contentUpdate);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void loadFolders() {

        folders = DBReader.getFolderList();
        foldersAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = adapterInfo.position;

        Object selectedObject = foldersAdapter.getItem(position);
        if (selectedObject.equals(FoldersAdapter.ADD_ITEM_OBJ)) {
            mPosition = position;
            return;
        }

        Folder folder = (Folder)selectedObject;

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.nav_folder_context, menu);

        menu.setHeaderTitle(folder.getName());

        mPosition = position;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final int position = mPosition;
        mPosition = -1; // reset
        if(position < 0) {
            return false;
        }

        Object selectedObject = foldersAdapter.getItem(position);
        if (selectedObject.equals(FoldersAdapter.ADD_ITEM_OBJ)) {
            // this is the add object, do nothing
            return false;
        }

        Folder folder = (Folder)selectedObject;
        switch(item.getItemId()) {
            case R.id.rename_item:
                new RenameFeedDialog(getActivity(), folder).show();
                return true;
            case R.id.remove_item:
                final FolderRemover remover = new FolderRemover(getContext(), folder) {
                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        loadFolders();
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
                return super.onContextItemSelected(item);
        }

    }

    private EventDistributor.EventListener contentUpdate = new EventDistributor.EventListener() {
        @Override
        public void update(EventDistributor eventDistributor, Integer arg) {
            if ((EVENTS & arg) != 0) {
                Log.d(TAG, "Received contentUpdate Intent.");
                loadFolders();
            }
        }
    };

    public List<Folder> getFolders() {
        return folders;
    }

    private void setUpTables(){
        PodDBAdapter.createFoldersTable();
        PodDBAdapter.createItemsFoldersTable();
        PodDBAdapter.addFolderNameColumnToFeedItemsTable();
    }

    private FoldersAdapter.ItemAccess itemAccess = new FoldersAdapter.ItemAccess() {
        @Override
        public int getCount() {
            if (folders != null) {
                return folders.size();
            } else {
                return 0;
            }
        }

        @Override
        public Folder getFolder(int position) {
            if (folders != null && 0 <= position && position < folders.size()) {
                return folders.get(position);
            } else {
                return null;
            }
        }

        @Override
        public int getFolderCounter(int position) {
            return DBReader.getNumberOfItemsInFolder(getFolder(position));
        }
    };

}

