package com.tech.loudcloud.ArtistPackage.artistFragments;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tech.loudcloud.R;

import java.util.HashMap;
import java.util.Map;

public class ArtistWalletFragment extends Fragment {

    String walletID, fundReq, artistEarnings;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;

    TextView text, earningText, earnings, paymentMethodText, pymentID;
    EditText walletIDEditText;
    Button submit, requestForFundsBTn;

    Context context;

    public ArtistWalletFragment(Context context, String walletID, String fundReq, String artistEarnings) {
       this.context = context;
       this.walletID = walletID;
       this.fundReq = fundReq;
       this.artistEarnings = artistEarnings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_artist_wallet, container, false);

        getActivity().setTitle("Wallet");

        text = view.findViewById(R.id.text);
        earningText = view.findViewById(R.id.earningtext);
        earnings = view.findViewById(R.id.earnings);
        paymentMethodText = view.findViewById(R.id.paymentText);
        pymentID = view.findViewById(R.id.pymentID);

        walletIDEditText = view.findViewById(R.id.id);
        submit = view.findViewById(R.id.submit);
        requestForFundsBTn = view.findViewById(R.id.requestForMoneyBtn);

        if(walletID.equals("")){
            text.setVisibility(View.VISIBLE);
            walletIDEditText.setVisibility(View.VISIBLE);
            submit.setVisibility(View.VISIBLE);
            earningText.setVisibility(View.GONE);
            earnings.setVisibility(View.GONE);
            paymentMethodText.setVisibility(View.GONE);
            pymentID.setVisibility(View.GONE);
            requestForFundsBTn.setVisibility(View.GONE);

        }else{
            text.setVisibility(View.GONE);
            walletIDEditText.setVisibility(View.GONE);
            submit.setVisibility(View.GONE);

            earningText.setVisibility(View.VISIBLE);
            earnings.setVisibility(View.VISIBLE);
            paymentMethodText.setVisibility(View.VISIBLE);
            pymentID.setVisibility(View.VISIBLE);
            requestForFundsBTn.setVisibility(View.VISIBLE);

            pymentID.setText("PayPal ID: "+walletID);
            earnings.setText(artistEarnings+"$");
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wID = walletIDEditText.getText().toString();
                if(wID.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(wID).matches()){
                    walletIDEditText.setError("Please enter correct PayPal ID!");
                    return;
                }
                updateWallet(wID);
            }
        });

        requestForFundsBTn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (artistEarnings.equals("0")) {
                    Toast.makeText(context, "Sorry, you do not having any earnings!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (fundReq.equals("") || fundReq.equals("no")) {

                    String userID = user.getUid();
                    DocumentReference documentReference = firebaseFirestore.collection("artist_data").document(userID);
                    Map<String, Object> userr = new HashMap<>();
                    userr.put("FundRequest", "yes");

                    documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Funds requested!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(context, "Already requested!", Toast.LENGTH_SHORT).show();
                }

            }

        });

        return view;
    }


    public void updateWallet(String walletId){
        String userID = user.getUid();
        DocumentReference documentReference = firebaseFirestore.collection("artist_data").document(userID);
        Map<String , Object> userr = new HashMap<>();
        userr.put("WalletID", walletId);

        documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Wallet ID Added!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}