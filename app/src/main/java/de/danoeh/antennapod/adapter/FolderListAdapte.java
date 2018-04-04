package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.ObjectUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.fragment.ItemlistFragment;
import de.danoeh.antennapod.fragment.QueueFragment;

/**
 * Created by Costa on 26/03/2018.
 */

public class FolderListAdapte extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private final WeakReference<MainActivity> mainActivity;

    public FolderListAdapte(Context context, ArrayList<Folder> users, MainActivity ma) {

        super(context, 0, users);
        mainActivity = new WeakReference<>(ma);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Folder folder = (Folder) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.nav_feedlistitem, parent, false);
        }
        // Lookup view for data population
        TextView tvTitle = (TextView) convertView.findViewById(R.id.txtvTitle);
        ImageView ivImage = (ImageView) convertView.findViewById(R.id.imgvCover);
        TextView tvCount = (TextView) convertView.findViewById(R.id.txtvCount);
        // Populate the data into the template view using the data object
        tvTitle.setText(folder.name);
       // tvCount.setText(folder.getEpisodesNum());
        // Return the completed view to render on screen
        return convertView;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Fragment fragment = ItemlistFragment.newInstance(getItemId(position));
        mainActivity.get().loadChildFragment(fragment);
    }

    public interface ItemAccess {
        int getCount();
        Folder getFolder(int position);
        int getFolderCounter(long folderId);
    }
}


