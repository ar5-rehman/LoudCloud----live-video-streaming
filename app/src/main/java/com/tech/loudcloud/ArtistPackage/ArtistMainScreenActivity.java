package com.tech.loudcloud.ArtistPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.share.widget.ShareDialog;
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
import com.tech.loudcloud.ArtistPackage.artistFragments.ArtistMainScreenFragment;
import com.tech.loudcloud.ArtistPackage.artistFragments.ArtistWalletFragment;
import com.tech.loudcloud.ArtistPackage.artistFragments.CampaignFragment;
import com.tech.loudcloud.UserPackage.ArchivedVideosService.WebServices;
import com.tech.loudcloud.ArtistPackage.artistFragments.ArtistProfile;
import com.tech.loudcloud.MainScreen;
import com.tech.loudcloud.R;
import com.tech.loudcloud.liveVideoBroadcaster.LiveVideoBroadcasterActivity;
import com.tech.loudcloud.login.UserArtistDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ArtistMainScreenActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    FirebaseAuth auth;

    TextView headerUserName, headerUserEmail;
    DrawerLayout drawer;
    Menu menu;
    public boolean fragmentCheck;
    ImageView userProfileImage;

    Fragment frag = null;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    WebServices webServices;
    String liveStreamName;

    boolean checkCampaignStatus = false;

    ImageView artistPicture;
    TextView songsList, performanceLocation, performanceLocationTwo, votesNeeded, votesEarned, streamTime, alterSongs,
             performanceOneVotes, performanceTwoVotes, locationOneVotes, locationTwoVotes;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    String list_of_songs, alternative_songs, location_of_performance,
            location_of_performance_two, votes_earn, votes_needed, stream_time,
            performanceOneEarnedVotes, performanceTwoEarnedVotes, locationOneEarnedVotes, locationTwoEarnedVotes;

    String userEmail, userName, profileImageURL, profileImageURLLIVE, artistApproval,
            walletID, artistWalletID, fundsRequest, fundsReq, earnings, earn;
    public static String uid, userChannelName;

    // Permission request code of any integer value
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            RECORD_AUDIO,
            Manifest.permission.CAMERA,
            WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_main_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.artist_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        NavigationView navigationView = findViewById(R.id.artist_nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        headerUserName = headerView.findViewById(R.id.headerUserName);
        headerUserEmail = headerView.findViewById(R.id.headerUserEmail);
        userProfileImage = headerView.findViewById(R.id.userProfileImage);

        menu = navigationView.getMenu();
        menu.findItem(R.id.go_live).setVisible(false);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId()==R.id.nav_login)
                {

                    FragmentManager manager = getSupportFragmentManager();
                    UserArtistDialogFragment mydialog = new UserArtistDialogFragment();
                    mydialog.show(manager, "mydialog");

                }else if(item.getItemId()==R.id.nav_profile){

                    frag  = new ArtistProfile(ArtistMainScreenActivity.this);

                }else if(item.getItemId()==R.id.nav_home){

                    frag = new ArtistMainScreenFragment(ArtistMainScreenActivity.this);

                }else if(item.getItemId()==R.id.nav_logout)
                {

                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();

                    headerUserName.setText(" ");
                    headerUserEmail.setText(" ");

                    Intent in = new Intent(ArtistMainScreenActivity.this, MainScreen.class);
                    startActivity(in);

                }else if(item.getItemId()==R.id.go_live){

                    int need = Integer.valueOf(votes_needed);
                    int earn = Integer.valueOf(votes_earn);
                    if(earn>=need) {
                        checkPermission();
                    }else{
                        Toast.makeText(ArtistMainScreenActivity.this, "MORE VOTES NEEDED", Toast.LENGTH_SHORT).show();
                    }

                }else if(item.getItemId()==R.id.nav_campaign){

                    if(!checkCampaignStatus) {

                        fragmentCheck = true;

                        FragmentManager manager = getSupportFragmentManager();
                        CampaignFragment campaignFragment = new CampaignFragment(ArtistMainScreenActivity.this);

                        Bundle bundle = new Bundle();
                        bundle.putString("userName", userChannelName);
                        bundle.putString("artistPic", profileImageURLLIVE);
                        campaignFragment.setArguments(bundle);
                        campaignFragment.show(manager, "CampaignFragment");
                    }else{
                        Toast.makeText(ArtistMainScreenActivity.this, "ALREADY ONE CAMPAIGN STARTED", Toast.LENGTH_SHORT).show();
                    }
                }else if(item.getItemId()==R.id.wallet){

                    frag = new ArtistWalletFragment(ArtistMainScreenActivity.this, artistWalletID, fundsReq, earn);

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

        getArtistData();

        frag = new ArtistMainScreenFragment(ArtistMainScreenActivity.this);

        if (frag != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
            transaction.commit(); // commit the changes
        }

    }

    private void checkPermission() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {
            String dateTime = getCurrentDateTime();
            liveStreamName = userChannelName+dateTime;
            Intent i = new Intent(this, LiveVideoBroadcasterActivity.class);
            i.putExtra("streamName", liveStreamName);
            i.putExtra("userChannelName", userChannelName);
            i.putExtra("profileImageURLLIVE", profileImageURLLIVE);
            i.putExtra("streamTime", stream_time);
            i.putExtra("votesEarn", votes_earn);
            startActivity(i);
        } else {
            requestPermissions();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                granted = (result == PackageManager.PERMISSION_GRANTED);
                if (!granted) break;
            }

            if (granted) {

            } else {
                Toast.makeText(this, "Necessary permissions acquired", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCurrentDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        String timeStamp = dateFormat.format(new Date());
        return timeStamp;
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
         if(v.getId() == R.id.goLiveBtn){
            int need = Integer.valueOf(votes_needed);
            int earn = Integer.valueOf(votes_earn);
            if(earn>=need) {
                checkPermission();
            }else{
                Toast.makeText(this, "MORE VOTES NEEDED", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void getArtistData()
    {
        firebaseFirestore.collection("artist_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e !=null)
                {

                }
                else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if(documentChange!=null) {
                            userEmail = documentChange.getDocument().getData().get("Email").toString();
                            userName = documentChange.getDocument().getData().get("Name").toString();
                            profileImageURL = documentChange.getDocument().getData().get("ArtistProPicUrl").toString();
                            artistApproval = documentChange.getDocument().getData().get("Approval").toString();
                            walletID = documentChange.getDocument().getData().get("WalletID").toString();
                            fundsRequest = documentChange.getDocument().getData().get("FundRequest").toString();
                            earnings = documentChange.getDocument().getData().get("Earnings").toString();


                            if(user.getEmail().equals(userEmail)) {

                                if(artistApproval.equals("false")) {
                                    menu.findItem(R.id.nav_campaign).setVisible(false);
                                    menu.findItem(R.id.wallet).setVisible(false);
                                    menu.findItem(R.id.go_live).setVisible(false);
                                }else{
                                    menu.findItem(R.id.nav_campaign).setVisible(true);
                                    menu.findItem(R.id.wallet).setVisible(true);

                                    userChannelName = userName;
                                    headerUserName.setText(userName);
                                    headerUserEmail.setText(userEmail);
                                    profileImageURLLIVE = profileImageURL;
                                    artistWalletID = walletID;
                                    fundsReq = fundsRequest;
                                    earn = earnings;

                                    if(profileImageURL.equals(" "))
                                    {

                                    }else {
                                        Glide.with(getApplicationContext()).load(profileImageURL).diskCacheStrategy( DiskCacheStrategy.DATA ).into(userProfileImage);
                                    }

                                    getCampaign();


                                }

                            }
                        }
                    }
                }
            }
        });
    }

    public void getCampaign()
    {
        ref = FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot dsp: dataSnapshot.getChildren()){

                    ref.child(userChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dsp) {
                            if (dataSnapshot.child(userChannelName).child("listOfSongs").exists()) {
                               checkCampaignStatus = true;
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}