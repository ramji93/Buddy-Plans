package com.chatapp.ramji.buddyplans;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatapp.ramji.buddyplans.ViewModels.ChatViewModel;
import com.chatapp.ramji.buddyplans.db.MessageEntity;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;
import com.chatapp.ramji.buddyplans.service.DownloadChatService;
import com.github.clans.fab.FloatingActionMenu;
import com.github.oliveiradev.image_zoom.lib.widget.ZoomAnimation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

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
    Query groupQuery;

    final int LOCATION_REQUEST = 2;
    final int PLACE_PICKER_REQUEST = 100;

    Boolean isfavourite = false;
    Boolean getfromdb = false;

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
    @BindView(R.id.menu_yellow)
    FloatingActionMenu attachMenu;

    Context mContext = this;
    @BindView(R.id.group_activity_logo)
    CircularImageView groupLogo;
    @BindView(R.id.groupchat_title)
    TextView groupTitle;
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
    Intent shareIntent = null;
    Menu menu;
    ChatViewModel chatViewModel = null;
    Long dbLastTimestamp;
    LiveData<List<MessageEntity>> messages;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        ButterKnife.bind(this);

        supportPostponeEnterTransition();

        Intent intent = getIntent();

        String transition = intent.getStringExtra("transition");

        groupheader = (Groupheader) intent.getSerializableExtra("group");

        groupChatId = groupheader.getChatId();

        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        chatViewModel.getSavedChat(groupChatId);

        chatViewModel.getmessages(groupChatId);

        chatViewModel.lastTimestamp.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                dbLastTimestamp = aLong;
            }
        });



       if (chatViewModel.savedchat.size() > 0  && dbLastTimestamp != null)
        {
            getfromdb = true;

        }

        if(groupChatId!=null && menu!=null)
        {
          if(PreferenceManager.getDefaultSharedPreferences(this).getString("savedchats","").contains(groupChatId))
          {
              isfavourite = true;
              menu.getItem(1).setIcon(R.drawable.fav_unselect);
          }

        }

        setSupportActionBar(groupChatToolbar);


        getSupportActionBar().setDisplayShowTitleEnabled(false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transition != null) {

            groupLogo.setTransitionName(transition);
        }

        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("image");
        shareIntent = (Intent) intent.getParcelableExtra("shareIntent");

        if(bitmap != null && transition != null ) {
            groupLogo.setImageBitmap(bitmap);
            supportStartPostponedEnterTransition();
        }

        else
        Glide.with(this).load(groupheader.getPhotoUrl()).into(groupLogo);


        groupLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomAnimation.zoom(v, groupLogo.getDrawable(), GroupChatActivity.this, false);
            }
        });


        groupTitle.setText(groupheader.getName());

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



        messages_adapter = new Messages_Adapter(this,currentUser.getUid());
        groupMessagesView.setLayoutManager(new LinearLayoutManager(this){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                super.smoothScrollToPosition(recyclerView, state, position);
            }
        });

        groupMessagesView.setAdapter(messages_adapter);

        messages = chatViewModel.messages;

        messages.observe(this, new Observer<List<MessageEntity>>() {
            @Override
            public void onChanged(@Nullable List<MessageEntity> messageEntities) {
                messages_adapter.messages = (ArrayList<MessageEntity>) messageEntities;
                messages_adapter.notifyDataSetChanged();
                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);
            }
        });

        //TODO : OBSERVE LIVEDATA OF MESSAGES

        groupmessageReference = firebaseDatabase.getReference().child("Messages").child(groupChatId);

        firebaseStorage = FirebaseStorage.getInstance();

        imageStorageReference = firebaseStorage.getReference().child("chat_photos");

        handlerThread = new HandlerThread("myhandlerThread");

        handlerThread.start();

        mhandler = new Handler(handlerThread.getLooper());

        if(shareIntent!= null)
        {

            handleSharedIntent();

        }

    }


    public void handleSharedIntent()
    {


        if (Intent.ACTION_SEND.equals(shareIntent.getAction()) && shareIntent.getType() != null) {
            if ("text/plain".equals(shareIntent.getType())) {
                // Handle text being sent

                String sharedText = shareIntent.getStringExtra(Intent.EXTRA_TEXT);
                groupMessageText.setText(sharedText);

                shareIntent = null;

            }

            else if(shareIntent.getType().contains("image")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Do you want to upload the image in this chat? ");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                        Uri imageUri = (Uri) shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                        uploadImage(imageUri);
                        shareIntent = null;

                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ((Activity) mContext).finishAndRemoveTask();
                        }
                        else
                        {
                            ((Activity) mContext).finishAffinity();
                        }


                    }
                });
                AlertDialog dialog =  builder.create();
                dialog.show();

                Log.d(this.getClass().getName(),"inside onsharedintent");

            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                String toastMsg = String.format("Place: %s", place.getId());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

                com.chatapp.ramji.buddyplans.Location location = new com.chatapp.ramji.buddyplans.Location(place.getLatLng().latitude,place.getLatLng().longitude);

                Message message = new Message(null,myName,null,null,currentUser.getUid(),location);

                String messageKey = groupmessageReference.push().getKey();

                groupmessageReference.child(messageKey).setValue(message);

                groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                groupMessageText.setText("");

                String notificationText = groupheader.getName()+ " : " + myName + " has uploaded a location";

                GroupNotification groupNotification = new GroupNotification(currentUser.getUid(),myName,groupChatId,notificationText);

                firebaseDatabase.getReference("GroupNotifications").push().setValue(groupNotification);

                firebaseDatabase.getReference("/GroupChat/" + groupheader.getGroupKey()).child("lastMessage").setValue(message.getUserName() + " uploaded a location ");

                firebaseDatabase.getReference("/GroupChat/" + groupheader.getGroupKey()).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);




            }


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.groupchat_menu, menu);
        if(groupChatId!=null)
        {
            if(PreferenceManager.getDefaultSharedPreferences(this).getString("savedchats","").contains(groupChatId))
            {
                isfavourite = true;
                menu.getItem(1).setIcon(R.drawable.fav_unselect);
            }

        }

        this.menu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId())
       {
           case R.id.editgroup_menu :

               Intent intent = new Intent(this,EditGroupActivity.class);
               intent.putExtra("group",groupheader);
               startActivity(intent);
               return true;

           case R.id.mark_favourite :

           persistChat();


            default :

              return super.onOptionsItemSelected(item);


       }



    }


    private void persistChat()
    {
           //// TODO: add persistchat
//          Intent serviceIntent = new Intent(this, DownloadChatService.class);
//          ServiceData serviceData = new ServiceData(groupheader.getChatId(),groupheader.getName(),groupheader.getPhotoUrl(),messages_adapter.messages,groupheader.getGroupKey());
//          serviceIntent.putExtra("data",serviceData);
//          startService(serviceIntent);
          menu.getItem(1).setIcon(R.drawable.fav_unselect);
          Toast.makeText(mContext, "This chat is marked as favourite", Toast.LENGTH_LONG).show();

    }




    @OnClick(R.id.attachlocation)
    public void startPlacePicker()
    {

        attachMenu.toggle(true);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)

        {

            handlePermissions();

            return;
        }



        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }




    }

    public void handlePermissions()
    {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST
            );


        }


    }




    @OnClick(R.id.attachPhoto)
    public void attachPhoto()
    {

        attachMenu.toggle(true);

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
                            uploadImage(uri);
                        }
                    })
                    .create();

        }

          bottomSheetDialogFragment.show(getSupportFragmentManager());



    }


    private void uploadImage(final Uri uri) {
        // here is selected uri

        StorageReference imageRef = imageStorageReference.child(uri.getLastPathSegment());


        final Snackbar snackbar = Snackbar.make(rootView, "Uploading the image", Snackbar.LENGTH_LONG);
        snackbar.show();

        imageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                @SuppressWarnings("VisibleForTests") Message message = new Message(null, myName, taskSnapshot.getDownloadUrl().toString(), uri.getLastPathSegment(), currentUser.getUid(), null);

                if (currentUser.getProfileDP() != null)
                    message.setPhotoUrl(currentUser.getProfileDP());

                String messageKey = groupmessageReference.push().getKey();

                groupmessageReference.child(messageKey).setValue(message);

                groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                groupMessageText.setText("");

                String notificationText = groupheader.getName() + " : " + myName + " has uploaded a image";

                GroupNotification groupNotification = new GroupNotification(currentUser.getUid(), myName, groupChatId, notificationText);

                firebaseDatabase.getReference("GroupNotifications").push().setValue(groupNotification);

                firebaseDatabase.getReference("/GroupChat/" + groupheader.getGroupKey()).child("lastMessage").setValue(message.getUserName() + " uploaded a image ");

                firebaseDatabase.getReference("/GroupChat/" + groupheader.getGroupKey()).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

                Snackbar snackbar2 = Snackbar.make(rootView, "Uploading done !", Snackbar.LENGTH_SHORT);
                snackbar2.show();

            }
        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==WRITE_REQUEST)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (bottomSheetDialogFragment == null) {

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

        else if(requestCode == LOCATION_REQUEST)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startPlacePicker();

            }


        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick(R.id.groupsend_button)
    public void groupMessageSend()
    {

        Message message = new Message(groupMessageText.getText().toString(),myName,null,null,currentUser.getUid(),null);

        if(currentUser.getProfileDP()!=null)
            message.setPhotoUrl(currentUser.getProfileDP());

        String messageKey = groupmessageReference.push().getKey();

        groupmessageReference.child(messageKey).setValue(message);

        groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

        groupMessageText.setText("");

        String notificationText;

        if(message.getText().length() <= 20)
            notificationText = groupheader.getName() + " ~ " + currentUser.getUserName() + " : " + message.getText();

        else
            notificationText = groupheader.getName() + " ~ " + currentUser.getUserName() + " : " + message.getText().substring(0,20) + "...";



        GroupNotification groupNotification = new GroupNotification(currentUser.getUid(),myName,groupChatId,notificationText);

        firebaseDatabase.getReference("GroupNotifications").push().setValue(groupNotification);


        firebaseDatabase.getReference("/GroupChat/"+groupheader.getGroupKey()).child("lastMessage").setValue(message.getUserName() +" : " + message.getText());

        firebaseDatabase.getReference("/GroupChat/"+groupheader.getGroupKey()).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

    }


    @Override
    protected void onStart() {
        super.onStart();

        if(groupChatMessageListener == null)
            groupChatMessageListener = new GroupChatMessageListener();
        if(getfromdb) {

            groupQuery = groupmessageReference.orderByChild("timeStamp").endAt(dbLastTimestamp);
//           .addChildEventListener(groupChatMessageListener);
        }
        else {
            groupQuery = groupmessageReference.orderByChild("timeStamp");
        }

        groupQuery.addChildEventListener(groupChatMessageListener);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if(groupChatMessageListener!=null) {
            groupQuery.removeEventListener(groupChatMessageListener);
            groupChatMessageListener = null;
            messages_adapter.messages.clear();

        }


    }

    class GroupChatMessageListener implements ChildEventListener {


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final Message message = dataSnapshot.getValue(Message.class);

//            if(message.getTimeStamp()!=null) {
//
//                messages_adapter.messages.add(message);
//
//                messages_adapter.messageMap.put(dataSnapshot.getKey(),message);
//
//                if(message.getPhotoContentUrl() != null)
//
//                mhandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Util.saveImage(GroupChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());
//
//
//                    }
//                });
//
//                messages_adapter.notifyDataSetChanged();
//
//                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);
//
//            }

            if(message.getTimeStamp()!=null ) {

                if (message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {

                            String contentphotourl = Util.saveImage(GroupChatActivity.this, message.getPhotoContentUrl(), message.getPhotoContentName());
                            String userphotourl = Util.saveImage(GroupChatActivity.this, message.getPhotoUrl(), message.getUid());
                            //// TODO: update the db message with local urls

                        }
                    });


                message.setMessageid(dataSnapshot.getKey());

                MessageEntity entity = Util.getEntityfromMessage(message, groupChatId, mContext);

                if(!getfromdb)
                {

                    chatViewModel.insertChat(new SavedChatsEntity(groupChatId,groupheader.getName(),groupheader.getPhotoUrl(),true,groupheader.getGroupKey()));
                    getfromdb = true;
                }

                chatViewModel.insertMessage(entity);

            }

        }




        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

          final   Message message = dataSnapshot.getValue(Message.class);


//            if(message.getTimeStamp()!=null && !messages_adapter.messageMap.containsKey(dataSnapshot.getKey()) ) {
              if(message.getTimeStamp()!=null ) {

//                messages_adapter.messages.add(message);
//
//                messages_adapter.messageMap.put(dataSnapshot.getKey(),message);
//
                if(message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {

                          String contentphotourl =  Util.saveImage(GroupChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());
                          String userphotourl =  Util.saveImage(GroupChatActivity.this,message.getPhotoUrl(),message.getUid());
                            //// TODO: update the db message with local urls

                        }
                    });
//
//                Util.getDate(message.getTimeStamp());
//
//                messages_adapter.notifyDataSetChanged();
//
//                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

                   message.setMessageid(dataSnapshot.getKey());

                   MessageEntity entity = Util.getEntityfromMessage(message,groupChatId,mContext);

                  chatViewModel.insertMessage(entity);

            }

//            else if(message.getTimeStamp()!=null && messages_adapter.messageMap.containsKey(dataSnapshot.getKey()))
//            {
//
//                messages_adapter.changeMessage(dataSnapshot.getKey(),message.getTimeStamp());
//
//                messages_adapter.notifyDataSetChanged();
//
//                groupMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);
//
//            }


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
