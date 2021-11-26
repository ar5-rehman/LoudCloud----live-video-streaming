package com.tech.loudcloud.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.ArtistPackage.ArtistMainScreenActivity;
import com.tech.loudcloud.MainScreen;
import com.tech.loudcloud.R;

public class ArtistLoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 0000;
    TextInputEditText userEmail, userPassword;
    TextInputLayout emailInputLayout, passInputLayout;
    Button login,register;
    TextView forgetPassword, refreshEmailVerificationLink;

    private FirebaseAuth mAuth;
    FirebaseAuth auth;
    ProgressBar progressBar;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    String email, pass, name, type;
    public boolean authChecker =  false;
    String str_email = " ", str_pass = " ";

    SharedPreferences.Editor artistOrUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();

        artistOrUser = getSharedPreferences("artistOrUser", MODE_PRIVATE).edit();

        if (user != null ) {

                if(user.isEmailVerified())
                {
                    Intent i = new Intent(ArtistLoginActivity.this, ArtistMainScreenActivity.class);
                    startActivity(i);
                }else{

                }

        } else {
            // User is signed out
        }

        userEmail = findViewById(R.id.user_email);
        userPassword = findViewById(R.id.user_pass);

        emailInputLayout = findViewById(R.id.userEmail);
        passInputLayout = findViewById(R.id.userpass);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        refreshEmailVerificationLink = findViewById(R.id.refreshToken);
        refreshEmailVerificationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshEmailVerificationLink();
            }
        });

        forgetPassword = findViewById(R.id.userForgetPass);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    // userEmail.setError("Please enter a valid e-mail address!");
                    emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    emailInputLayout.setError("Please enter a valid e-mail address!");

                    return;
                }
                if (TextUtils.isEmpty(email)){
                    // userEmail.setError("Please fill the field!");
                    emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    emailInputLayout.setError("Please fill the field!");
                    return;
                }

                getArtistEmailForgetPassword(email);
            }
        });

        login.setOnClickListener(this);
        register.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);



    }

    @Override
    protected void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE)
        {

        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.login)
        {
            String Email = userEmail.getText().toString().trim();
            String Password = userPassword.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
            {
                // userEmail.setError("Please enter a valid e-mail address!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                emailInputLayout.setError("Please enter a valid e-mail address!");

                return;
            }else{
                emailInputLayout.setErrorEnabled(false);
                emailInputLayout.setBoxStrokeColor(Color.WHITE);

            }
            if(TextUtils.isEmpty(Email)){
                // userEmail.setError("Please fill the field!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                emailInputLayout.setError("Please fill the field!");
                return;
            }else{
                emailInputLayout.setErrorEnabled(false);
                emailInputLayout.setBoxStrokeColor(Color.WHITE);
            }
            if (TextUtils.isEmpty(Password)){
                // userPassword.setError("Please fill the field!");
                passInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                passInputLayout.setError("Please fill the field!");

                return;
            }else{
                passInputLayout.setErrorEnabled(false);
                passInputLayout.setBoxStrokeColor(Color.WHITE);
            }
            progressBar.setVisibility(View.VISIBLE);
            getArtistDetails(Email,Password);

        } else if(v.getId()==R.id.register)
        {
            Intent registerIntent = new Intent(ArtistLoginActivity.this, ArtistRegisterActivity.class);
            startActivity(registerIntent);
            userEmail.setText("");
            userPassword.setText("");
        }
    }

    public void getArtistEmailForgetPassword(String email)
    {
        firebaseFirestore.collection("artist_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                String uemail = null, stremail = null;
                if (e != null) {

                } else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if (documentChange != null) {
                            uemail = documentChange.getDocument().getData().get("Email").toString();
                            if (email.equals(uemail)) {
                                stremail = uemail;
                            } else {

                            }
                        }
                    }

                    if (email.equals(stremail)) {
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ArtistLoginActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ArtistLoginActivity.this, "Wrong Email!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(ArtistLoginActivity.this, MainScreen.class);
        startActivity(in);
    }

    public void getArtistDetails(String uemail, String upass){
        firebaseFirestore.collection("artist_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {

                } else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if (documentChange != null) {
                            email = documentChange.getDocument().getData().get("Email").toString();
                            name = documentChange.getDocument().getData().get("Name").toString();
                            type = documentChange.getDocument().getData().get("Type").toString();
                            pass = documentChange.getDocument().getData().get("Password").toString();

                            if (uemail.equals(email) && upass.equals(pass)) {
                                str_email = email;
                                str_pass = upass;
                            }else{

                            }
                        }
                    }

                    if (uemail.equals(str_email) && upass.equals(str_pass)) {
                        auth.signInWithEmailAndPassword(uemail, upass)
                                .addOnCompleteListener(ArtistLoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            if (auth.getCurrentUser().isEmailVerified()) {
                                                artistOrUser.putString("artistOrUser", "artist");
                                                artistOrUser.apply();
                                                Intent intent = new Intent(getApplicationContext(), ArtistMainScreenActivity.class);
                                                startActivity(intent);
                                                userEmail.setText("");
                                                userPassword.setText("");
                                            } else {
                                                refreshEmailVerificationLink.setVisibility(View.VISIBLE);
                                                Toast.makeText(ArtistLoginActivity.this, "Please verify, check your Gmail account", Toast.LENGTH_SHORT).show();
                                            }

                                        } else {

                                            Toast.makeText(ArtistLoginActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        emailInputLayout.setError("Wrong Email!");
                        emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                        passInputLayout.setError("Wrong Password!");
                        passInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void refreshEmailVerificationLink()
    {
        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Please check your Gmail account for verification and login once!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ArtistLoginActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
}