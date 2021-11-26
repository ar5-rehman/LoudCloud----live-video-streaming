package com.tech.loudcloud.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.tech.loudcloud.R;

public class UserArtistDialogFragment extends DialogFragment implements View.OnClickListener {

    Button user,artist;

    public UserArtistDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("User or Artist?");
        View view =  inflater.inflate(R.layout.fragment_user_artist_dialog, container, false);

        user = view.findViewById(R.id.user);
        artist =view.findViewById(R.id.artist);

        user.setOnClickListener(this);
        artist.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.user){
            dismiss();
            Intent userIntent = new Intent(getActivity(), UserLoginActivity.class);
            getActivity().startActivity(userIntent);

        }else if(v.getId() == R.id.artist){
            dismiss();
            Intent artistIntent = new Intent(getActivity(), ArtistLoginActivity.class);
            getActivity().startActivity(artistIntent);

        }
    }
}