package com.chatapp.ramji.buddyplans;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.github.oliveiradev.image_zoom.lib.widget.ZoomAnimation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatReference;
    FirebaseStorage firebaseStorage;
    StorageReference imageStorageReference;
    @BindView(R.id.chat_messages)
    RecyclerView chatMessagesView;
    private Friend friend;
    private String friendUid;
    private String myUid;
    private String chatId;
    @BindView(R.id.chatsend_button)
    ImageButton sendButton;
    @BindView(R.id.chat_message_input)
    EmojiconEditText messageInput;
    @BindView(R.id.smiley_button)
    ImageView smileyButton;
    @BindView(R.id.activity_chat)
    View RootView;
    private String myName;
    Messages_Adapter messages_adapter;
    ChatMessageListener chatMessageListener;
    @BindView(R.id.chat_toolbar)
    Toolbar toolbar;
   @BindView(R.id.chat_title)
    TextView chatTitle;
    @BindView(R.id.circular_image)
    CircularImageView circularImage;

    @BindView(R.id.menu_yellow)
    FloatingActionMenu attachMenu;

    EmojIconActions emojIcon;
    TedBottomPicker bottomSheetDialogFragment;
    final int WRITE_REQUEST = 1;
    final int LOCATION_REQUEST = 2;
    final int PLACE_PICKER_REQUEST = 100;
    HandlerThread handlerThread;
    Handler mhandler;
    User currentUser;
    Intent shareIntent = null;
    private GoogleApiClient mGoogleApiClient;
    DatabaseReference messageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        firebaseDatabase = FirebaseDatabase.getInstance();


        Intent intent = getIntent();
        friend = (Friend) intent.getSerializableExtra("Friend");
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("image");
         shareIntent = (Intent) intent.getParcelableExtra("shareIntent");

        if(bitmap !=null) {
            circularImage.setImageBitmap(bitmap);

        }

        else {

            Glide.with(this).load(friend.getPhotourl()).into(circularImage);

        }


        circularImage.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 ZoomAnimation.zoom(v, circularImage.getDrawable(), ChatActivity.this, false);
                                             }
                                         });

        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);
        myName = currentUser.getUserName();
        chatTitle.setText(friend.getName());
        myUid = currentUser.getUid();
        friendUid = friend.getUid();
        chatId = friend.getChatid();

        messageReference = firebaseDatabase.getReference().child("Messages").child(chatId);
         messages_adapter = new Messages_Adapter(this,myUid);
        chatMessagesView.setLayoutManager(new LinearLayoutManager(this){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                super.smoothScrollToPosition(recyclerView, state, position);
            }
        });

        chatMessagesView.setAdapter(messages_adapter);
        emojIcon = new EmojIconActions(this, RootView, messageInput, smileyButton);
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


        firebaseStorage = FirebaseStorage.getInstance();

        imageStorageReference = firebaseStorage.getReference().child("chat_photos");

        handlerThread = new HandlerThread("myhandlerThread");

        handlerThread.start();

        mhandler = new Handler(handlerThread.getLooper());

//        if(friend.getChatid() == null)
//            createChatId();

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
                  messageInput.setText(sharedText);



             }
         }

     }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.addLocation_menu :

                startPlacePicker();
                return true;

            default :

                return super.onOptionsItemSelected(item);


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

    @OnClick(R.id.attachlocation)
    public void startPlacePicker()
    {

        attachMenu.hideMenu(true);

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







     public void createChatId()
     {

         DatabaseReference ChatReference = firebaseDatabase.getReference().child("Messages");

          chatId = ChatReference.push().getKey();

         friend.setChatid(chatId);

         firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).setValue(friend);

         firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("chatid").setValue(chatId);


     }


    @OnClick(R.id.attachPhoto)
    public void attachPhoto()
    {

        attachMenu.hideMenu(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_REQUEST);

            return;

        }

        boolean addListener = false;


        if(friend.getChatid() == null) {
            createChatId();
            addListener = true;
        }

        if(addListener)
        {
            chatMessageListener = new ChatMessageListener();
            chatReference = firebaseDatabase.getReference().child("Messages").child(chatId);
            chatReference.addChildEventListener(chatMessageListener);

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

                            attachMenu.setVisibility(View.VISIBLE);

                            boolean stop = false;

                            StorageReference imageRef = imageStorageReference.child(uri.getLastPathSegment());

                            final Snackbar snackbar = Snackbar.make(RootView,"Uploading the image",Snackbar.LENGTH_LONG);
                            snackbar.show();


                            final Message loadmessage = new Message(null, myName,uri.toString(),uri.getLastPathSegment(),currentUser.getUid(),null);
                            loadmessage.setTimeStamp(System.currentTimeMillis());

                            messages_adapter.messages.add(loadmessage);

                            messages_adapter.notifyDataSetChanged();



                            imageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                    @SuppressWarnings("VisibleForTests") Message message = new Message(null, myName, taskSnapshot.getDownloadUrl().toString(),uri.getLastPathSegment(),currentUser.getUid(),null);



                                    if (currentUser.getProfileDP() != null)
                                        message.setPhotoUrl(currentUser.getProfileDP());



                                    String messageKey = messageReference.push().getKey();

                                    messageReference.child(messageKey).setValue(message);

                                    messages_adapter.messages.remove(loadmessage);

                                    messages_adapter.notifyDataSetChanged();

                                    messageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                                    messageInput.setText("");

                                    firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).child("lastMessage").setValue(currentUser.getUserName() + " has uploaded a image");

                                    firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

                                    firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("lastMessage").setValue(currentUser.getUserName() + " has uploaded a image");

                                    firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

                                    NotificationMessage notificationMessage = new NotificationMessage(myUid,friendUid,currentUser.getUserName() + " has uploaded a image" );

                                    firebaseDatabase.getReference().child("Notications").push().setValue(notificationMessage);

                                    Snackbar snackbar2 = Snackbar.make(RootView, "Uploading done !", Snackbar.LENGTH_SHORT);
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



    @OnClick(R.id.chatsend_button)
    public void SendMessage()
    {

        boolean addListener = false;


        if(friend.getChatid() == null) {
            createChatId();
            addListener = true;
        }





        Message message = new Message(messageInput.getText().toString(),myName,null,null,currentUser.getUid(),null);


        String messageKey = messageReference.push().getKey();

        messageReference.child(messageKey).setValue(message);

        messageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

        firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).child("lastMessage").setValue(message.getText());

        firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

        firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("lastMessage").setValue(message.getText());

        firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

        String notificationtext;

        if(message.getText().length() <= 20)
            notificationtext = currentUser.getUserName() + " : " + message.getText();

        else
            notificationtext = currentUser.getUserName() + " : " + message.getText().substring(0,20) + "...";



         NotificationMessage notificationMessage = new NotificationMessage(myUid,friendUid,notificationtext );

        firebaseDatabase.getReference().child("Notications").push().setValue(notificationMessage);


        if(addListener)
        {
            chatMessageListener = new ChatMessageListener();
            chatReference = firebaseDatabase.getReference().child("Messages").child(chatId);
            chatReference.addChildEventListener(chatMessageListener);

        }

        messageInput.setText("");

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                String toastMsg = String.format("Place: %s", place.getId());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

                com.chatapp.ramji.buddyplans.Location location = new com.chatapp.ramji.buddyplans.Location(place.getLatLng().latitude,place.getLatLng().longitude);

                Message message = new Message(null,myName,null,null,currentUser.getUid(),location);

                String messageKey = messageReference.push().getKey();

                messageReference.child(messageKey).setValue(message);

                messageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                messageInput.setText("");

                firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).child("lastMessage").setValue(currentUser.getUserName() + " has uploaded a location");

                firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

                firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("lastMessage").setValue(currentUser.getUserName() + " has uploaded a location");

                firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("lastMessageTimestap").setValue(ServerValue.TIMESTAMP);

                NotificationMessage notificationMessage = new NotificationMessage(myUid,friendUid,currentUser.getUserName() + " has uploaded a location" );

                firebaseDatabase.getReference().child("Notications").push().setValue(notificationMessage);



            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(chatId!=null)
        {
            if(chatMessageListener==null)

            {

                chatMessageListener = new ChatMessageListener();
                chatReference = firebaseDatabase.getReference().child("Messages").child(chatId);
                chatReference.addChildEventListener(chatMessageListener);

            }

        }

    }


    @Override
    protected void onStop() {
        super.onStop();

        if(chatId!=null && chatMessageListener!=null) {
            chatReference.removeEventListener(chatMessageListener);
            chatMessageListener = null;
            messages_adapter.messages.clear();

        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Google API connection","Google API connection " + connectionResult.getErrorMessage());
    }



    class ChatMessageListener implements ChildEventListener{


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

                            Util.saveImage(ChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());


                        }
                    });

                Util.getDate(message.getTimeStamp());

                messages_adapter.notifyDataSetChanged();

                chatMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

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

                            Util.saveImage(ChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());

                        }
                    });

                Util.getDate(message.getTimeStamp());

                messages_adapter.notifyDataSetChanged();

                chatMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

            }

            else if(message.getTimeStamp()!=null && messages_adapter.messageMap.containsKey(dataSnapshot.getKey()))
            {

                messages_adapter.changeMessage(dataSnapshot.getKey(),message.getTimeStamp());

                messages_adapter.notifyDataSetChanged();

                chatMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);

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


}
