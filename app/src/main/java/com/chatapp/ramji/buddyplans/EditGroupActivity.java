package com.chatapp.ramji.buddyplans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
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
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class EditGroupActivity extends AppCompatActivity implements GroupCreateFriendListAdapter.FriendSelectListener, GroupEditMembersAdapter.DeleteSelectedItemListener  {

    @BindView(R.id.toolbar_groupedit)
    Toolbar toolbar;
    @BindView(R.id.groupmembers_list)
    RecyclerView recyclerView1;
    @BindView(R.id.nonmember_friends_list)
    RecyclerView recyclerView2;
    @BindView(R.id.groupedit_photoicon)
    ImageView attachPhotoview;
    @BindView(R.id.groupedit_circularview)
    CircularImageView groupPhoto;
    @BindView(R.id.coordinatorlayout)
    View rootView;
    @BindView(R.id.groupname_edittext)
    EmojiconEditText groupNameText;
    @BindView(R.id.groupedit_smiley)
    ImageView smileyButton;
    @BindView(R.id.edit_button)
    FloatingActionButton ediButton;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference friendsReference;
    DatabaseReference groupChatReference;
    DatabaseReference groupmessageReference;
    DatabaseReference groupMembersReference;
    DatabaseReference groupchatFriendReference;
    StorageReference imageStorageReference;
    FriendsListener friendsListener;
    GroupMemberListener groupMemberListener;
    Query friendsQuery;

    GroupCreateFriendListAdapter friendListAdapter;
    GroupEditMembersAdapter memberListAdapter;
    User currentUser;

    TedBottomPicker bottomSheetDialogFragment;
    final int WRITE_REQUEST = 1;
    Uri ProfilePhoto_old = null;
    Uri ProfilePhoto_new = null;
    EmojIconActions emojIcon;
    Groupheader groupheader;
    String groupName_old = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Group");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        groupheader = (Groupheader) intent.getSerializableExtra("group");

        ProfilePhoto_old = Uri.parse(groupheader.getPhotoUrl());

        Glide.with(this).load(ProfilePhoto_old).into(groupPhoto);

        groupNameText.setText(groupheader.getName());

        groupNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ediButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        groupName_old = groupheader.getName();

        friendListAdapter = new GroupCreateFriendListAdapter(this);
        memberListAdapter = new GroupEditMembersAdapter(this);

        recyclerView1.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
        recyclerView2.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true));



        recyclerView1.setAdapter(memberListAdapter);
        recyclerView2.setAdapter(friendListAdapter);



        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User",""),User.class);
        firebaseDatabase = FirebaseDatabase.getInstance();
        friendsReference = firebaseDatabase.getReference().child("Friends").child(currentUser.getUid());
        groupMembersReference = firebaseDatabase.getReference("GroupMemebers").child(groupheader.getGroupKey());

        imageStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");
        friendsListener = new FriendsListener();
        groupMemberListener = new GroupMemberListener();

        friendsQuery = friendsReference.orderByChild("name");
        friendsQuery.addChildEventListener(friendsListener);

        groupMembersReference.addChildEventListener(groupMemberListener);

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

            bottomSheetDialogFragment = new TedBottomPicker.Builder(this).setTitle("Choose new Group Photo")
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


                            groupPhoto.setVisibility(View.VISIBLE);

                            Glide.with(EditGroupActivity.this).load(ProfilePhoto_new).into(groupPhoto);
                            ediButton.setVisibility(View.VISIBLE);

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

                bottomSheetDialogFragment = new TedBottomPicker.Builder(this).setTitle("Choose new Group Photo")
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                // here is selected uri
                                ProfilePhoto_new = uri;


                                groupPhoto.setVisibility(View.VISIBLE);

                                Glide.with(EditGroupActivity.this).load(ProfilePhoto_new).into(groupPhoto);
                                ediButton.setVisibility(View.VISIBLE);

                            }
                        })
                        .create();

            }

            bottomSheetDialogFragment.show(getSupportFragmentManager());



        }
    }


    @OnClick(R.id.edit_button)
    public void editGroup()
    {
        Boolean photochanged = false;
        Boolean namechanged = false;

        groupmessageReference = firebaseDatabase.getReference().child("Messages").child(groupheader.getChatId());

        if(ProfilePhoto_new != null )
            photochanged = true;

        String groupName_new = groupNameText.getText().toString();

        if(!groupName_new.equalsIgnoreCase(groupName_old))
            namechanged = true;

        groupChatReference = firebaseDatabase.getReference().child("GroupChat");

        if(photochanged)
        {

            imageStorageReference.child(groupheader.getGroupKey()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
//TODO : check if photochanged works or not
                    StorageReference imageRef = imageStorageReference.child(groupheader.getGroupKey());

                    imageRef.putFile(ProfilePhoto_new).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //noinspection VisibleForTests
                            groupChatReference.child(groupheader.getGroupKey()).child("photoUrl").setValue(taskSnapshot.getDownloadUrl().toString());
                        }
                    });

                }
            });

            Message message = new Message(currentUser.getUserName() + " has changed group photo ",null,null,null,null,null);

            String messageKey = groupmessageReference.push().getKey();

            groupmessageReference.child(messageKey).setValue(message);

            groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

        }

        if(namechanged)
        {

            groupChatReference.child(groupheader.getGroupKey()).child("name").setValue(groupName_new);

            Message message = new Message(currentUser.getUserName() + " has changed group name ",null,null,null,null,null);

            String messageKey = groupmessageReference.push().getKey();

            groupmessageReference.child(messageKey).setValue(message);

            groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);
        }


     //   Map<String,Boolean> groupmembers = new HashMap<String, Boolean>();

        for (User user:memberListAdapter.memberlist) {

//            groupmembers.put(user.getUid(),true);

            groupMembersReference.child(user.getUid()).child("current").setValue(true);

        }

        groupMembersReference.child(currentUser.getUid()).child("current").setValue(true);

       // groupmembers.put(currentUser.getUid(),true);

       // groupMembersReference.setValue(groupmembers);

        Toast.makeText(this,"Group is being edited ",Toast.LENGTH_LONG).show();


        onBackPressed();



    }



    //    @Override
//    protected void onStart() {
//        super.onStart();
//
//        friendsQuery = friendsReference.orderByChild("name");
//        friendsQuery.addChildEventListener(friendsListener);
//
//       groupMembersReference.addChildEventListener(groupMemberListener);
//
//
//
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if(friendsListener != null)
//        friendsQuery.removeEventListener(friendsListener);
//
//        if(groupMemberListener != null)
//        groupMembersReference.removeEventListener(groupMemberListener);
//
//
//        friendListAdapter.friendList.clear();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(friendsListener != null)
            friendsQuery.removeEventListener(friendsListener);

        if(groupMemberListener != null)
            groupMembersReference.removeEventListener(groupMemberListener);


        friendListAdapter.friendList.clear();
        memberListAdapter.memberlist.clear();
    }

    @Override
    public void onFriendSelect(Friend friend) {
        User user = new User();
        user.setProfileDP(friend.getPhotourl());
        user.setUid(friend.getUid());
        user.setUserName(friend.getName());
        memberListAdapter.add(user);
        memberListAdapter.notifyDataSetChanged();
        recyclerView1.setVisibility(View.VISIBLE);
        ediButton.setVisibility(View.VISIBLE);

    }

    @Override
    public void ondeleteselecteditem(User user) {

        Friend friend = new Friend();
        friend.setUid(user.getUid());
        friend.setName(user.getUserName());
        friend.setPhotourl(user.getProfileDP());
        friendListAdapter.add(friend);
        friendListAdapter.notifyDataSetChanged();
        recyclerView2.smoothScrollToPosition(friendListAdapter.getItemCount()-1);
        if(memberListAdapter.memberlist.isEmpty())
        {
            recyclerView1.setVisibility(View.GONE);
        }


    }

    class GroupMemberListener implements ChildEventListener{


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            HashMap<String,Boolean> hashMap  = (HashMap<String, Boolean>) dataSnapshot.getValue();

            boolean bool = hashMap.get("current");

            String memberuid = dataSnapshot.getKey();

            if(memberuid != null && bool && !(memberuid.equalsIgnoreCase(currentUser.getUid())) )
            {
                firebaseDatabase.getReference("Users").child(memberuid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       User user =  dataSnapshot.getValue(User.class);
                        // TODO: 23-06-2017

                        memberListAdapter.add(user);
                        memberListAdapter.notifyDataSetChanged();
                        recyclerView1.setVisibility(View.VISIBLE);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


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

    class FriendsListener implements ChildEventListener{


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
           final Friend friend = dataSnapshot.getValue(Friend.class);




              groupchatFriendReference =  firebaseDatabase.getReference("GroupMemebers").child(groupheader.getGroupKey()).child(friend.getUid());
            groupchatFriendReference.addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(DataSnapshot dataSnapshot) {

                     if(!dataSnapshot.exists())
                     {
                         firebaseDatabase.getReference("Users").child(friend.getUid()).child("profileDP").addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(DataSnapshot dataSnapshot) {

                                 friend.setPhotourl((String) dataSnapshot.getValue());
                                 friendListAdapter.add(friend);
                                 friendListAdapter.notifyDataSetChanged();
                                 recyclerView2.smoothScrollToPosition(friendListAdapter.getItemCount()-1);
                             }

                             @Override
                             public void onCancelled(DatabaseError databaseError) {

                             }
                         });

                     }

                     else{

                         HashMap<String,Boolean> hashMap  = (HashMap<String, Boolean>) dataSnapshot.getValue();

                         boolean bool = hashMap.get("current");

                         if(!bool) {

                             firebaseDatabase.getReference("Users").child(friend.getUid()).child("profileDP").addListenerForSingleValueEvent(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(DataSnapshot dataSnapshot) {

                                     friend.setPhotourl((String) dataSnapshot.getValue());
                                     friendListAdapter.add(friend);
                                     friendListAdapter.notifyDataSetChanged();
                                     recyclerView2.smoothScrollToPosition(friendListAdapter.getItemCount() - 1);
                                 }

                                 @Override
                                 public void onCancelled(DatabaseError databaseError) {

                                 }
                             });

                         }


                     }

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
    public void onBackPressed() {
        super.onBackPressed();
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
