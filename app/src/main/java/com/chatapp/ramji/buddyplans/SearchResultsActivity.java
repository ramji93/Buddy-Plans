package com.chatapp.ramji.buddyplans;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultsActivity extends BaseActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private Query userQuery;
    private Query userQuery1;
    private ArrayList<User> userList;
    private UserListener userListener;
    @BindView(R.id.usersearchlist) RecyclerView userSearchList;
    @BindView(R.id.search_results_toolbar) Toolbar toolbar;
    HashMap<String,String> hashMap;

    UserSearchAdapter userSearchAdapter;

    User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        ButterKnife.bind(this);
        hashMap = new HashMap<String, String>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference().child("Users");
        userList = new ArrayList<User>();
        userSearchAdapter = new UserSearchAdapter(this);
        userSearchList.setLayoutManager(new LinearLayoutManager(this));
        userSearchList.setAdapter(userSearchAdapter);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(!Util.checkConnection(SearchResultsActivity.this))
        {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.nointernet)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                                     finish();
                        }
                    });

            // Create the AlertDialog object and return it
            builder.create().show();

            return;

        }

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

            if(!hashMap.containsKey(user.getUid())) {

                if (!user.getUid().equalsIgnoreCase(currentUser.getUid())) {
                    userSearchAdapter.userList.add(user);
                    hashMap.put(user.getUid(),user.getUid());
                    userSearchAdapter.notifyDataSetChanged();

                }

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

        String query1 = String.valueOf(query.charAt(0));
        query1 = query1.toUpperCase();
        query1 = query1.concat(query.substring(1));

        userQuery = usersReference.orderByChild("userName").startAt(query).endAt(query+"\uf8ff");

        userQuery1 =  usersReference.orderByChild("userName").startAt(query1).endAt(query+"\uf8ff");



        userListener = new UserListener();



    }

    @Override
    protected void onStart() {
        super.onStart();

        if(userListener!=null) {
            userQuery.addChildEventListener(userListener);
            userQuery1.addChildEventListener(userListener);

        }


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

    @Override
    protected void onStop() {
        super.onStop();
        if(userListener != null) {
            userQuery.removeEventListener(userListener);
            userQuery1.removeEventListener(userListener);
            userListener = null;
        }


    }
}
