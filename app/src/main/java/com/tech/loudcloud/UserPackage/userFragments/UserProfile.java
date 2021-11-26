package com.tech.loudcloud.UserPackage.userFragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;
import com.tech.loudcloud.login.ArtistLoginActivity;
import com.tech.loudcloud.login.CompleteArtistProfileActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class UserProfile extends Fragment implements View.OnClickListener {

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    String userID;
    StorageReference fireRef;
    StorageReference mStorageRef;
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    Button update;
    TextInputEditText userName, userEmail;
    TextInputLayout nameLayout, emailLayout;

    ImageView profileImage, profileImage2;
    ProgressBar progressBar;

    Bitmap selectedImage;
    InputStream imageStream;
    Uri imageUri;
    String imageURL = "";

    Context context;

    private static final int MY_PERMISSION_STORAGE = 11122;

    public UserProfile(Context context) {
        // Required empty public constructor
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        getActivity().setTitle("Profile");

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        userEmail =  view.findViewById(R.id.email);
        userName = view.findViewById(R.id.name);

        userEmail.setEnabled(false);
        userName.setEnabled(false);

        userName.setFocusable(false);
        userEmail.setFocusable(false);

        nameLayout = view.findViewById(R.id.userName);
        emailLayout = view.findViewById(R.id.userEmail);

        update = view.findViewById(R.id.update);
        update.setOnClickListener(this);

        profileImage = view.findViewById(R.id.insertProfilePic);
        profileImage2 = view.findViewById(R.id.insertProfilePic2);

        profileImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_STORAGE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_STORAGE);

                } else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1) //You can skip this for free form aspect ratio)
                            .start(getActivity());
                }
            }
        });

        progressBar = view.findViewById(R.id.progressbar);

        getUserData();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try{
                    Uri resultUri = result.getUri();
                    imageUri = resultUri;
                    imageStream = getActivity().getContentResolver().openInputStream(resultUri);
                    selectedImage = BitmapFactory.decodeStream(imageStream);
                    profileImage.setImageBitmap(selectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void getUserData()
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e !=null)
                {

                }
                else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if(documentChange!=null) {
                            String uEmail = documentChange.getDocument().getData().get("Email").toString();
                            String uName = documentChange.getDocument().getData().get("Name").toString();
                            String profileImageURL = documentChange.getDocument().getData().get("UserProfileImage").toString();

                            if(user.getEmail().equals(uEmail)) {
                                userName.setText(uName);
                                userEmail.setText(uEmail);
                                if(profileImageURL.equals(" "))
                                {

                                }else {
                                    Glide.with(context.getApplicationContext()).load(profileImageURL).into(profileImage);
                                }

                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        if(profileImage.getDrawable()==null){
            Toast.makeText(getContext(), "Please, select a profile photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        uploadPic();
    }

    public String getDate() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentdate = df.format(Calendar.getInstance().getTime());
        return currentdate;
    }

    public void uploadPic()
    {
        try {

            String strFileName = userName.getText() + ".jpg";

            Uri file = imageUri;

            fireRef = mStorageRef.child("images/" + "user " + user.getUid().toString() + "/" + strFileName);

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
                        DocumentReference documentReference = firebaseFirestore.collection("user_data").document(userID);
                        Map<String , Object> userr = new HashMap<>();

                        userr.put("UserProfileImage", imageURL);

                        documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), "Image upload unsuccessful. Please try again."
                                , Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception ex) {
          //  Toast.makeText(getContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }
}