package com.tech.loudcloud.UserPackage.userFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.CheckoutActivity;

public class UserWalletFragment extends Fragment {

    TextView totalVotes;
    Button fiveVotesBtn, tenVotesBtn;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    double total_votes;

    Context context;

    public UserWalletFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_wallet, container, false);

        getActivity().setTitle("Wallet");

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        totalVotes = view.findViewById(R.id.totalVotes);
        fiveVotesBtn = view.findViewById(R.id.fiveVotesBtn);
        tenVotesBtn = view.findViewById(R.id.tenVotesBtn);

        fiveVotesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, CheckoutActivity.class);
                in.putExtra("amount", 5);
                in.putExtra("TotalVotes", total_votes);
                startActivity(in);
            }
        });

        tenVotesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, CheckoutActivity.class);
                in.putExtra("amount",   10);
                in.putExtra("TotalVotes", total_votes);
                startActivity(in);
            }
        });

        getUserVotes();

        return view;
    }

    public void getUserVotes()
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
                            String votes = documentChange.getDocument().getData().get("Votes").toString();

                            if(user.getEmail().equals(userEmail)) {
                                totalVotes.setText(votes);
                                total_votes = Double.valueOf(votes);
                            }
                        }
                    }
                }
            }
        });
    }

}