package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.FeedImage;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.glide.ApGlideSettings;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>{

    private Context context;
    private List<FeedItem> feedItems = Collections.emptyList();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private String title = "";

    // data is passed into the constructor
    public HomeRecyclerViewAdapter(Context context, List<FeedItem> feedItems, String title) {
        this.mInflater = LayoutInflater.from(context);
        this.feedItems = feedItems;
        this.context =context;
        this.title = title;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.home_recycler_view_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemTitle.setText(title);
        String feed = feedItems.get(position).getTitle();
        holder.myTextView.setText(feed);
        Glide.with(context)
                .load(feedItems.get(position).getImageLocation())
                .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                .into(holder.myView);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView itemTitle;
        public ImageView myView;
        public TextView myTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemTitle = (TextView) itemView.findViewById(R.id.itemTitle);
            myView = (ImageView) itemView.findViewById(R.id.cover);
            myTextView = (TextView) itemView.findViewById(R.id.feedName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public FeedItem getItem(int id) {
        return feedItems.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
