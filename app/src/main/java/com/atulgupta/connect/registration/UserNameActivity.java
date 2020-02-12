package com.atulgupta.connect.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.atulgupta.connect.MainActivity;
import com.atulgupta.connect.R;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserNameActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private Button removeProfilePicButton, createOrUpdateButton;
    private EditText userName;
    private  ProgressBar progressBar;
    private Uri photoUri;
    public final static String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    private StorageReference storage;
    private FirebaseAuth firebaseAuth;
    private String url = "";
    ////testing1
    FirebaseUser currentUser;
    Uri currentUserPhotoUrl;
    String urltostring;
    private static final String TAG = "UserNameActivity";
    ////
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        init();

        storage= FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        ////testing1


//       if (firebaseAuth.getCurrentUser() != null)
//        {
            Log.e(TAG, "Entered in Profile check section");
            firestore.collection("users").document(firebaseAuth.getCurrentUser().getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().exists() && !task.getResult().contains("profile_URL"))
                            {
                                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                currentUserPhotoUrl = currentUser.getPhotoUrl();
                                urltostring = currentUserPhotoUrl.toString().trim();

                                if (urltostring != null)
                                {
//                                    CheckForProfilePic checkForProfilePic = new CheckForProfilePic(profileImageView);
//                                    checkForProfilePic.execute(urltostring);
                                    Glide.with(UserNameActivity.this).load(urltostring).into(profileImageView);

                                }

                            } else {
                                String error1 = task.getException().getMessage();
                                Toast.makeText(UserNameActivity.this , error1, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
            //return;
    //    }
     ////

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(UserNameActivity.this)
                        .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted())
                        {
                            selectImage();
                        }else {
                            Toast.makeText(UserNameActivity.this, "Please give permissions", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();

            }
        });

        removeProfilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUri = null;
                profileImageView.setImageResource(R.drawable.profileplaceholder);
            }
        });

        createOrUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userName.setError(null);
                if (userName.getText().toString().isEmpty() || userName.getText().toString().length() < 3)
                {
                    userName.setError("Must contain atleast 3 chracters");
                    return;
                }

                if (!userName.getText().toString().matches(USERNAME_PATTERN))
                {
                    userName.setError("Only \"a to z, 0-9, _ and -\" these characters are allowed");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                firestore.collection("users").whereEqualTo("username", userName.getText().toString())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            List<DocumentSnapshot> document = task.getResult().getDocuments();

                            if (document.isEmpty())
                            {
                                upLoadData();
                                return;
                            }else {
                                progressBar.setVisibility(View.INVISIBLE);
                                userName.setError("User name already exist");
                               return;
                            }
                        }else {
                            progressBar.setVisibility(View.INVISIBLE);
                            String error = task.getException().getMessage();
                            Toast.makeText(UserNameActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void init()
    {
        profileImageView = findViewById(R.id.profile_image);
        removeProfilePicButton = findViewById(R.id.remove_button);
        createOrUpdateButton = findViewById(R.id.createaccountbutton);
        userName = findViewById(R.id.username);
        progressBar = findViewById(R.id.progressBar);
    }

    private void selectImage()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityMenuIconColor(getResources().getColor(R.color.colorAccent))

                .setActivityTitle("Profile Photo")
                .setFixAspectRatio(true)
                .setAspectRatio(1,1)
                .start(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                photoUri = result.getUri();

                Glide
                        .with(this)
                        .load(photoUri)
                        .centerCrop()
                        .placeholder(R.drawable.profileplaceholder)
                        .into(profileImageView);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void upLoadData()
    {
        if (photoUri != null)
        {
            //upload profile photo
            final StorageReference ref = storage.child("profile_images/"+firebaseAuth.getCurrentUser().getUid());
            UploadTask uploadTask = ref.putFile(photoUri);



            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        String error = task.getException().getMessage();
                        Toast.makeText(UserNameActivity.this, error, Toast.LENGTH_SHORT).show();

                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            url = uri.toString();
                        }
                    });
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        uploadUserName();
                    } else {
                        // Handle failures
                        progressBar.setVisibility(View.INVISIBLE);
                        String error = task.getException().getMessage();
                        Toast.makeText(UserNameActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            //upload username only
            uploadUserName();
        }
    }

    private void uploadUserName()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("username", userName.getText().toString());
        map.put("profile_URL", url);
        firestore.collection("users").document(firebaseAuth.getCurrentUser().getUid()).update(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Intent mainIntent = new Intent(UserNameActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                    return;
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    String error = task.getException().getMessage();
                    Toast.makeText(UserNameActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    public class CheckForProfilePic extends AsyncTask<String, Void, String>
    {
        CircleImageView profilephoto;
        public CheckForProfilePic(CircleImageView profilephoto) {
            this.profilephoto = profilephoto;
        }

        @Override
        protected String doInBackground(String... strings) {
            String photoURL1 = strings[0];
                //Glide.with(UserNameActivity.this).load(photoURL1).into(profilephoto);
                return photoURL1;
        }

        @Override
        protected void onPostExecute(String photoURL2) {
            Glide.with(UserNameActivity.this).load(photoURL2).into(profilephoto);
        }
    }*/


 }

