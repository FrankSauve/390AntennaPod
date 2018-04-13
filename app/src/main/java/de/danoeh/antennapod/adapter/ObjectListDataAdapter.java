package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.glide.ApGlideSettings;

public class ObjectListDataAdapter extends RecyclerView.Adapter<ObjectListDataAdapter.ViewHolder> {

    private List<FeedItem> itemsList;
    private Context context;

    public ObjectListDataAdapter(Context context, List<FeedItem> itemsList) {
        this.itemsList = itemsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_single_card, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FeedItem feedItem = itemsList.get(position);

        holder.title.setText(feedItem.getTitle());


        Glide.with(context)
                .load(itemsList.get(position).getImageLocation())
                .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView title;

        protected ImageView itemImage;


        public ViewHolder(View view) {
            super(view);

            this.title = (TextView) view.findViewById(R.id.title);
            title.setMovementMethod(new ScrollingMovementMethod());
            this.itemImage = (ImageView) view.findViewById(R.id.cover);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Toast.makeText(v.getContext(), title.getText(), Toast.LENGTH_SHORT).show();

                }
            });


        }


    }
}
