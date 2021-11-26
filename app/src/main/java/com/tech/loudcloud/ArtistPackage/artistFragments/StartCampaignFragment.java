package com.tech.loudcloud.ArtistPackage.artistFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tech.loudcloud.ArtistPackage.pojoClasses.ArtistCampaignData;
import com.tech.loudcloud.ArtistPackage.ArtistMainScreenActivity;
import com.tech.loudcloud.R;

public class StartCampaignFragment extends Fragment implements View.OnClickListener {


    EditText listOfSongs, alternativeSongs, location, alternativeLocation, customVotes, customStreamTime;
    Button startCampaign;
    RadioGroup timeAndVotes;
    RadioButton oneHour, twoHour, threeHour, unlimitedTime, custom;

    String userName, liveTime, votesNeeded, artistPic;

    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    Context context;

    public StartCampaignFragment( Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_campaign, container, false);

        getActivity().setTitle("Campaign");

        userName = getArguments().getString("userName");
        artistPic = getArguments().getString("artistPic");

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference(userName);

        listOfSongs = view.findViewById(R.id.listOfSongs);
        alternativeSongs = view.findViewById(R.id.alternativeSongs);
        location = view.findViewById(R.id.location);
        alternativeLocation = view.findViewById(R.id.alternativelocation);

        customVotes = view.findViewById(R.id.customVotes);
        customStreamTime = view.findViewById(R.id.customStreamTime);

        oneHour = view.findViewById(R.id.oneHour);
        twoHour = view.findViewById(R.id.twoHours);
        threeHour = view.findViewById(R.id.threeHours);
        unlimitedTime = view.findViewById(R.id.unlimitedTime);
        custom = view.findViewById(R.id.custom);
        timeAndVotes = view.findViewById(R.id.radioGroup);


        startCampaign = view.findViewById(R.id.start);

        startCampaign.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.start){

            String listofSongs = listOfSongs.getText().toString();
            String listOfAlternativeSongs = alternativeSongs.getText().toString();
            String performanceLocation = location.getText().toString();
            String alternativeLocations = alternativeLocation.getText().toString();

            if(listofSongs.isEmpty()){
                listOfSongs.setError("Should not be empty!");
                return;
            }

            if(performanceLocation.isEmpty()){
                location.setError("Should not be empty!");
                return;
            }

            if(timeAndVotes.getCheckedRadioButtonId()==-1){
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Please select your stream time!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            if(oneHour.isChecked()){
                liveTime = "1";
                votesNeeded = "100";
            }

            if(twoHour.isChecked()){
                liveTime = "2";
                votesNeeded = "500";
            }

            if(threeHour.isChecked()){
                liveTime = "3";
                votesNeeded = "1500";
            }
            if(unlimitedTime.isChecked()){
                liveTime = "Unlimited";
                votesNeeded = "5000";
            }

            if(custom.isChecked()){
                votesNeeded = customVotes.getText().toString();
                liveTime = customStreamTime.getText().toString();

                if(votesNeeded.isEmpty()){
                    customVotes.setError("Please input the needed votes");
                    return;
                }

                if(liveTime.isEmpty()){
                    customStreamTime.setError("Please input the stream hour");
                    return;
                }

            }

            setCampaignDataFirebase(listofSongs,listOfAlternativeSongs, performanceLocation, alternativeLocations);

            /*Intent in = new Intent(context, ArtistMainScreenActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);*/

            Fragment frag = new ArtistMainScreenFragment(context);

            if (frag != null) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                transaction.commit(); // commit the changes
            }

        }
    }

    private void setCampaignDataFirebase(String listOfSongs, String alterntiveSongs, String location, String alternativeLocation)
    {
        ArtistCampaignData campaignData = new ArtistCampaignData();
        campaignData.setArtistName(userName);
        campaignData.setArtistPic(artistPic);
        campaignData.setListOfSongs(listOfSongs);
        campaignData.setAlternativeSongs(alterntiveSongs);
        campaignData.setLocation(location);
        campaignData.setAlternativeLocations(alternativeLocation);
        campaignData.setLiveTime(liveTime);
        campaignData.setVotesNeeded(votesNeeded);
        campaignData.setVotesEarned(0);
        campaignData.setPerformanceOneVote(0);
        campaignData.setPerformanceTwoVote(0);
        campaignData.setLocationOneVote(0);
        campaignData.setLocationTwoVote(0);

        ref.setValue(campaignData);
    }
}