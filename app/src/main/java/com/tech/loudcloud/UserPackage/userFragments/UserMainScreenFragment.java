package com.tech.loudcloud.UserPackage.userFragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;
import com.tech.loudcloud.UserPackage.adapters.LiveArtistAdapter;
import com.tech.loudcloud.UserPackage.pojoClasses.LiveArtistPojo;

import java.util.ArrayList;
import java.util.List;

public class UserMainScreenFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;

    TextView headerUserName, headerUserEmail;
    ImageView userProfileImage;
    DrawerLayout drawer;

    SharedPreferences artistOrUser, fbUserData, gogleUserData;

    boolean fbUser = false, gogle = false;
    public boolean  fragmentCheck;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    Fragment frag = null;

    public String channelName, artistName, artistImageUrl, streamName, artistId, userProfileName, userImage;

    SwipeRefreshLayout swipeRefreshLayout;

    ImageView logo;
    TextView text;

    // Permission request code of any integer value
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecyclerView.Adapter adapter;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    List<LiveArtistPojo> list;

    Context context;

    public UserMainScreenFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context.registerReceiver(receiver, new IntentFilter("com.tech.loudcloud.UserPackage.adapters"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_main_screen, container, false);

        getActivity().setTitle("Stage");

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        list = new ArrayList();

        logo = view.findViewById(R.id.logo);
        text = view.findViewById(R.id.text);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(layoutManager); // set LayoutManager to RecyclerView

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLiveArtists();
            }
        });

        getLiveArtists();

        context.registerReceiver(receiver, new IntentFilter("com.tech.loudcloud.UserPackage.adapters"));

        return view;
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
    public void onDestroy() {
        super.onDestroy();
        if(receiver!=null) {
            context.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.userProfileImage) {
            if (fbUser || gogle) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                fragmentCheck = true;
                Fragment fragment = new UserProfile(context);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.user_drawer_layout, fragment, "userProfile").addToBackStack("back").commit();
                drawer.closeDrawer(GravityCompat.START);
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                streamName = intent.getStringExtra("channelName");
            }
        }
    };


}