package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.folders.Folder;
import de.danoeh.antennapod.core.glide.ApGlideSettings;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.fragment.FolderItemListFragment;
import de.danoeh.antennapod.fragment.HomeFragment;
import de.danoeh.antennapod.fragment.ItemFragment;

public class ObjectListDataAdapter extends RecyclerView.Adapter<ObjectListDataAdapter.ViewHolder> {

    private List<FeedItem> itemsList;
    private List<Folder> foldersList;
    private Context context;
    private HomeFragment fragment;

    public ObjectListDataAdapter(Context context, List<FeedItem> itemsList, List<Folder> foldersList, HomeFragment fragment) {
        this.itemsList = itemsList;
        this.foldersList = foldersList;
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_single_card, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(itemsList != null){
            FeedItem feedItem = itemsList.get(position);
            holder.title.setText(feedItem.getTitle());
            Glide.with(context)
                    .load(itemsList.get(position).getImageLocation())
                    .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                    .into(holder.itemImage);
        }
        else if (foldersList != null){
            Folder folder = foldersList.get(position);
            holder.title.setText(folder.getName());
            if(foldersList.get(position).getEpisodes() != null){
                Glide.with(context)
                        .load(folder.getEpisodes().get(0).getImageLocation()) //loads the first episode image of the folder
                        .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                        .into(holder.itemImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(itemsList != null){
            count = itemsList.size();
        }
        else if(foldersList != null){
            count = foldersList.size();
        }

        return count;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView title;

        protected ImageView itemImage;

        public ViewHolder(View view) {
            super(view);

            this.title = (TextView) view.findViewById(R.id.title);
            title.setMovementMethod(new ScrollingMovementMethod());
            this.itemImage = (ImageView) view.findViewById(R.id.cover);

            // On click listener for homepage items
            view.setOnClickListener(v -> {
                // Open ItemFragment if a feed is clicked
                long[] ids = FeedItemUtil.getIds(itemsList);
                ((MainActivity)fragment.getActivity()).loadChildFragment(ItemFragment.newInstance(ids, getAdapterPosition()));

                // Open FolderItemListFragment if a folder is clicked
                if(foldersList != null){
                    ((MainActivity)fragment.getActivity()).loadChildFragment(FolderItemListFragment.newInstance(foldersList.get(0).getId()));
                    ((MainActivity)fragment.getActivity()).setActionBarTitle(foldersList.get(0).getName());
                }
            });
        }
    }
}
