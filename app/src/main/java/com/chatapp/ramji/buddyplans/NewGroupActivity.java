package com.chatapp.ramji.buddyplans;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class NewGroupActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference friendsReference;
    DatabaseReference groupChatReference;
    DatabaseReference groupMembersReference;
    @BindView(R.id.newgroup_friends_list)
    RecyclerView friendsList;
    @BindView(R.id.group_name)
    EditText groupName;
    Query friendsQuery;
    FriendsListener friendsListener;
    NewGroupListAdapter newGroupListAdapter;
    @BindView(R.id.toolbar2)
    Toolbar toolbar;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select Your Friends");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User",""),User.class);
        firebaseDatabase = FirebaseDatabase.getInstance();
        friendsReference = firebaseDatabase.getReference().child("Friends").child(currentUser.getUid());
        friendsList.setLayoutManager(new LinearLayoutManager(this));
        friendsListener = new FriendsListener();
        newGroupListAdapter = new NewGroupListAdapter(this);
        friendsList.setAdapter(newGroupListAdapter);
        //setadapter;


    }


    @Override
    protected void onStart() {
        super.onStart();

        friendsQuery = friendsReference.orderByChild("name");
        friendsQuery.addChildEventListener(friendsListener);


    }


    @Override
    protected void onStop() {
        super.onStop();

        if(friendsListener != null) {
            friendsQuery.removeEventListener(friendsListener);
                    }

       // newGroupListAdapter.friendArrayList.clear();

    }

    @OnClick(R.id.create_button)
    public void createAction()
    {

        groupChatReference = firebaseDatabase.getReference().child("GroupChat");
        String groupkey = groupChatReference.push().getKey();
       // groupChatReference.child(groupkey).child("Name").setValue(groupName.getText().toString());

        DatabaseReference ChatReference = firebaseDatabase.getReference().child("Messages");

        String groupNameString = groupName.getText().toString();

        String newChatId = ChatReference.push().getKey();

        groupChatReference.child(groupkey).setValue(new Groupheader(groupNameString,newChatId));

        groupMembersReference = firebaseDatabase.getReference().child("GroupMemebers").child(groupkey);

//        Iterator<Friend> iterator =  newGroupListAdapter.selectedFriendsList.iterator();

        Map<String,Boolean> friendsmap = new HashMap<String, Boolean>();

        for (Friend friend:newGroupListAdapter.selectedFriendsList) {

            friendsmap.put(friend.getUid(),true);

        }

        friendsmap.put(currentUser.getUid(),true);

        groupMembersReference.setValue(friendsmap);


        Toast.makeText(this,"New Group "+ groupNameString +" is created ",Toast.LENGTH_LONG).show();


        finish();
    }



   private class FriendsListener implements ChildEventListener
    {


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Friend friend = (Friend) dataSnapshot.getValue(Friend.class);

            newGroupListAdapter.add(friend);

            newGroupListAdapter.notifyDataSetChanged();

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

}
