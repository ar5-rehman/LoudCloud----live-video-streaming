package com.tech.loudcloud.ArtistPackage.artistFragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.tech.loudcloud.login.ArtistRegisterActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ArtistProfile extends Fragment implements View.OnClickListener{

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    String userID;
    StorageReference fireRef;
    StorageReference mStorageRef;
    DatabaseReference databaseReference;

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener birthDate;

    Button update;
    TextInputEditText userName, userEmail, userRegion, userDOB, userDesc, fbLinks, instaLinks, tikLinks, yuLinks, otherLinks;
    TextInputLayout nameLayout, emailLayout, userDescription, userReg;

    ImageView profileImage, profileImage2;
    ProgressBar progressBar;

    Bitmap selectedImage;
    InputStream imageStream;
    Uri imageUri;
    String imageURL = "";

    Context context;

    private static final int MY_PERMISSION_STORAGE = 11122;

    public ArtistProfile(Context context) {
        // Required empty public constructor
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_profile, container, false);

        getActivity().setTitle("Profile");

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        userEmail =  view.findViewById(R.id.email);
        userName = view.findViewById(R.id.name);
        userRegion = view.findViewById(R.id.region);
        userDOB = view.findViewById(R.id.dob);
        userDesc = view.findViewById(R.id.description);
        fbLinks = view.findViewById(R.id.fb);
        instaLinks = view.findViewById(R.id.insta);
        yuLinks = view.findViewById(R.id.youtube);
        tikLinks = view.findViewById(R.id.tiktok);
        otherLinks = view.findViewById(R.id.links);

        userDOB.setFocusable(false);
        userDOB.setKeyListener(null);

        userDOB.setOnClickListener(this);

        userDescription = view.findViewById(R.id.userDescription);
        userReg = view.findViewById(R.id.userRegion);

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
        firebaseFirestore.collection("artist_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            String profileImageURL = documentChange.getDocument().getData().get("ArtistProPicUrl").toString();
                            String description = documentChange.getDocument().getData().get("ArtistDescription").toString();
                            String artistFBLinks = documentChange.getDocument().getData().get("ArtistFBLinks").toString();
                            String artistInstaLinks = documentChange.getDocument().getData().get("ArtistInstaLinks").toString();
                            String artistYoutubeLinks = documentChange.getDocument().getData().get("ArtistYoutubeLinks").toString();
                            String artistTiktokLinks = documentChange.getDocument().getData().get("ArtistTiktokLinks").toString();
                            String artistLiveLinks = documentChange.getDocument().getData().get("ArtistLiveLinks").toString();
                            String artistRegion = documentChange.getDocument().getData().get("ArtistRegion").toString();
                            String artistDOB = documentChange.getDocument().getData().get("ArtistDOB").toString();

                            if(user.getEmail().equals(uEmail)) {
                                userName.setText(uName);
                                userEmail.setText(uEmail);

                                if(profileImageURL.equals(" "))
                                {

                                }else {
                                    Glide.with(context.getApplicationContext()).load(profileImageURL).into(profileImage);
                                }

                                userRegion.setText(artistRegion);
                                userDOB.setText(artistDOB);
                                userDesc.setText(description);
                                fbLinks.setText(artistFBLinks);
                                instaLinks.setText(artistInstaLinks);
                                yuLinks.setText(artistYoutubeLinks);
                                tikLinks.setText(artistTiktokLinks);
                                otherLinks.setText(artistLiveLinks);

                            }
                        }
                    }
                }
            }
        });
    }

    private void updateDOB () {
        String myFormat = "MM-dd-yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        userDOB.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.update) {
            if (TextUtils.isEmpty(userDesc.getText())) {
                userDesc.setError("Please fill the field!");
                userDescription.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            } else {
                userDescription.setErrorEnabled(false);
                userDescription.setBoxStrokeColor(Color.WHITE);
            }

            if (userDesc.getText().length() < 50) {
                userDescription.setError("The description must be 50 character long!");
                userDescription.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            } else {
                userDescription.setErrorEnabled(false);
                userDescription.setBoxStrokeColor(Color.WHITE);
            }

            if (TextUtils.isEmpty(userRegion.getText().toString())) {
                userReg.setError("Please fill the field!");
                userReg.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                return;
            } else {
                userReg.setErrorEnabled(false);
                userReg.setBoxStrokeColor(Color.WHITE);
            }

            if (profileImage.getDrawable() == null) {
                Toast.makeText(getContext(), "Please, select a profile photo!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            uploadPic();
            updateData();
        }else if (v.getId() == R.id.dob) {
            new DatePickerDialog(getContext(), birthDate,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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

            String strFileName = userName.getText() + ".jpg";

            Uri file = imageUri;

            fireRef = mStorageRef.child("images/" + "artist " + user.getUid().toString() + "/" + strFileName);

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
                        Map<String , Object> userr = new HashMap<>();

                        userr.put("ArtistProPicUrl", imageURL);

                        documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Image upload unsuccessful. Please try again.", Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception ex) {
           // Toast.makeText(getContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateData()
    {
        try {
            progressBar.setVisibility(View.GONE);
            userID = user.getUid();
            DocumentReference documentReference = firebaseFirestore.collection("artist_data").document(userID);
            Map<String, Object> userr = new HashMap<>();

            userr.put("ArtistDescription", userDesc.getText().toString());
            userr.put("ArtistRegion", userRegion.getText().toString());
            userr.put("ArtistDOB", userDOB.getText().toString());
            userr.put("ArtistFBLinks", fbLinks.getText().toString());
            userr.put("ArtistInstaLinks", instaLinks.getText().toString());
            userr.put("ArtistYoutubeLinks", yuLinks.getText().toString());
            userr.put("ArtistTiktokLinks", tikLinks.getText().toString());
            userr.put("ArtistLiveLinks", otherLinks.getText().toString());

            documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show();
                }
            });
        }catch(Exception ee)
        {

        }

    }
}