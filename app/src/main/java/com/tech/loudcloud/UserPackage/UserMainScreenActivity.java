package com.tech.loudcloud.UserPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
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
import com.tech.loudcloud.MainScreen;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.pojoClasses.LiveArtistPojo;
import com.tech.loudcloud.UserPackage.userFragments.ArchievedStreamFragment;
import com.tech.loudcloud.UserPackage.userFragments.UserCampaignFragment;
import com.tech.loudcloud.UserPackage.userFragments.UserMainScreenFragment;
import com.tech.loudcloud.UserPackage.userFragments.UserProfile;
import com.tech.loudcloud.UserPackage.userFragments.UserWalletFragment;
import java.util.ArrayList;
import java.util.List;

public class UserMainScreenActivity extends AppCompatActivity implements View.OnClickListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        list = new ArrayList();

        artistOrUser = getSharedPreferences("artistOrUser",MODE_PRIVATE);
        fbUser = artistOrUser.getBoolean("fb",false);
        gogle = artistOrUser.getBoolean("gogle",false);


        fbUserData = getSharedPreferences("fbUserData", MODE_PRIVATE);
        gogleUserData = getSharedPreferences("gogleUserData", MODE_PRIVATE);

        drawer = findViewById(R.id.user_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.user_nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        headerUserName = headerView.findViewById(R.id.headerUserName);
        headerUserEmail = headerView.findViewById(R.id.headerUserEmail);
        userProfileImage = headerView.findViewById(R.id.userProfileImage);

        userProfileImage.setOnClickListener(this);

        Menu menu = navigationView.getMenu();

        if(fbUser==true){

            String email = fbUserData.getString("email", " ");
            String name = fbUserData.getString("name", " ");
            String proPic = fbUserData.getString("proPic", " ");

            headerUserName.setText(name);
            headerUserEmail.setText(email);
            Glide.with(getApplicationContext()).load(proPic).diskCacheStrategy( DiskCacheStrategy.DATA ).into(userProfileImage);

        }else if(gogle==true){


            String email = gogleUserData.getString("email", " ");
            String name = gogleUserData.getString("name", " ");
            String proPic = gogleUserData.getString("proPic", " ");

            headerUserName.setText(name);
            headerUserEmail.setText(email);
            Glide.with(getApplicationContext()).load(proPic).diskCacheStrategy( DiskCacheStrategy.DATA ).into(userProfileImage);

        }else if (user.isEmailVerified()) {
            getUserData();

        } else {
            Toast.makeText(this, "Please, verify your email first!", Toast.LENGTH_SHORT).show();
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.nav_logout)
                {
                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();

                    headerUserName.setText(" ");
                    headerUserEmail.setText(" ");

                    artistOrUser.edit().clear().apply();
                    fbUserData.edit().clear().apply();
                    gogleUserData.edit().clear().apply();
                    fbUser = false;
                    gogle = false;
                    Intent in = new Intent(UserMainScreenActivity.this, MainScreen.class);
                    startActivity(in);
                }else if(item.getItemId()==R.id.nav_profile){

                    if (fbUser || gogle) {
                        drawer.closeDrawer(GravityCompat.START);
                    } else {
                        frag = new UserProfile(UserMainScreenActivity.this);
                    }

                }else if(item.getItemId()==R.id.nav_home){

                    frag = new UserMainScreenFragment(UserMainScreenActivity.this);

                }else if(item.getItemId()==R.id.nav_archived){

                    frag = new ArchievedStreamFragment(UserMainScreenActivity.this);

                }else if(item.getItemId()==R.id.nav_campaigns){

                    frag = new UserCampaignFragment(UserMainScreenActivity.this);

                }else if(item.getItemId()==R.id.nav_wallet){

                    frag = new UserWalletFragment(UserMainScreenActivity.this);

                }

                if (frag != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                    transaction.commit(); // commit the changes
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        frag = new UserMainScreenFragment(UserMainScreenActivity.this);
        if (frag != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
            transaction.commit(); // commit the changes
        }

    }

    public void getUserData()
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e !=null)
                {

                }
                else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if(documentChange.getDocument().exists()) {
                            String userEmail = documentChange.getDocument().getData().get("Email").toString();
                            String userName = documentChange.getDocument().getData().get("Name").toString();
                            String profileImageURL = documentChange.getDocument().getData().get("UserProfileImage").toString();

                            if(user.getEmail().equals(userEmail)) {
                                userProfileName = userName;
                                headerUserName.setText(userName);
                                headerUserEmail.setText(userEmail);
                                if(profileImageURL.equals(" "))
                                {
                                    userImage = "";
                                }else {
                                    userImage = profileImageURL;
                                    Glide.with(getApplicationContext()).load(profileImageURL).diskCacheStrategy( DiskCacheStrategy.DATA ).into(userProfileImage);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
                super.onBackPressed();
                finishAffinity();
            }
        }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.userProfileImage) {
            if (fbUser || gogle) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                fragmentCheck = true;
                Fragment fragment = new UserProfile(UserMainScreenActivity.this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, fragment, "userProfile").addToBackStack("back").commit();
                drawer.closeDrawer(GravityCompat.START);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
}