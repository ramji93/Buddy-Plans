package com.chatapp.ramji.buddyplans;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class GroupChatActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {


    FirebaseDatabase firebaseDatabase;
    DatabaseReference groupmessageReference;
    FirebaseStorage firebaseStorage;
    StorageReference imageStorageReference;

    String groupChatId;
    @BindView(R.id.groupchat_messages)
    RecyclerView groupMessagesView;
    @BindView(R.id.group_message_input)
    EmojiconEditText groupMessageText;
    @BindView(R.id.groupsend_button)
    ImageButton groupSendButton;
    @BindView(R.id.smiley_button)
    ImageView smileyButton;
    @BindView(R.id.groupchat_toolbar)
    Toolbar groupChatToolbar;
    @BindView(R.id.activity_chat_group)
    View rootView;
    @BindView(R.id.attachPhoto)
    FloatingActionButton photoAttachButton;
    GroupChatMessageListener groupChatMessageListener;
    Messages_Adapter messages_adapter;
    User currentUser;
    String myName;
    Groupheader groupheader;
    EmojIconActions emojIcon;
    TedBottomPicker bottomSheetDialogFragment;
    final int WRITE_REQUEST = 1;
    HandlerThread handlerThread;
    Handler mhandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        groupheader = (Groupheader) intent.getSerializableExtra("group");

        groupChatId = groupheader.getChatId();


        setSupportActionBar(groupChatToolbar);

        getSupportActionBar().setTitle(groupheader.getName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);
        myName = currentUser.getUserName();

        firebaseDatabase = FirebaseDatabase.getInstance();

        emojIcon = new EmojIconActions(this, rootView, groupMessageText, smileyButton);
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


        messages_adapter = new Messages_Adapter(this,myName);
        groupMessagesView.setLayoutManager(new LinearLayoutManager(this){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                super.smoothScrollToPosition(recyclerView, state, position);
            }
        });

        groupMessagesView.setAdapter(messages_adapter);

        groupmessageReference = firebaseDatabase.getReference().child("Messages").child(groupChatId);

        firebaseStorage = FirebaseStorage.getInstance();

        imageStorageReference = firebaseStorage.getReference().child("chat_photos");

        handlerThread = new HandlerThread("myhandlerThread");

        handlerThread.start();

        mhandler = new Handler(handlerThread.getLooper());

    }


    @OnClick(R.id.attachPhoto)
    public void attachPhoto()
    {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_REQUEST);

            return;

        }

        if(bottomSheetDialogFragment==null) {

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
                        public void onImageSelected(final Uri uri) {
                            // here is selected uri

                             boolean stop = false;

                              StorageReference imageRef = imageStorageReference.child(uri.getLastPathSegment());

                            final Snackbar snackbar = Snackbar.make(rootView,"Uploading the image",Snackbar.LENGTH_LONG);
                            snackbar.show();

                                imageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                        Message message = new Message(null, myName, taskSnapshot.getDownloadUrl().toString(),uri.getLastPathSegment());

                                        if (currentUser.getProfileDP() != null)
                                            message.setPhotoUrl(currentUser.getProfileDP());

                                        String messageKey = groupmessageReference.push().getKey();

                                        groupmessageReference.child(messageKey).setValue(message);

                                        groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                                        groupMessageText.setText("");

                                        firebaseDatabase.getReference("/GroupChat/" + groupheader.getGroupKey()).child("lastMessage").setValue(message.getText());

                                        firebaseDatabase.getReference("/GroupChat/" + groupheader.getGroupKey()).child("lastMessageTimestap").setValue(message.getTimeStamp());

                                        Snackbar snackbar2 = Snackbar.make(rootView, "Uploading done !", Snackbar.LENGTH_SHORT);
                                        snackbar2.show();

                                    }
                                });





//                            if(currentUser.getProfileDP()!=null)
//                                message.setPhotoUrl(currentUser.getProfileDP());
//
//                            String messageKey = groupmessageReference.push().getKey();
//
//                            groupmessageReference.child(messageKey).setValue(message);
//
//                            groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);
//
//                            groupMessageText.setText("");
//
//                            firebaseDatabase.getReference("/GroupChat/"+groupheader.getGroupKey()).child("lastMessage").setValue(message.getText());
//
//                            firebaseDatabase.getReference("/GroupChat/"+groupheader.getGroupKey()).child("lastMessageTimestap").setValue(message.getTimeStamp());






                        }
                    })
                    .create();

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
                            }
                        })
                        .create();

            }

            bottomSheetDialogFragment.show(getSupportFragmentManager());



        }
    }

    @OnClick(R.id.groupsend_button)
    public void groupMessageSend()
    {

        Message message = new Message(groupMessageText.getText().toString(),myName,null,null);

        if(currentUser.getProfileDP()!=null)
            message.setPhotoUrl(currentUser.getProfileDP());

        String messageKey = groupmessageReference.push().getKey();

        groupmessageReference.child(messageKey).setValue(message);

        groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

        groupMessageText.setText("");

        firebaseDatabase.getReference("/GroupChat/"+groupheader.getGroupKey()).child("lastMessage").setValue(message.getText());

        firebaseDatabase.getReference("/GroupChat/"+groupheader.getGroupKey()).child("lastMessageTimestap").setValue(message.getTimeStamp());

    }


    @Override
    protected void onStart() {
        super.onStart();

        if(groupChatMessageListener == null)
            groupChatMessageListener = new GroupChatMessageListener();
        groupmessageReference.addChildEventListener(groupChatMessageListener);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(groupChatMessageListener!=null) {
            groupmessageReference.removeEventListener(groupChatMessageListener);
            groupChatMessageListener = null;
            messages_adapter.messages.clear();

        }


    }

    class GroupChatMessageListener implements ChildEventListener {


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final Message message = dataSnapshot.getValue(Message.class);


            if(message.getTimeStamp()!=null) {

                messages_adapter.messages.add(message);

                messages_adapter.messageMap.put(dataSnapshot.getKey(),message);

                if(message.getPhotoContentUrl() != null)

                mhandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Util.saveImage(GroupChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());


                    }
                });

                Util.getDate(message.getTimeStamp());

                messages_adapter.notifyDataSetChanged();

                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

            }

        }




        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

          final   Message message = dataSnapshot.getValue(Message.class);


            if(message.getTimeStamp()!=null && !messages_adapter.messageMap.containsKey(dataSnapshot.getKey()) ) {

                messages_adapter.messages.add(message);

                messages_adapter.messageMap.put(dataSnapshot.getKey(),message);

                if(message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {

                            Util.saveImage(GroupChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());


                        }
                    });

                Util.getDate(message.getTimeStamp());

                messages_adapter.notifyDataSetChanged();

                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

            }

            else if(message.getTimeStamp()!=null && messages_adapter.messageMap.containsKey(dataSnapshot.getKey()))
            {

                messages_adapter.changeMessage(dataSnapshot.getKey(),message.getTimeStamp());

                messages_adapter.notifyDataSetChanged();

                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

            }


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
    protected void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }
}
