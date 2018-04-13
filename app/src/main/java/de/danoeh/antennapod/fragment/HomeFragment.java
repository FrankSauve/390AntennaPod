package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.danoeh.antennapod.Model.SectionDataModel;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.adapter.HomeRecyclerViewAdapter;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.storage.DBReader;

public class HomeFragment extends Fragment {

    private HomeRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    View v;

    public static final String TAG = "HomeFragment";
    List<SectionDataModel> allData;

    Map<String, List<FeedItem>> dataList;

    /**
     * Constructor
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allData = loadData();

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        v = inflater.inflate(R.layout.activity_main, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        adapter = new HomeRecyclerViewAdapter(getContext(), allData);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return v;
    }

    public List<SectionDataModel> loadData() {
        dataList = new HashMap<>();
        allData = new ArrayList<>();
        List<FeedItem> favorites = DBReader.getFavoriteItemsList();
        List<FeedItem> queued = DBReader.getQueue();
        List<FeedItem> latest = DBReader.getNewItemsList();
        List<FeedItem> recent = DBReader.getRecentlyPublishedEpisodes(10);
        List<FeedItem> history = DBReader.getPlaybackHistory();
        if (!queued.isEmpty()) {
            dataList.put("Queued", queued);
        }
        if (!favorites.isEmpty()) {
            dataList.put("Favorites", favorites);
        }
        if (!latest.isEmpty()) {
            dataList.put("Recently Added", latest);
        }
        if (!recent.isEmpty()) {
            dataList.put("Newest", recent);
        }
        if (!history.isEmpty()) {
            dataList.put("Playback History", history);
        }


        for (String name : dataList.keySet()) {
            SectionDataModel data = new SectionDataModel();
            data.setTitle(name.toString());
            data.setFeedItem(dataList.get(name));
            allData.add(data);
        }
        return allData;
    }
}
