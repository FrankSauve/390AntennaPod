package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.danoeh.antennapod.Model.SectionDataModel;
import de.danoeh.antennapod.R;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<SectionDataModel> dataList;


    // data is passed into the constructor
    public HomeRecyclerViewAdapter(Context context, List<SectionDataModel> dataList) {
        this.dataList = dataList;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String sectionName = dataList.get(position).getTitle();
        List singleSectionItems = dataList.get(position).getFeedItem();
        List singleSectionFolders = dataList.get(position).getFolders();
        holder.itemTitle.setText(sectionName);
        ObjectListDataAdapter itemListAdapter = null;
        if(singleSectionItems != null){
            itemListAdapter = new ObjectListDataAdapter(context, singleSectionItems, null);
        }
        else if(singleSectionFolders != null){
            Log.d("HomeRecycler", "INSIDE: " + singleSectionFolders);
            itemListAdapter = new ObjectListDataAdapter(context, null, singleSectionFolders);
        }
        holder.recycler_view_list.setHasFixedSize(true);
        holder.recycler_view_list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recycler_view_list.setAdapter(itemListAdapter);
        holder.recycler_view_list.setNestedScrollingEnabled(false);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView itemTitle;
        protected RecyclerView recycler_view_list;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemTitle = (TextView) itemView.findViewById(R.id.itemTitle);
            this.recycler_view_list = (RecyclerView) itemView.findViewById(R.id.recycler_view_list);

        }

    }

    // convenience method for getting data at click position
//    public FeedItem getItem(int id) {
//        return feedItems.get(id);
//    }

    // allows clicks events to be caught
//    public void setClickListener(ItemClickListener itemClickListener) {
//        this.mClickListener = itemClickListener;
//    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
