package com.tech.loudcloud.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.R;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText name,email,pass,conpass;
    TextInputLayout userName, userEmail, userPass, userCpass;
    Button register;
    FirebaseUser user;
    FirebaseAuth auth;
    ProgressBar progressBar;
    String str_name,str_email, str_password, str_confirm_password;

    FirebaseFirestore firebaseFirestore;
    String userID;

    private String checkName;
    private String checkUserName;
    private boolean nameExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPass = findViewById(R.id.userPass);
        userCpass = findViewById(R.id.conpass);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        pass  =findViewById(R.id.pass);
        conpass = findViewById(R.id.cpass);
        register = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressbar);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();

        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        str_name = name.getText().toString().trim();
        str_email = email.getText().toString().trim();
        str_password = pass.getText().toString().trim();
        str_confirm_password = conpass.getText().toString().trim();

        if (TextUtils.isEmpty(str_name)) {
            userName.setError("Please fill the field!");
            userName.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userName.setErrorEnabled(false);
            userName.setBoxStrokeColor(Color.WHITE);
        }
        if (TextUtils.isEmpty(str_email)) {
            userEmail.setError("Please fill the field!");
            userEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userEmail.setErrorEnabled(false);
            userEmail.setBoxStrokeColor(Color.WHITE);
        }
        if (TextUtils.isEmpty(str_password)) {
            userPass.setError("Please fill the field!");
            userPass.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }
        else{
            userPass.setErrorEnabled(false);
            userPass.setBoxStrokeColor(Color.WHITE);
        }
        if (TextUtils.isEmpty(str_confirm_password)) {
            userCpass.setError("Please fill the field!");
            userCpass.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userCpass.setErrorEnabled(false);
            userCpass.setBoxStrokeColor(Color.WHITE);
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
            userEmail.setError("Please enter a valid e-mail address!");
            userEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userEmail.setErrorEnabled(false);
            userEmail.setBoxStrokeColor(Color.WHITE);
        }
        if (str_password.length() < 8 && str_confirm_password.length() < 8) {
            userPass.setError("Password must contains 8 alphabet/digit/symbol!");
            userCpass.setError("Password must contains 8 alphabet/digit/symbol!");
            return;
        }else{
            userCpass.setErrorEnabled(false);
            userCpass.setBoxStrokeColor(Color.WHITE);
        }
        if (!str_password.equals(str_confirm_password)) {
            userPass.setError("Password mismatched!");
            userCpass.setError("Password mismatched!");
            return;
        }
        else{
            userPass.setErrorEnabled(false);
            userPass.setBoxStrokeColor(Color.WHITE);
            userCpass.setErrorEnabled(false);
            userCpass.setBoxStrokeColor(Color.WHITE);
        }

        checkUserNameExistence(str_name);
    }

    public void checkEmailVerification()
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
                    Toast.makeText(UserRegisterActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void checkUserNameExistence(String uname)
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {

                } else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if (documentChange != null) {
                            checkUserName = documentChange.getDocument().getData().get("Name").toString();
                            if(uname.equalsIgnoreCase(checkUserName))
                            {
                                checkName = checkUserName;
                            }
                        }
                    }
                    if(!uname.equalsIgnoreCase(checkName))
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        if (str_password.equals(str_confirm_password)) {
                            auth.createUserWithEmailAndPassword(str_email, str_confirm_password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                userID = auth.getCurrentUser().getUid();
                                                DocumentReference documentReference = firebaseFirestore.collection("user_data").document(userID);
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("Type", "user");
                                                user.put("Name", str_name);
                                                user.put("Email", str_email);
                                                user.put("Password", str_password);
                                                user.put("UserID", userID);
                                                user.put("UserProfileImage", " ");
                                                user.put("Votes", 0);
                                                documentReference.set(user);

                                                Intent intent = new Intent(UserRegisterActivity.this, UserLoginActivity.class);
                                                startActivity(intent);
                                                checkEmailVerification();

                                            } else {
                                                userEmail.setError("This Gmail account is already taken!");
                                                userEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                                               // Toast.makeText(UserRegisterActivity.this, "This Gmail account is already taken!", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });

                        } else {

                        }
                    }else{
                        if(uname.equalsIgnoreCase(checkName)){
                            userName.setError("This name is already taken!");
                            userName.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                        }
                    }
                }
            }
        });
    }

}