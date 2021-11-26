package com.tech.loudcloud;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tech.loudcloud.ArtistPackage.ArtistMainScreenActivity;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;

public class MainActivity extends AppCompatActivity {

    public static final String RTMP_BASE_URL = "rtmp://3.236.184.191/LiveApp/";

    private final int SPLASH_DISPLAY_LENGTH = 8000;
    SharedPreferences artistOrUser;
    String authChecker;
    VideoView anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        artistOrUser = getSharedPreferences("artistOrUser",MODE_PRIVATE);
        authChecker = artistOrUser.getString("artistOrUser", "");

        try{
            anim = findViewById(R.id.anim);
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.animm);
            anim.setVideoURI(video);

            anim.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    if (authChecker.equals("user")) {

                                Intent in = new Intent(MainActivity.this, UserMainScreenActivity.class);
                                startActivity(in);
                                finish();

                    } else if (authChecker.equals("artist")) {

                                Intent in = new Intent(MainActivity.this, ArtistMainScreenActivity.class);
                                startActivity(in);
                                finish();


                    }else{
                                Intent in = new Intent(MainActivity.this, MainScreen.class);
                                startActivity(in);
                                finish();
                            }


                }

            });
            anim.start();
        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }
}