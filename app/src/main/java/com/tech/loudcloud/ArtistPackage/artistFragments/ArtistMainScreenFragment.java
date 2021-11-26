package com.tech.loudcloud.ArtistPackage.artistFragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tech.loudcloud.ArtistPackage.ArtistMainScreenActivity;
import com.tech.loudcloud.MainScreen;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.ArchivedVideosService.WebServices;
import com.tech.loudcloud.liveVideoBroadcaster.LiveVideoBroadcasterActivity;
import com.tech.loudcloud.login.UserArtistDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ArtistMainScreenFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    FirebaseAuth auth;

    public boolean fragmentCheck;
    ImageView logo;
    TextView campaignText, approval;
    Button goLiveBtn, IG_share_btn, createCampaign;
    CardView cardView;

    ShareDialog shareDialog;

    WebServices webServices;
    String liveStreamName;

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

    SwipeRefreshLayout swipeRefreshLayout;

    String artistName, artistPic;

    // Permission request code of any integer value
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            RECORD_AUDIO,
            Manifest.permission.CAMERA,
            WRITE_EXTERNAL_STORAGE
    };

    Context context;

    public ArtistMainScreenFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_main_screen, container, false);

        getActivity().setTitle("Stage");

        getCurrentDateTime();

        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {

        } else {
            requestPermissions();
        }


        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference(auth.getUid());

        artistPicture = view.findViewById(R.id.picture);
        songsList = view.findViewById(R.id.songsList);
        performanceLocation = view.findViewById(R.id.performancelocation);
        performanceLocationTwo = view.findViewById(R.id.performancelocationTwo);
        votesNeeded = view.findViewById(R.id.votesNeeded);
        votesEarned = view.findViewById(R.id.votesEarned);
        streamTime = view.findViewById(R.id.streamTime);
        alterSongs = view.findViewById(R.id.alternativeSongsList);
        goLiveBtn = view.findViewById(R.id.goLiveBtn);
        IG_share_btn = view.findViewById(R.id.Ig_share_btn);
        cardView = view.findViewById(R.id.cardView);
        approval = view.findViewById(R.id.approval);
        createCampaign = view.findViewById(R.id.createCampaign);
        logo = view.findViewById(R.id.logo);

        performanceOneVotes = view.findViewById(R.id.performanceOneVotes);
        performanceTwoVotes = view.findViewById(R.id.performanceTwoVotes);

        locationOneVotes = view.findViewById(R.id.locationOneVotes);
        locationTwoVotes = view.findViewById(R.id.locationTwoVotes);

        uid = user.getUid();

        campaignText = view.findViewById(R.id.campaign);

        shareDialog = new ShareDialog(this);

        getArtistData();

        IG_share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIGIntent();
            }
        });


        goLiveBtn.setOnClickListener(this);
        createCampaign.setOnClickListener(this);


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        return view;
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
            Intent i = new Intent(context, LiveVideoBroadcasterActivity.class);
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
                context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_REQ_CODE);
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
                Toast.makeText(context, "Necessary permissions acquired", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCurrentDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        String timeStamp = dateFormat.format(new Date());
        return timeStamp;
    }


    private void createIGIntent() {

        View view = LayoutInflater.from(context).inflate(R.layout.image_share_layout, null, false);
        ImageView mainImage = view.findViewById(R.id.mainImage);

        //Glide.with(context).load(profileImageURLLIVE+".jpeg").centerCrop().into(mainImage);

        Picasso.get().load(profileImageURLLIVE+".jpeg").into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mainImage.setImageBitmap(bitmap);
                getBitmapFromView(context, view);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

       /* Glide
                .with(context)
                .load(profileImageURLLIVE+".jpeg")
                .centerCrop()
                .into(mainImage);*/


       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getBitmapFromView(context, view);
            }
        },2000);*/



    }

    /*private void createBitmapFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        Intent shareIntent = new Intent(
                android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                context.getContentResolver(), bitmap, "Title", null);

        if(path!=null) {
            Uri uri = Uri.parse(path);

            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "www.loudcloudevents.com");

            // shareIntent.setPackage("com.instagram.android");
            startActivity(shareIntent);
        }else{
            Toast.makeText(context, "Alert!!\nPlease check your phone memory!!", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void getBitmapFromView(Context ctx, View view) {
        try {
            view.setLayoutParams(new
                    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT));
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            view.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(dm.heightPixels,
                            View.MeasureSpec.EXACTLY));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                    view.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            view.draw(canvas);

            Intent shareIntent = new Intent(
                    android.content.Intent.ACTION_SEND);
            shareIntent.setType("image/*");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(
                    context.getContentResolver(), bitmap, "IMG_" + System.currentTimeMillis(), null);

            if (path != null) {
                Uri uri = Uri.parse(path);

                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "www.loudcloudevents.com");
                if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
                    // shareIntent.setPackage("com.instagram.android");
                    startActivity(shareIntent);
                }
            } else {
                Toast.makeText(context, "Alert!!\nPlease check your phone memory!!", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(ctx, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                    approval.setVisibility(View.VISIBLE);
                                    campaignText.setVisibility(View.INVISIBLE);
                                    logo.setVisibility(View.INVISIBLE);
                                    createCampaign.setVisibility(View.INVISIBLE);

                                }else{
                                    approval.setVisibility(View.GONE);
                                    userChannelName = userName;

                                    profileImageURLLIVE = profileImageURL;
                                    artistWalletID = walletID;
                                    fundsReq = fundsRequest;
                                    earn = earnings;

                                    if(profileImageURL.equals(" "))
                                    {

                                    }else {
                                       // Glide.with(context).load(profileImageURL).diskCacheStrategy( DiskCacheStrategy.DATA ).into(userProfileImage);
                                    }

                                    getCampaignData();

                                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                        @Override
                                        public void onRefresh() {
                                            getCampaignData();
                                        }
                                    });
                                }

                            }
                        }
                    }
                }
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.artist_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }*/


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.userProfileImage)
        {
            fragmentCheck = true;
            Fragment fragment = new ArtistProfile(context);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_drawer_layout, fragment, "artistProfile").addToBackStack("back").commit();

        }else if(v.getId() == R.id.goLiveBtn){
            int need = Integer.valueOf(votes_needed);
            int earn = Integer.valueOf(votes_earn);
            if(earn>=need) {
                checkPermission();
            }else{
                Toast.makeText(context, "MORE VOTES NEEDED", Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId() == R.id.createCampaign){

            FragmentManager manager = getActivity().getSupportFragmentManager();
            CampaignFragment campaignFragment = new CampaignFragment(context);

            Bundle bundle = new Bundle();
            bundle.putString("userName", userChannelName);
            bundle.putString("artistPic", profileImageURLLIVE);
            campaignFragment.setArguments(bundle);
            campaignFragment.show(manager, "CampaignFragment");

        }
    }

    public void getCampaignData()
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
                                list_of_songs = dataSnapshot.child(userChannelName).child("listOfSongs").getValue().toString();
                                alternative_songs = dataSnapshot.child(userChannelName).child("alternativeSongs").getValue().toString();
                                location_of_performance = dataSnapshot.child(userChannelName).child("location").getValue().toString();
                                location_of_performance_two = dataSnapshot.child(userChannelName).child("alternativeLocations").getValue().toString();
                                votes_earn = dataSnapshot.child(userChannelName).child("votesEarned").getValue().toString();
                                votes_needed = dataSnapshot.child(userChannelName).child("votesNeeded").getValue().toString();
                                stream_time = dataSnapshot.child(userChannelName).child("liveTime").getValue().toString();
                                performanceOneEarnedVotes = dataSnapshot.child(userChannelName).child("performanceOneVote").getValue().toString();
                                performanceTwoEarnedVotes = dataSnapshot.child(userChannelName).child("performanceTwoVote").getValue().toString();
                                locationOneEarnedVotes = dataSnapshot.child(userChannelName).child("locationOneVote").getValue().toString();
                                locationTwoEarnedVotes = dataSnapshot.child(userChannelName).child("locationTwoVote").getValue().toString();

                                Glide.with(context).load(profileImageURLLIVE).diskCacheStrategy(DiskCacheStrategy.DATA).into(artistPicture);
                                songsList.setText(list_of_songs);
                                votesNeeded.setText(votes_needed);
                                votesEarned.setText(votes_earn);
                                streamTime.setText(stream_time);
                                alterSongs.setText(alternative_songs);
                                performanceLocation.setText(location_of_performance);
                                performanceLocationTwo.setText(location_of_performance_two);

                                performanceOneVotes.setText("Votes ("+performanceOneEarnedVotes+")");
                                performanceTwoVotes.setText("Votes ("+performanceTwoEarnedVotes+")");

                                locationOneVotes.setText("Votes ("+locationOneEarnedVotes+")");
                                locationTwoVotes.setText("Votes ("+locationTwoEarnedVotes+")");

                                swipeRefreshLayout.setRefreshing(false);

                                    goLiveBtn.setVisibility(View.VISIBLE);
                                    IG_share_btn.setVisibility(View.VISIBLE);
                                    cardView.setVisibility(View.VISIBLE);

                                    campaignText.setVisibility(View.INVISIBLE);
                                    logo.setVisibility(View.INVISIBLE);
                                    createCampaign.setVisibility(View.INVISIBLE);

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

        swipeRefreshLayout.setRefreshing(false);
        campaignText.setVisibility(View.VISIBLE);
        logo.setVisibility(View.VISIBLE);
        createCampaign.setVisibility(View.VISIBLE);
    }


}