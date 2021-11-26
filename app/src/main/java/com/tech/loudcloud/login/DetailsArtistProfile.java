package com.tech.loudcloud.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tech.loudcloud.R;

public class DetailsArtistProfile extends AppCompatActivity implements View.OnClickListener {

    Button next;
    TextInputEditText description, fb, insta, youtube, tiktok, links;
    TextInputLayout userDescription,  fbLinks, instaLinks, youtubeLinks, tiktokLinks, liveLinks;
    String str_des, str_fbLinks, str_yuLinks, str_instaLinks, str_tiktokLinks, str_liveLinks;
    String str_name,str_email,str_region, str_dob, str_pass, str_cpass, str_gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_artist_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        str_name = intent.getStringExtra("name");
        str_email = intent.getStringExtra("email");
        str_region = intent.getStringExtra("region");
        str_dob = intent.getStringExtra("dob");
        str_pass = intent.getStringExtra("pass");
        str_cpass = intent.getStringExtra("cpass");
        str_gender = intent.getStringExtra("gender");

        description = findViewById(R.id.description);
        fb = findViewById(R.id.fb);
        insta = findViewById(R.id.insta);
        youtube = findViewById(R.id.youtube);
        tiktok = findViewById(R.id.tiktok);
        links = findViewById(R.id.links);

        userDescription = findViewById(R.id.userDescription);
        fbLinks = findViewById(R.id.fbLink);
        instaLinks = findViewById(R.id.instaLink);
        youtubeLinks = findViewById(R.id.youtubeLink);
        tiktokLinks = findViewById(R.id.tiktokLink);
        liveLinks = findViewById(R.id.livelinks);

        next = findViewById(R.id.next);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.next)
        {
            str_des = description.getText().toString().trim();
            str_fbLinks = fb.getText().toString().trim();
            str_instaLinks = insta.getText().toString().trim();
            str_tiktokLinks = tiktok.getText().toString().trim();
            str_yuLinks = youtube.getText().toString().trim();
            str_liveLinks = links.getText().toString().trim();

            if(TextUtils.isEmpty(str_des))
            {
                userDescription.setError("Please fill the field!");
                userDescription.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }else{
                userDescription.setErrorEnabled(false);
                userDescription.setBoxStrokeColor(Color.WHITE);
            }
            /*if(TextUtils.isEmpty(str_fbLinks))
            {
                fbLinks.setError("Please fill the field!");
                fbLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }else{

            }
            if(TextUtils.isEmpty(str_yuLinks))
            {
                youtubeLinks.setError("Please fill the field!");
                youtubeLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }else{
                youtubeLinks.setErrorEnabled(false);
                youtubeLinks.setBoxStrokeColor(Color.WHITE);
            }
            if(TextUtils.isEmpty(str_instaLinks))
            {
                instaLinks.setError("Please fill the field!");
                instaLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            else{
                instaLinks.setErrorEnabled(false);
                instaLinks.setBoxStrokeColor(Color.WHITE);
            }
            if(TextUtils.isEmpty(str_tiktokLinks))
            {
                tiktokLinks.setError("Please fill the field!");
                tiktokLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            else{
                tiktokLinks.setErrorEnabled(false);
                tiktokLinks.setBoxStrokeColor(Color.WHITE);
            }
            if(TextUtils.isEmpty(str_liveLinks))
            {
                liveLinks.setError("Please fill the field!");
                liveLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            else{
                liveLinks.setErrorEnabled(false);
                liveLinks.setBoxStrokeColor(Color.WHITE);
            }
*/

/*
            if(!URLUtil.isValidUrl(str_fbLinks))
            {
                fbLinks.setError("Please enter a valid link!");
                fbLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            else{
                fbLinks.setErrorEnabled(false);
                fbLinks.setBoxStrokeColor(Color.WHITE);
            }
            if(!URLUtil.isValidUrl(str_yuLinks))
            {
                youtubeLinks.setError("Please enter a valid link!");
                youtubeLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            else{
                youtubeLinks.setErrorEnabled(false);
                youtubeLinks.setBoxStrokeColor(Color.WHITE);
            }

            if(!URLUtil.isValidUrl(str_instaLinks))
            {
                instaLinks.setError("Please enter a valid link!");
                instaLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            } else{
                instaLinks.setErrorEnabled(false);
                instaLinks.setBoxStrokeColor(Color.WHITE);
            }

            if(!URLUtil.isValidUrl(str_tiktokLinks))
            {
                tiktokLinks.setError("Please enter a valid link!");
                tiktokLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }else{
                tiktokLinks.setErrorEnabled(false);
                tiktokLinks.setBoxStrokeColor(Color.WHITE);
            }

            if(!URLUtil.isValidUrl(str_liveLinks))
            {
                liveLinks.setError("Please enter a valid link!");
                liveLinks.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }else{
                liveLinks.setErrorEnabled(false);
                liveLinks.setBoxStrokeColor(Color.WHITE);
            }*/

            if(str_des.length()<50)
            {
                userDescription.setError("The description must be 50 character long!");
                userDescription.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            }else{
                userDescription.setErrorEnabled(false);
                userDescription.setBoxStrokeColor(Color.WHITE);
            }

            Intent in = new Intent(DetailsArtistProfile.this, CompleteArtistProfileActivity.class);

            in.putExtra("name", str_name);
            in.putExtra("email",str_email);
            in.putExtra("region", str_region);
            in.putExtra("dob", str_dob);
            in.putExtra("gender", str_gender);
            in.putExtra("pass", str_pass);
            in.putExtra("cpass", str_cpass);

            in.putExtra("des", str_des);
            in.putExtra("fbLink", str_fbLinks);
            in.putExtra("instaLink", str_instaLinks);
            in.putExtra("youLinks", str_yuLinks);
            in.putExtra("liveLinks", str_liveLinks);
            in.putExtra("tikLinks", str_tiktokLinks);
            startActivity(in);

            description.setText("");
            fb.setText("");
            insta.setText("");
            youtube.setText("");
            tiktok.setText("");
            links.setText("");
        }
    }
}