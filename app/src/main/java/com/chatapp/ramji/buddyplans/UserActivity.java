package com.chatapp.ramji.buddyplans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserActivity extends BaseActivity {


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
    @BindView(R.id.mail_group)
    LinearLayout mailLayout;
    @BindView(R.id.mail_id)
    TextView mailText;
    @BindView(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.already_exists_frame)
    View constraintLayout;
    @BindView(R.id.useradd)
    FloatingActionButton userAdd;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference friendDatabaseReference1;
    DatabaseReference friendDatabaseReference2;
    User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        user = (User) intent.getSerializableExtra("User");



        Gson gson = new Gson();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User",""),User.class);


        if(user.getFb_id()!=null)
        {
            dividerview.setVisibility(View.VISIBLE);
            fbLayout.setVisibility(View.VISIBLE);


        }

        getSupportActionBar().setTitle(user.getUserName());

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mailText.setText(user.geteMail());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();

        Glide.with(this).load(user.getProfileDP()).into(profilePhotoView);

        //userEmailView.setText(user.geteMail());

        //userNameView.setText(user.getUserName());

        firebaseDatabase.getReference("Friends").child(currentUser.getUid()).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()) {

                   constraintLayout.setVisibility(View.VISIBLE);

               }
               else {

                   userAdd.setVisibility(View.VISIBLE);

               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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



        startActivity(intent);

    }

    @OnClick(R.id.mail_group)
    public void sendMail() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, user.geteMail());
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        startActivity(Intent.createChooser(intent, "Send Email"));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            super.onBackPressed();
        }

        return true;
    }

    @OnClick(R.id.useradd)
    public void addToFriends()
    {


        //1. add new object in friends/user id/


        friendDatabaseReference1 = firebaseDatabase.getReference().child("Friends").child(currentUser.getUid()).child(user.getUid());

        Friend friend1 = new Friend(user.getUserName(),user.getProfileDP(),true,user.getUid());

        friendDatabaseReference1.setValue(friend1);


        //2. add new object in friends/user id2/

           friendDatabaseReference2 = firebaseDatabase.getReference().child("Friends").child(user.getUid()).child(currentUser.getUid());

           Friend friend2 = new Friend(currentUser.getUserName(),currentUser.getProfileDP(),true,currentUser.getUid());

           friendDatabaseReference2.setValue(friend2);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Added to your friends list", Snackbar.LENGTH_LONG);

        snackbar.show();

        finish();


    }





}
