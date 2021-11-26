package com.tech.loudcloud.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.MainScreen;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserLoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 0000;
    TextInputEditText userEmail, userPassword;
    TextInputLayout emailInputLayout, passInputLayout;
    Button login,register;
    TextView forgetPassword, refreshEmailVerificationLink;

    private FirebaseAuth mAuth;
    private LoginButton fb_login;
    public CallbackManager callbackManager;

    private SignInButton signInButton;
    private GoogleSignInOptions gso;
    private GoogleSignInClient signInClient;

    FirebaseAuth auth;
    ProgressBar progressBar;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences.Editor artistOrUser, fbUserData, gogleUserData;

    String email, pass, name, type;
    String str_email = " ", str_pass = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        firebaseFirestore = FirebaseFirestore.getInstance();

        artistOrUser = getSharedPreferences("artistOrUser", MODE_PRIVATE).edit();

        fbUserData = getSharedPreferences("fbUserData", MODE_PRIVATE).edit();

        gogleUserData = getSharedPreferences("gogleUserData", MODE_PRIVATE).edit();

        signInButton = findViewById(R.id.google_sign_in_button);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(this);

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

                getUserEmailForgetPassword(email);
            }
        });

        login.setOnClickListener(this);
        register.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);

        callbackManager = CallbackManager.Factory.create();
        fb_login = findViewById(R.id.fb_login_button);
        fb_login.setReadPermissions("public_profile", "email");

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(UserLoginActivity.this, "Error"+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount signInAcc = task.getResult(ApiException.class);
                firebaseGoogleAuth(signInAcc);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

    }

    private void firebaseGoogleAuth(GoogleSignInAccount signInAcc)
    {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAcc.getIdToken(),null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    artistOrUser.putString("artistOrUser", "user");
                    artistOrUser.putBoolean("gogle", true);
                    artistOrUser.apply();

                    gogleUserData.putString("email", signInAcc.getEmail());
                    gogleUserData.putString("name", signInAcc.getDisplayName());
                    gogleUserData.putString("proPic", signInAcc.getPhotoUrl().toString());
                    gogleUserData.apply();

                    Intent i = new Intent(UserLoginActivity.this, UserMainScreenActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(UserLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.login)
        {
            String email = userEmail.getText().toString().trim();
            String password = userPassword.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
               // userEmail.setError("Please enter a valid e-mail address!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                emailInputLayout.setError("Please enter a valid e-mail address!");

                return;
            }else{
                emailInputLayout.setErrorEnabled(false);
                emailInputLayout.setBoxStrokeColor(Color.WHITE);
            }
             if(TextUtils.isEmpty(email)){
               // userEmail.setError("Please fill the field!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                emailInputLayout.setError("Please fill the field!");
                return;
            }else{
                emailInputLayout.setErrorEnabled(false);
                emailInputLayout.setBoxStrokeColor(Color.WHITE);
            }
            if(TextUtils.isEmpty(password)){
               // userPassword.setError("Please fill the field!");
                passInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                passInputLayout.setError("Please fill the field!");

                return;
            }else{
                passInputLayout.setErrorEnabled(false);
                passInputLayout.setBoxStrokeColor(Color.WHITE);
        }

            getUserDetails(email,password);
            progressBar.setVisibility(View.VISIBLE);

        } else if(v.getId()==R.id.register)
        {
            Intent registerIntent = new Intent(UserLoginActivity.this, UserRegisterActivity.class);
            startActivity(registerIntent);
            userEmail.setText("");
            userPassword.setText("");
        }else if(v.getId()==R.id.google_sign_in_button)
        {
            Intent googlesignIn = signInClient.getSignInIntent();
            startActivityForResult(googlesignIn, REQUEST_CODE);
        }
    }


    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            getFacebookData(token);

                        } else {
                            // If sign-in fails, a message will display to the user.
                            Toast.makeText(UserLoginActivity.this, "Authentication failed. ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getFacebookData(AccessToken token)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                token, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        try {
                            Log.i("Response",response.toString());

                            String email = response.getJSONObject().getString("email");
                            String firstName = response.getJSONObject().getString("first_name");
                            String lastName = response.getJSONObject().getString("last_name");
                            Uri fbProfile = null;
                            if (Profile.getCurrentProfile()!=null)
                            {
                                fbProfile = Profile.getCurrentProfile().getProfilePictureUri(200, 200);
                            }

                            // Sign in success, UI will update with the signed-in user's information
                            artistOrUser.putString("artistOrUser", "user");
                            artistOrUser.putBoolean("fb", true);
                            artistOrUser.apply();

                            fbUserData.putString("email", email);
                            fbUserData.putString("name", firstName+" "+lastName);
                            fbUserData.putString("proPic", fbProfile.toString());
                            fbUserData.apply();

                            Intent registerIntent = new Intent(UserLoginActivity.this, UserMainScreenActivity.class);
                            startActivity(registerIntent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(UserLoginActivity.this, MainScreen.class);
        startActivity(in);
    }

    public void getUserEmailForgetPassword(String email)
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                    Toast.makeText(UserLoginActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(UserLoginActivity.this, "Wrong Email!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void getUserDetails(String uemail, String upass){

        try {
            firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                } else {

                                }
                            }
                        }

                        if (uemail.equals(str_email) && upass.equals(str_pass)) {
                            auth.signInWithEmailAndPassword(uemail, upass)
                                    .addOnCompleteListener(UserLoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                if (auth.getCurrentUser().isEmailVerified()) {
                                                    artistOrUser.putString("artistOrUser", "user");
                                                    artistOrUser.apply();
                                                    Intent intent = new Intent(getApplicationContext(), UserMainScreenActivity.class);
                                                    startActivity(intent);
                                                    userEmail.setText("");
                                                    userPassword.setText("");
                                                } else {
                                                    refreshEmailVerificationLink.setVisibility(View.VISIBLE);
                                                    Toast.makeText(UserLoginActivity.this, "Please verify, check your Gmail account", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {

                                                Toast.makeText(UserLoginActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            emailInputLayout.setError("Wrong Email!");
                            emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                            passInputLayout.setError("Wrong Password!");
                            passInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
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
                    Toast.makeText(UserLoginActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
}