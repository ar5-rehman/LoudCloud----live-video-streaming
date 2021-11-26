package com.tech.loudcloud.UserPackage.userFragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.R;

import java.util.HashMap;
import java.util.Map;

public class VoteCastFragment extends DialogFragment {

    Button yes, no;
    TextView locationText, performanceText, locationOneText, locationTwoText, performanceOneText, performanceTwoText;
    RadioGroup locationGroup, performanceGroup, defaultGroup;
    RadioButton performanceOne, performanceTwo, locationOne, locationTwo, campaign;

    String locationCheck = "", performanceCheck = "";

    FirebaseDatabase database;
    DatabaseReference mDatabaseRef;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    String artistName, location, alternativeLocation, performance, alternativePerformance;
    private Double total_votes, cast_vote;
    Context context;

    public VoteCastFragment(Context context) {
        // Required empty public constructor
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vote_cast, container, false);

        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        artistName = getArguments().getString("artistName");
        location = getArguments().getString("location");
        alternativeLocation = getArguments().getString("alternativeLocation");
        performance = getArguments().getString("performance");
        alternativePerformance = getArguments().getString("alternativePerformance");

        yes = view.findViewById(R.id.yes);
        no = view.findViewById(R.id.no);

        locationGroup = view.findViewById(R.id.locationGroup);
        performanceGroup = view.findViewById(R.id.performanceGroup);
        defaultGroup = view.findViewById(R.id.defaultGroup);

        locationOne = view.findViewById(R.id.locationOne);
        locationTwo = view.findViewById(R.id.locationTwo);
        performanceOne = view.findViewById(R.id.performanceOne);
        performanceTwo = view.findViewById(R.id.performanceTwo);

        locationText = view.findViewById(R.id.locationText);
        performanceText = view.findViewById(R.id.performanceText);

        locationOneText = view.findViewById(R.id.locationOneText);
        locationTwoText = view.findViewById(R.id.locationTwoText);

        performanceOneText = view.findViewById(R.id.performanceOneText);
        performanceTwoText = view.findViewById(R.id.performanceTwoText);

        campaign = view.findViewById(R.id.campaign);

        locationOneText.setText(location);
        locationTwoText.setText(alternativeLocation);
        performanceOneText.setText(performance);
        performanceTwoText.setText(alternativePerformance);

        if(alternativeLocation.equals("")){
            locationText.setVisibility(View.GONE);
            locationGroup.setVisibility(View.GONE);

            locationOneText.setVisibility(View.GONE);
            locationTwoText.setVisibility(View.GONE);
        }

        if(alternativePerformance.equals("")){
            performanceText.setVisibility(View.GONE);
            performanceGroup.setVisibility(View.GONE);

            performanceOneText.setVisibility(View.GONE);
            performanceTwoText.setVisibility(View.GONE);
        }

        if(alternativePerformance.equals("") && alternativeLocation.equals(""))
        {
            defaultGroup.setVisibility(View.GONE);
        }

        defaultGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                    performanceCheck = "";
                    locationCheck = "";
                    performanceOne.setChecked(false);
                    performanceTwo.setChecked(false);
                    locationOne.setChecked(false);
                    locationTwo.setChecked(false);

            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(performanceOne.isChecked()){
                    performanceCheck = "one";
                }else if(performanceTwo.isChecked()){
                    performanceCheck = "two";
                }

                if(locationOne.isChecked()){
                    locationCheck = "one";
                }else if(locationTwo.isChecked()){
                    locationCheck = "two";
                }

                if(campaign.isChecked()){
                    performanceOne.setChecked(false);
                    performanceTwo.setChecked(false);
                    locationOne.setChecked(false);
                    locationTwo.setChecked(false);
                }

                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            mDatabaseRef.child(artistName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dsp) {
                                    if(dataSnapshot.child(artistName).child("listOfSongs").exists()) {

                                        int castNoOfVotes = 0;

                                        if(performanceCheck.equals("one")){

                                            long performanceOneVote = (long) dataSnapshot.child(artistName).child("performanceOneVote").getValue();
                                            long performanceOneVoteFinal = performanceOneVote+1;
                                            mDatabaseRef.child(artistName).child("performanceOneVote").setValue(performanceOneVoteFinal);

                                            castNoOfVotes = castNoOfVotes + 1;

                                        }else if(performanceCheck.equals("two")){

                                            long performanceTwoVote = (long) dataSnapshot.child(artistName).child("performanceTwoVote").getValue();
                                            long performanceTwoVoteFinal = performanceTwoVote+1;
                                            mDatabaseRef.child(artistName).child("performanceTwoVote").setValue(performanceTwoVoteFinal);
                                            castNoOfVotes = castNoOfVotes + 1;

                                        }

                                        if (locationCheck.equals("one")){

                                            long locationOneVote = (long) dataSnapshot.child(artistName).child("locationOneVote").getValue();
                                            long locationOneVoteFinal = locationOneVote+1;
                                            mDatabaseRef.child(artistName).child("locationOneVote").setValue(locationOneVoteFinal);
                                            castNoOfVotes = castNoOfVotes + 1;

                                        }else if(locationCheck.equals("two")){

                                            long locationTwoVote = (long) dataSnapshot.child(artistName).child("locationTwoVote").getValue();
                                            long locationTwoVoteFinal = locationTwoVote+1;
                                            mDatabaseRef.child(artistName).child("locationTwoVote").setValue(locationTwoVoteFinal);
                                            castNoOfVotes = castNoOfVotes + 1;

                                        }

                                        long votes = (long) dataSnapshot.child(artistName).child("votesEarned").getValue();
                                        long finalVote = votes+castNoOfVotes+1;
                                        mDatabaseRef.child(artistName).child("votesEarned").setValue(finalVote);

                                        minusVoteFromUserAccount(castNoOfVotes);

                                        Toast.makeText(context, "Vote casted successfully!", Toast.LENGTH_SHORT).show();

                                        dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public void minusVoteFromUserAccount(int votesMinus){
        DocumentReference docRef = firebaseFirestore.collection("user_data").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                       // Log.d("TAG", document.getString("Votes")); //Print the name

                        String email = document.getString("Email");
                        double votes = Double.valueOf(document.getLong("Votes"));

                        if(user.getEmail().equals(email)) {
                            cast_vote = votes-votesMinus;
                            updateVotes(cast_vote);
                        }

                    } else {

                    }
                } else {

                }
            }
        });
    }

    public void updateVotes(double votes){
        String userID = user.getUid();
        DocumentReference documentReference = firebaseFirestore.collection("user_data").document(userID);
        Map<String , Object> userr = new HashMap<>();
        userr.put("Votes", votes);

        documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(getContext(), "Congrats!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}