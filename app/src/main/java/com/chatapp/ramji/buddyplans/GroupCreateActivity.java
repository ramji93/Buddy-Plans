package com.chatapp.ramji.buddyplans;

import android.*;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class GroupCreateActivity extends AppCompatActivity implements GroupCreateFriendListAdapter.FriendSelectListener, GroupCreateSelectedListAdapter.DeleteSelectedItemListener {

    @BindView(R.id.toolbar_groupcreate)
    Toolbar toolbar;
    @BindView(R.id.selected_friends)
    RecyclerView recyclerView1;
    @BindView(R.id.group_create_friends_list)
    RecyclerView recyclerView2;
    @BindView(R.id.groupcreate_photoicon)
    ImageView attachPhotoview;
    @BindView(R.id.groupcreate_circularview)
    CircularImageView groupPhoto;
    @BindView(R.id.coordinatorlayout)
    View rootView;
    @BindView(R.id.groupname_edittext)
    EmojiconEditText groupNameText;
    @BindView(R.id.groupcreate_smiley)
    ImageView smileyButton;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference friendsReference;
    DatabaseReference groupChatReference;
    DatabaseReference groupMembersReference;
    StorageReference imageStorageReference;
    FriendsListener friendsListener;
    Query friendsQuery;

    ArrayList<Friend> selectedFriends;
    GroupCreateFriendListAdapter friendListAdapter;
    GroupCreateSelectedListAdapter selectedListAdapter;
    User currentUser;

    TedBottomPicker bottomSheetDialogFragment;
    final int WRITE_REQUEST = 1;
    Uri selectedProfilePhoto = null;

    EmojIconActions emojIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create New Group");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
        recyclerView2.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true));
        friendListAdapter = new GroupCreateFriendListAdapter(this);
        recyclerView2.setAdapter(friendListAdapter);
        selectedListAdapter = new GroupCreateSelectedListAdapter(this);
        recyclerView1.setAdapter(selectedListAdapter);
        selectedFriends = new ArrayList<Friend>();
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User",""),User.class);
        firebaseDatabase = FirebaseDatabase.getInstance();
        friendsReference = firebaseDatabase.getReference().child("Friends").child(currentUser.getUid());
        imageStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");
        friendsListener = new FriendsListener();
        friendsQuery = friendsReference.orderByChild("name");
        friendsQuery.addChildEventListener(friendsListener);

        emojIcon = new EmojIconActions(this, rootView, groupNameText, smileyButton);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_keyboard, R.drawable.ic_smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e(GroupChatActivity.class.getSimpleName(), "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e(GroupChatActivity.class.getSimpleName(), "Keyboard closed");
            }
        });


    }


     @OnClick(R.id.photoframe)
    public void setGroupPhoto() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);

            return;

        }

        if (bottomSheetDialogFragment == null) {

            bottomSheetDialogFragment = new TedBottomPicker.Builder(this)
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

                             selectedProfilePhoto = uri;

                             attachPhotoview.setVisibility(View.GONE);
                             groupPhoto.setVisibility(View.VISIBLE);

                            Glide.with(GroupCreateActivity.this).load(selectedProfilePhoto).into(groupPhoto);

                        }


                    }).create();
        }

        bottomSheetDialogFragment.show(getSupportFragmentManager());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==WRITE_REQUEST)
        {

            if(bottomSheetDialogFragment==null) {

                bottomSheetDialogFragment = new TedBottomPicker.Builder(this)
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                // here is selected uri
                                selectedProfilePhoto = uri;

                                attachPhotoview.setVisibility(View.GONE);
                                groupPhoto.setVisibility(View.VISIBLE);

                                Glide.with(GroupCreateActivity.this).load(selectedProfilePhoto).into(groupPhoto);

                            }
                        })
                        .create();

            }

            bottomSheetDialogFragment.show(getSupportFragmentManager());



        }
    }

    @OnClick(R.id.create_button)
    public void createGroup()
    {

        if(selectedProfilePhoto == null)
        {
            Snackbar snackbar = Snackbar.make(rootView, "Please select a Group photo !", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }


        groupChatReference = firebaseDatabase.getReference().child("GroupChat");
        final String groupkey = groupChatReference.push().getKey();
        // groupChatReference.child(groupkey).child("Name").setValue(groupName.getText().toString());

        DatabaseReference ChatReference = firebaseDatabase.getReference().child("Messages");

        final String groupNameString = groupNameText.getText().toString();

        final String newChatId = ChatReference.push().getKey();

        StorageReference imageRef = imageStorageReference.child(groupkey).child(selectedProfilePhoto.getLastPathSegment());

        imageRef.putFile(selectedProfilePhoto).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //noinspection VisibleForTests
                groupChatReference.child(groupkey).setValue(new Groupheader(groupNameString,newChatId,taskSnapshot.getDownloadUrl().toString()));
            }
        });


        groupMembersReference = firebaseDatabase.getReference().child("GroupMemebers").child(groupkey);

//        Iterator<Friend> iterator =  newGroupListAdapter.selectedFriendsList.iterator();

        Map<String,Boolean> friendsmap = new HashMap<String, Boolean>();

        for (Friend friend:selectedListAdapter.selectedlist) {

           // friendsmap.put(friend.getUid(),true);

            groupMembersReference.child(friend.getUid()).child("current").setValue(true);

        }

        groupMembersReference.child(currentUser.getUid()).child("current").setValue(true);

      //  friendsmap.put(currentUser.getUid(),true);

      //  groupMembersReference.setValue(friendsmap);


        Toast.makeText(this,"New Group "+ groupNameString +" is created ",Toast.LENGTH_LONG).show();


        finish();


    }



//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        friendsQuery = friendsReference.orderByChild("name");
//        friendsQuery.addChildEventListener(friendsListener);
//
//
//    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if(friendsListener != null) {
//            friendsQuery.removeEventListener(friendsListener);
//        }
//
//        friendListAdapter.friendList.clear();
//
//
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(friendsListener != null) {
            friendsQuery.removeEventListener(friendsListener);
        }

        friendListAdapter.friendList.clear();

    }


    private class FriendsListener implements ChildEventListener
    {


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final Friend friend = (Friend) dataSnapshot.getValue(Friend.class);

            firebaseDatabase.getReference("Users").child(friend.getUid()).child("profileDP").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    friend.setPhotourl((String) dataSnapshot.getValue());
                    friendListAdapter.add(friend);

                    friendListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



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


    @Override
    public void onFriendSelect(Friend friend) {

        selectedListAdapter.selectedlist.add(friend);
        selectedListAdapter.notifyDataSetChanged();
        recyclerView1.setVisibility(View.VISIBLE);

    }

    @Override
    public void ondeleteselecteditem(Friend friend) {

        friendListAdapter.friendList.add(friend);
        friendListAdapter.notifyDataSetChanged();
        if(selectedListAdapter.selectedlist.isEmpty())
           recyclerView1.setVisibility(View.GONE);

    }
}
