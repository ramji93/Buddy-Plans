package com.chatapp.ramji.buddyplans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserActivity extends AppCompatActivity {


    private User user;
    @BindView(R.id.userDP)
    ImageView profilePhotoView;
//    @BindView(R.id.username)
//    TextView userNameView;
//    @BindView(R.id.useremail)
//    TextView userEmailView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference friendDatabaseReference1;
    DatabaseReference friendDatabaseReference2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        user = (User) intent.getSerializableExtra("User");

        getSupportActionBar().setTitle(user.getUserName());

        getSupportActionBar().setDisplayShowTitleEnabled(true);



        ButterKnife.bind(this);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();

        Glide.with(this).load(user.getProfileDP()).into(profilePhotoView);

        //userEmailView.setText(user.geteMail());

        //userNameView.setText(user.getUserName());

    }

    @OnClick(R.id.useradd)
    public void addToFriends()
    {


        //1. add new object in friends/user id/

        User currentUser;

        Gson gson = new Gson();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User",""),User.class);

        friendDatabaseReference1 = firebaseDatabase.getReference().child("Friends").child(currentUser.getUid()).child(user.getUid());

        Friend friend1 = new Friend(user.getUserName(),user.getProfileDP(),true,user.getUid());

        friendDatabaseReference1.setValue(friend1);


        //2. add new object in friends/user id2/

           friendDatabaseReference2 = firebaseDatabase.getReference().child("Friends").child(user.getUid()).child(currentUser.getUid());

           Friend friend2 = new Friend(currentUser.getUserName(),currentUser.getProfileDP(),true,currentUser.getUid());

           friendDatabaseReference2.setValue(friend2);


    }



}
