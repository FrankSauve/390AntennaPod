package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.adapter.HomeRecyclerViewAdapter;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.storage.DBReader;

public class HomeFragment extends ItunesSearchFragment {

    private HomeRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    View v;

    public static final String TAG = "HomeFragment";
    List<FeedItem> favorites;



    /**
     * Constructor
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favorites = loadFavoriteData();

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayoutManager layoutManager = new  LinearLayoutManager(getActivity());
        v = inflater.inflate(R.layout.activity_main, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.home_recyclerview);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        adapter = new HomeRecyclerViewAdapter(getContext(), favorites, "Favorites");
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return v;
    }

    protected List<FeedItem> loadFavoriteData() {
        return DBReader.getFavoriteItemsList();
    }

}
