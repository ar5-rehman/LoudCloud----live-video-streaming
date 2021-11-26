package com.tech.loudcloud.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ArtistRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    Button next;
    TextInputEditText artistName, email, region, dob, pass, cpass;
    TextInputLayout nameLayout, emailLayout, regionLayout, dobLayout, passLayout, cpassLayout;
    RadioButton male,female;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener birthDate;

    FirebaseFirestore firebaseFirestore;

    String str_name,str_email,str_region, str_dob, str_pass, str_cpass, str_gender;

    private String checkName, checkEmail;
    private String checkUserName, checkUserEmail;
    private boolean nameExist, emailExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseFirestore = FirebaseFirestore.getInstance();

        artistName = findViewById(R.id.name);
        email = findViewById(R.id.email);
        region = findViewById(R.id.region);
        dob = findViewById(R.id.dob);
        dob.setFocusable(false);
        dob.setKeyListener(null);
        pass = findViewById(R.id.pass);
        cpass = findViewById(R.id.cpass);

        nameLayout = findViewById(R.id.userName);
        emailLayout = findViewById(R.id.userEmail);
        regionLayout = findViewById(R.id.userRegion);
        dobLayout = findViewById(R.id.userDob);
        passLayout = findViewById(R.id.userPass);
        cpassLayout = findViewById(R.id.conpass);

        male = findViewById(R.id.radioMale);
        female = findViewById(R.id.radioFemale);

        male.setChecked(true);
        myCalendar = Calendar.getInstance();
        birthDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDOB();
            }
        };

         next = findViewById(R.id.next);
        next.setOnClickListener(this);
        dob.setOnClickListener(this);
        }

        @Override
        public void onClick (View v){
            if (v.getId() == R.id.next) {
                str_name = artistName.getText().toString().trim();
                str_email = email.getText().toString().trim();
                str_region = region.getText().toString().trim();
                str_dob = dob.getText().toString().trim();

                if (male.isChecked()) {
                    str_gender = "male";
                } else if (female.isChecked()) {
                    str_gender = "female";
                }
                str_pass = pass.getText().toString().trim();
                str_cpass = cpass.getText().toString().trim();

                if (TextUtils.isEmpty(str_name)) {
                    nameLayout.setError("Please fill the field!");
                    nameLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }else{
                    nameLayout.setErrorEnabled(false);
                    nameLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (TextUtils.isEmpty(str_email)) {
                    emailLayout.setError("Please fill the field!");
                    emailLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }else{
                    emailLayout.setErrorEnabled(false);
                    emailLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (TextUtils.isEmpty(str_region)) {
                    regionLayout.setError("Please fill the field!");
                    regionLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }
                else{
                    regionLayout.setErrorEnabled(false);
                    regionLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (TextUtils.isEmpty(str_dob)) {
                    dobLayout.setError("Please fill the field!");
                    dobLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }
                else{
                    dobLayout.setErrorEnabled(false);
                    dobLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (TextUtils.isEmpty(str_pass)) {
                    passLayout.setError("Please fill the field!");
                    passLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }
                else{
                    passLayout.setErrorEnabled(false);
                    passLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (TextUtils.isEmpty(str_cpass)) {
                    cpassLayout.setError("Please fill the field!");
                    cpassLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }
                else{
                    cpassLayout.setErrorEnabled(false);
                    cpassLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
                    emailLayout.setError("Please enter a valid e-mail address!");
                    emailLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    return;
                }
                else{
                    emailLayout.setErrorEnabled(false);
                    emailLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (str_pass.length() < 8 && str_cpass.length() < 8) {
                    passLayout.setError("Password must contains 8 alphabet/digit/symbol!");
                    cpassLayout.setError("Password must contains 8 alphabet/digit/symbol!");
                    return;
                }
                else{
                    passLayout.setErrorEnabled(false);
                    passLayout.setBoxStrokeColor(Color.WHITE);
                    cpassLayout.setErrorEnabled(false);
                    cpassLayout.setBoxStrokeColor(Color.WHITE);
                }
                if (!str_pass.equals(str_cpass)) {
                    passLayout.setError("Password mismatched!");
                    cpassLayout.setError("Password mismatched!");
                    return;
                }
                else{
                    passLayout.setErrorEnabled(false);
                    passLayout.setBoxStrokeColor(Color.WHITE);
                    cpassLayout.setErrorEnabled(false);
                    cpassLayout.setBoxStrokeColor(Color.WHITE);
                }

               checkUserNameExistence(str_name, str_email);

            }
            else if (v.getId() == R.id.dob) {
                new DatePickerDialog(ArtistRegisterActivity.this, birthDate,
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        }

    private void updateDOB () {
        String myFormat = "MM-dd-yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dob.setText(sdf.format(myCalendar.getTime()));
    }

    public void checkUserNameExistence(String uname, String uemail)
    {
        firebaseFirestore.collection("artist_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {

                } else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if (documentChange != null) {
                            checkUserName = documentChange.getDocument().getData().get("Name").toString();
                            checkUserEmail = documentChange.getDocument().getData().get("Email").toString();
                            if(uname.equalsIgnoreCase(checkUserName))
                            {
                                checkName = checkUserName;
                            }
                            if(uemail.equals(checkUserEmail))
                            {
                                checkEmail = checkUserEmail;
                            }
                        }
                    }
                    if(!uname.equalsIgnoreCase(checkName) && !uemail.equals(checkEmail))
                    {
                        Intent in = new Intent(ArtistRegisterActivity.this, DetailsArtistProfile.class);
                        in.putExtra("name", str_name);
                        in.putExtra("email", str_email);
                        in.putExtra("region", str_region);
                        in.putExtra("dob", str_dob);
                        in.putExtra("gender", str_gender);
                        in.putExtra("pass", str_pass);
                        in.putExtra("cpass", str_cpass);
                        startActivity(in);

                        artistName.setText("");
                        email.setText("");
                        region.setText("");
                        dob.setText("");
                        dob.setText("");
                        pass.setText("");
                        cpass.setText("");
                    }else{
                        if(uname.equalsIgnoreCase(checkName))
                        {
                            nameLayout.setError("This name is already taken!");
                            nameLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                        }else{
                            nameLayout.setErrorEnabled(false);
                            nameLayout.setBoxStrokeColor(Color.WHITE);
                        }
                        if(uemail.equals(checkEmail))
                        {
                            emailLayout.setError("This Gmail account is already taken!");
                            emailLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                        }else{
                            emailLayout.setErrorEnabled(false);
                            emailLayout.setBoxStrokeColor(Color.WHITE);
                        }
                    }
                }
            }
        });
    }

}