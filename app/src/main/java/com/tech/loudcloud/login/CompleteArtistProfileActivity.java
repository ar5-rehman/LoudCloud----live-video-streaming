package com.tech.loudcloud.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tech.loudcloud.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CompleteArtistProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMG = 1235;
    private static final int MY_PERMISSION_STORAGE = 1122;

    ImageView profilePic, profilePic2;
    Button siginUp;
    ProgressBar progressBar;

    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String userID;
    StorageReference fireRef;
    private StorageReference mStorageRef;
    DatabaseReference databaseReference;

    Bitmap selectedImage;
    InputStream imageStream;
    Uri imageUri;
    String imageURL = "";

    String str_name,str_email,str_region, str_dob, str_pass, str_cpass, str_gender;
    String str_des, str_fbLinks, str_yuLinks, str_instaLinks, str_tiktokLinks, str_liveLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_artist_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        siginUp = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressbar);

        auth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        str_name = intent.getStringExtra("name");
        str_email = intent.getStringExtra("email");
        str_region = intent.getStringExtra("region");
        str_dob = intent.getStringExtra("dob");
        str_pass = intent.getStringExtra("pass");
        str_cpass = intent.getStringExtra("cpass");
        str_gender = intent.getStringExtra("gender");

        str_des = intent.getStringExtra("des");
        str_fbLinks = intent.getStringExtra("fbLink");
        str_yuLinks = intent.getStringExtra("youLinks");
        str_instaLinks = intent.getStringExtra("instaLink");
        str_tiktokLinks = intent.getStringExtra("tikLinks");
        str_liveLinks = intent.getStringExtra("liveLinks");

        profilePic = findViewById(R.id.insertProfilePic);
        profilePic2 = findViewById(R.id.insertProfilePic2);

        profilePic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CompleteArtistProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(CompleteArtistProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CompleteArtistProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_STORAGE);
                    ActivityCompat.requestPermissions(CompleteArtistProfileActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_STORAGE);

                } else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1) //You can skip this for free form aspect ratio)
                            .start(CompleteArtistProfileActivity.this);
                }
            }
        });

        siginUp.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try{
                Uri resultUri = result.getUri();
                imageUri = resultUri;
                imageStream = getContentResolver().openInputStream(resultUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                profilePic.setImageBitmap(selectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(CompleteArtistProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public String getDate() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentdate = df.format(Calendar.getInstance().getTime());
        return currentdate;
    }

    public void uploadPic()
    {
        try {
            String strFileName = str_name + ".jpg";

            Uri file = imageUri;

            user = auth.getCurrentUser();
            if(user!=null) {

                mStorageRef = FirebaseStorage.getInstance().getReference();
                databaseReference = FirebaseDatabase.getInstance().getReference();

                fireRef = mStorageRef.child("images/" + "artist " +user.getUid().toString() + "/" + strFileName);
            }else{
                Toast.makeText(this, "User not found yet!", Toast.LENGTH_SHORT).show();
            }

            UploadTask uploadTask = fireRef.putFile(file);
            Log.e("Fire Path", fireRef.toString());
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fireRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Uri downloadUri = task.getResult();
                        Log.e("Image URL", downloadUri.toString());
                        imageURL = downloadUri.toString();
                        selectedImage = null;

                        userID = user.getUid();
                        DocumentReference documentReference = firebaseFirestore.collection("artist_data").document(userID);
                        Map<String , Object> user = new HashMap<>();

                        user.put("ArtistID", userID);
                        user.put("Approval", "false");
                        user.put("Type", "artist");
                        user.put("Name", str_name);
                        user.put("Email", str_email);
                        user.put("Password", str_pass);
                        user.put("ArtistRegion", str_region);
                        user.put("ArtistDOB", str_dob);
                        user.put("ArtistGender", str_gender);

                        user.put("ArtistDescription", str_des);
                        user.put("ArtistFBLinks", str_fbLinks);
                        user.put("ArtistInstaLinks", str_instaLinks);
                        user.put("ArtistYoutubeLinks", str_yuLinks);
                        user.put("ArtistTiktokLinks", str_tiktokLinks);
                        user.put("ArtistLiveLinks", str_liveLinks);
                        user.put("ArtistProPicUrl", imageURL);
                        user.put("WalletID", "");
                        user.put("Earnings", "");
                        user.put("FundRequest", "no");

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CompleteArtistProfileActivity.this, "Data Saved", Toast.LENGTH_SHORT).show();
                            }
                        });
                        checkEmailVerification();
                        Intent intent = new Intent(CompleteArtistProfileActivity.this, ArtistLoginActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(CompleteArtistProfileActivity.this, "Image upload unsuccessful. Please try again."
                                , Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(CompleteArtistProfileActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.signup){

            if(profilePic.getDrawable()==null){
                Toast.makeText(this, "Please, select a profile photo!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            auth.createUserWithEmailAndPassword(str_email, str_pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                uploadPic();
                            }
                            else
                            {
                                Toast.makeText(CompleteArtistProfileActivity.this, "This Gmail account is already taken!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    public void checkEmailVerification()
    {
        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(CompleteArtistProfileActivity.this, "Please check your gmail account for verification and login once!", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(CompleteArtistProfileActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

}