package com.tech.loudcloud.ArtistPackage.artistFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tech.loudcloud.R;

public class CampaignFragment extends DialogFragment {

    TextView contract;
    Button agree;
    String userName, artistPic;

    Context context;

    public CampaignFragment(Context context){
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_campaign, container, false);

        userName = getArguments().getString("userName");
        artistPic = getArguments().getString("artistPic");

        contract = view.findViewById(R.id.contract);
        agree = view.findViewById(R.id.agreeBtn);

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StartCampaignFragment startCampaignFragment = new StartCampaignFragment(context);
                FragmentManager manager = getActivity().getSupportFragmentManager();

                Bundle bundle = new Bundle();
                bundle.putString("userName", userName);
                bundle.putString("artistPic",artistPic);
                startCampaignFragment.setArguments(bundle);

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.frame,startCampaignFragment,"startCampaignFragment");
                transaction.addToBackStack(null);
                transaction.commit();

                dismiss();
            }
        });

        contract.setText("I UNDERSTAND THAT THE CREATION OF A CAMPAIGN ON THE LOUD CLOUD PLATFORM IS AN UNEQUIVOCAL ACCEPTANCE OF IT’S TERMS AND AGREEMENTS, BY PROCEEDING I AM AGREEING TO FOLLOW THEM.");
        return view;
    }
}