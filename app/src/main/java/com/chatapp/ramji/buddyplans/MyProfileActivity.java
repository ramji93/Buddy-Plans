package com.chatapp.ramji.buddyplans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gun0912.tedbottompicker.TedBottomPicker;

public class MyProfileActivity extends BaseActivity {


    private User user;
    @BindView(R.id.userDP)
    CircularImageView profilePhotoView;
    //    @BindView(R.id.username)
//    TextView userNameView;
//    @BindView(R.id.useremail)
//    TextView userEmailView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.divider)
    View dividerview;
    @BindView(R.id.fb_button)
    ImageButton fb_button;
    @BindView(R.id.fb_group)
    LinearLayout fbLayout;
    @BindView(R.id.mail_id)
    TextView mailText;
    @BindView(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorLayout;
    FirebaseDatabase firebaseDatabase;
    TedBottomPicker bottomSheetDialogFragment;
    final int WRITE_REQUEST = 1;
    @BindView(R.id.useredit)
    FloatingActionButton editbutton;
    Uri ProfilePhoto_new = null;
    StorageReference imageStorageReference;
    DatabaseReference userreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        boolean b = getParent() instanceof MainActivity;


        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        user = gson.fromJson(sharedPreferences.getString("User",""),User.class);

        String profile_dp_uri = sharedPreferences.getString("profiledp",user.getProfileDP());
        Glide.with(this).load(profile_dp_uri).into(profilePhotoView);
        profilePhotoView.setActivated(true);


        if(user.getFb_id()!=null)
        {
            dividerview.setVisibility(View.VISIBLE);
            fbLayout.setVisibility(View.VISIBLE);

        }

        imageStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");

        firebaseDatabase = FirebaseDatabase.getInstance();

        userreference = firebaseDatabase.getReference("Users");

        getSupportActionBar().setTitle(user.getUserName());

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mailText.setText(user.geteMail());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();



    }


    @OnClick(R.id.userDP)
    public void setProfilePhoto() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);

            return;

        }

        if (bottomSheetDialogFragment == null) {

            bottomSheetDialogFragment = new TedBottomPicker.Builder(this).setTitle("Choose new Profile Photo")
                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {


                        private boolean stop = false;

                        public boolean isStop() {
                            return stop;
                        }

                        public void setStop(boolean stop) {
                            this.stop = stop;
                        }

                        @Override
                        public void onImageSelected(Uri uri) {



                            ProfilePhoto_new = uri;



                            Glide.with(MyProfileActivity.this).load(ProfilePhoto_new).into(profilePhotoView);
                            editbutton.setVisibility(View.VISIBLE);

                        }


                    }).create();
        }

        bottomSheetDialogFragment.show(getSupportFragmentManager());

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==WRITE_REQUEST)
        {

            if(bottomSheetDialogFragment==null) {

                bottomSheetDialogFragment = new TedBottomPicker.Builder(this).setTitle("Choose new Profile Photo")
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                // here is selected uri
                                ProfilePhoto_new = uri;


                                Glide.with(MyProfileActivity.this).load(ProfilePhoto_new).into(profilePhotoView);
                                editbutton.setVisibility(View.VISIBLE);

                            }
                        })
                        .create();

            }

            bottomSheetDialogFragment.show(getSupportFragmentManager());



        }
    }


    @OnClick(R.id.useredit)
    public void editProfile()
    {

        userreference.child(user.getUid()).child("profileDP").setValue(null);

        SharedPreferences sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(MyProfileActivity.this);

        SharedPreferences.Editor editor =  sharedPreferences.edit();

        editor.putString("profiledp",ProfilePhoto_new.toString());

        editor.commit();

            StorageReference imageRef = imageStorageReference.child(user.getUid()).child("profile");

            imageRef.putFile(ProfilePhoto_new).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //noinspection VisibleForTests
                    userreference.child(user.getUid()).child("profileDP").setValue(taskSnapshot.getDownloadUrl().toString());
                }
            });

//        }

        Toast.makeText(this,"Your Profile is being edited ",Toast.LENGTH_LONG).show();

        finish();

    }

    @OnClick(R.id.fb_group)
    public void viewFbProfile()
    {

        Intent intent = new Intent();

        try {
            getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
            String url = "https://www.facebook.com/"+user.getFb_id();
            intent = new  Intent(Intent.ACTION_VIEW,
//                        Uri.parse("fb://page/"+user.getFb_id())); //Trys to make intent with FB's URI
                    Uri.parse("fb://facewebmodal/f?href="+url));
            intent.setPackage("com.facebook.katana");
        } catch (Exception e) {
            intent = new  Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/"+user.getFb_id())); //catches and opens a url to the desired page
        }


        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

}
