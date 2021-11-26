package com.tech.loudcloud.UserPackage.userFragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.adapters.CampaignsAdapter;
import com.tech.loudcloud.UserPackage.pojoClasses.CampaignsPojo;

import java.util.ArrayList;
import java.util.List;

public class UserCampaignFragment extends Fragment {

    Context context;

    String artist_pic, artist_name, list_of_songs, alternative_songs, alternativeLocation, location_of_performance, votes_earn, votes_needed, stream_time;

    public CampaignsAdapter adapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    List<CampaignsPojo> list;

    DatabaseReference ref;

    public UserCampaignFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_campaign, container, false);

        getActivity().setTitle("Campaigns");

        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCampaignData();
            }
        });

        getCampaignData();

        return view;
    }

    public void getCampaignData()
    {
        list.clear();
        adapter = new CampaignsAdapter(context, list);
        recyclerView.setAdapter(adapter);

        ref= FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    String key = dsp.getKey();

                    ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dsp) {
                            if(dataSnapshot.child(key).child("listOfSongs").exists()) {

                                artist_pic = dataSnapshot.child(key).child("artistPic").getValue().toString();
                                artist_name = dataSnapshot.child(key).child("artistName").getValue().toString();
                                list_of_songs = dataSnapshot.child(key).child("listOfSongs").getValue().toString();
                                alternative_songs = dataSnapshot.child(key).child("alternativeSongs").getValue().toString();
                                location_of_performance = dataSnapshot.child(key).child("location").getValue().toString();
                                votes_earn = dataSnapshot.child(key).child("votesEarned").getValue().toString();
                                votes_needed = dataSnapshot.child(key).child("votesNeeded").getValue().toString();
                                stream_time = dataSnapshot.child(key).child("liveTime").getValue().toString();
                                alternativeLocation = dataSnapshot.child(key).child("alternativeLocations").getValue().toString();

                                list.add(new CampaignsPojo(artist_pic,artist_name,
                                        location_of_performance,list_of_songs,alternative_songs,stream_time,
                                        votes_earn,votes_needed, alternativeLocation));
                                adapter = new CampaignsAdapter(context, list);
                                recyclerView.setAdapter(adapter);

                                swipeRefreshLayout.setRefreshing(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        if(list.size()==0) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.campaign_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_settings);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

}