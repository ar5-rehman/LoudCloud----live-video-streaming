package com.tech.loudcloud;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tech.loudcloud.UserPackage.adapters.LiveArtistAdapter;
import com.tech.loudcloud.UserPackage.pojoClasses.LiveArtistPojo;
import com.tech.loudcloud.login.UserArtistDialogFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends AppCompatActivity {

    ImageView profileImage;
    SwipeRefreshLayout swipeRefreshLayout;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    public String channelName, artistName, artistImageUrl, streamName, artistId, userProfileName, userImage;

    private RecyclerView.Adapter adapter;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    List<LiveArtistPojo> list;

    Context context;

    ImageView logo;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Loud Cloud");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        firebaseDatabase = FirebaseDatabase.getInstance();

        list = new ArrayList();

        logo = findViewById(R.id.logo);
        text = findViewById(R.id.text);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        profileImage = headerView.findViewById(R.id.userProfileImage);
        profileImage.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);


        Menu menu = navigationView.getMenu();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.nav_login)
                {
                    FragmentManager manager = getSupportFragmentManager();
                    UserArtistDialogFragment mydialog = new UserArtistDialogFragment();
                    mydialog.show(manager, "mydialog");
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(layoutManager); // set LayoutManager to RecyclerView

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLiveArtists();
            }
        });

        getLiveArtists();

    }

    public void getLiveArtists()
    {
        list.clear();
        adapter = new LiveArtistAdapter(list, context, userProfileName, userImage);
        recyclerView.setAdapter(adapter);

        ref= FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    String key = dsp.getKey();

                    ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dsp) {
                            if(dataSnapshot.child(key).child("channelName").exists()) {
                                channelName = dataSnapshot.child(key).child("channelName").getValue().toString();
                                artistName = dataSnapshot.child(key).child("artistName").getValue().toString();
                                artistImageUrl = dataSnapshot.child(key).child("artistImage").getValue().toString();
                                artistId = dataSnapshot.child(key).child("artistId").getValue().toString();

                                list.add(new LiveArtistPojo(artistId, channelName, artistName, artistImageUrl));
                                adapter = new LiveArtistAdapter(list, context, userProfileName, userImage);
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
            recyclerView.setVisibility(View.GONE);
            logo.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }
}