package de.danoeh.antennapod.fragment;


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
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.storage.DBReader;
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

        Feed feed = (Feed)selectedObject;

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.nav_feed_context, menu);

        menu.setHeaderTitle(feed.getTitle());

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

        Feed feed = (Feed)selectedObject;
        switch(item.getItemId()) {
            case R.id.mark_all_seen_item:
                return true;
            case R.id.mark_all_read_item:
                return true;
            case R.id.rename_item:
                new RenameFeedDialog(getActivity(), feed).show();
                return true;
            case R.id.remove_item:
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
        public int getFolderCounter(long folderId) {
            return folders != null ? 25 : 0; //Hard coded (to change later)
        }
    };
}

