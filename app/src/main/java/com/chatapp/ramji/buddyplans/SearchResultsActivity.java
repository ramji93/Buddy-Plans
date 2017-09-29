package com.chatapp.ramji.buddyplans;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultsActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private Query userQuery;
    private ArrayList<User> userList;
    private UserListener userListener;
    @BindView(R.id.usersearchlist) RecyclerView userSearchList;
    @BindView(R.id.search_results_toolbar) Toolbar toolbar;

    UserSearchAdapter userSearchAdapter;

    User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        ButterKnife.bind(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference().child("Users");
        userList = new ArrayList<User>();
        userSearchAdapter = new UserSearchAdapter(this);
        userSearchList.setLayoutManager(new LinearLayoutManager(this));
        userSearchList.setAdapter(userSearchAdapter);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            displayResults(query);
        }



        Gson gson = new Gson();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User",""),User.class);

    }


    public class UserListener implements ChildEventListener {


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            User user = dataSnapshot.getValue(User.class);

            if(!user.getUid().equalsIgnoreCase(currentUser.getUid())) {
                userSearchAdapter.userList.add(user);
                userSearchAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    public void displayResults(String query)
    {


        userQuery = usersReference.orderByChild("userName").startAt(query) ;

         userListener = new UserListener();


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(userListener!=null)
        userQuery.addChildEventListener(userListener);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(userListener != null) {
            userQuery.removeEventListener(userListener);
            userListener = null;
        }


    }
}
