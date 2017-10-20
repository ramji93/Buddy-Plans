package com.chatapp.ramji.buddyplans;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.chatapp.ramji.buddyplans.ViewModels.ChatViewModel;
import com.chatapp.ramji.buddyplans.db.MessageEntity;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    Context mContext = this;
    final int LOCATION_REQUEST = 2;
    final int PLACE_PICKER_REQUEST = 100;
    HandlerThread handlerThread;
    Handler mhandler;
    User currentUser;
    Intent shareIntent = null;
    private GoogleApiClient mGoogleApiClient;
    DatabaseReference messageReference;

    Menu menu;
    Query chatQuery;
    ChatViewModel chatViewModel = null;
    Long dbLastTimestamp;
    LiveData<List<MessageEntity>> messages;
    Boolean isfavourite = false;
    boolean getfromdb = false;
    Calendar remindCalendar;
    AlertDialog dialog = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        attachMenu.setClosedOnTouchOutside(true);

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


        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        chatViewModel.getSavedChat(chatId);

        chatViewModel.getmessages(chatId);

        dbLastTimestamp = chatViewModel.lastTimestamp;

        chatViewModel.lastTimestampLive.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                if(aLong != null)
                    dbLastTimestamp = aLong;
            }
        });

        if (chatViewModel.savedchat.size() > 0  && dbLastTimestamp != null)
        {
            getfromdb = true;

        }

        if (chatId != null && menu != null) {
            if(chatViewModel.savedchat.size()>0 ) {
                if(chatViewModel.savedchat.get(1).favourite==true) {
                    isfavourite = true;
                    menu.getItem(0).setIcon(R.drawable.fav_unselect);
                    menu.getItem(1).setVisible(true);
                }
            }

        }


        messageReference = firebaseDatabase.getReference().child("Messages").child(chatId);
         messages_adapter = new Messages_Adapter(this,myUid);
        chatMessagesView.setLayoutManager(new LinearLayoutManager(this){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                super.smoothScrollToPosition(recyclerView, state, position);
            }
        });

        chatMessagesView.setAdapter(messages_adapter);

        messages = chatViewModel.messages;

        messages.observe(this, new Observer<List<MessageEntity>>() {
            @Override
            public void onChanged(@Nullable List<MessageEntity> messageEntities) {
                messages_adapter.messages = (ArrayList<MessageEntity>) messageEntities;
                messages_adapter.notifyDataSetChanged();
                chatMessagesView.scrollToPosition(messages_adapter.getItemCount() - 1);
            }
        });

        chatReference = firebaseDatabase.getReference().child("Messages").child(chatId);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        if (chatId != null) {
            if(chatViewModel!=null) {
                if (chatViewModel.savedchat.size() > 0) {
                    if (chatViewModel.savedchat.get(1).favourite == true) {
                        isfavourite = true;
                        menu.getItem(0).setIcon(R.drawable.fav_unselect);
                        menu.getItem(1).setVisible(true);
                    }
                }
            }

        }

        this.menu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.mark_favourite:

                onFavouritePress();

            case R.id.add_reminder:

                addReminder();


            default:

                return super.onOptionsItemSelected(item);



        }

    }

    private void addReminder()
    {

        remindCalendar = Calendar.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.setreminder_dialog_layout,null);
        final EditText title_view = (EditText) view.findViewById(R.id.reminder_title);
        Button setdate_button =  (Button) view.findViewById(R.id.setdatetime_button);
        final TextView dateTime_view = (TextView) view.findViewById(R.id.datetime_textview);
        Button proceed_view = (Button) view.findViewById(R.id.proceed_button);
        builder.setView(view);
        dialog = builder.show();
        setdate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                datePicker(dateTime_view);

            }
        });

        proceed_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String datetime = dateTime_view.getText().toString();
                String title = title_view.getText().toString();

                if(datetime.isEmpty() || datetime == null || datetime.equalsIgnoreCase("") || title.isEmpty() || title == null || title.equalsIgnoreCase("") )
                {
                    Toast.makeText(mContext, "Mandatory fields should be entered", Toast.LENGTH_SHORT).show();
                }
                else {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.MINUTE,5);
                    long minLong = c.getTimeInMillis();

                    long remLong = remindCalendar.getTimeInMillis();

                    if(remLong < minLong)
                    {
                        Toast.makeText(mContext, "Time set should be atleast 5 mins later than current time", Toast.LENGTH_LONG).show();
                    }
                    else{

                        //// TODO: add reminder
                        dialog.dismiss();

                    }


                }

            }
        });



    }

    private void onFavouritePress()
    {

        if(isfavourite)
        {
            chatViewModel.setNotFavouriteChat(chatId);
            isfavourite = false;

        }

        else {
            chatViewModel.setFavouriteChat(chatId);
            isfavourite = false;
        }

        invalidateOptionsMenu();

    }

    private void datePicker(final TextView datetimetext){

        // Get Current Date

        int mYear = remindCalendar.get(Calendar.YEAR);
        int mMonth = remindCalendar.get(Calendar.MONTH);
        int mDay = remindCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        remindCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        remindCalendar.set(Calendar.MONTH,monthOfYear);
                        remindCalendar.set(Calendar.YEAR,year);
                        timePicker(datetimetext);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void timePicker(final TextView datetimeText){
        // Get Current Time

        int mHour = remindCalendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = remindCalendar.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        remindCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        remindCalendar.set(Calendar.MINUTE,minute);

                        datetimeText.setText(remindCalendar.get(Calendar.DAY_OF_MONTH) + "/" + (remindCalendar.get(Calendar.MONTH)+1) + "/" + remindCalendar.get(Calendar.YEAR) + " " +hourOfDay + ":" +  ((minute > 9) ? minute : "0"+minute ) +"  ");
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
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

//            attachMenu.toggleMenu(true);
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

        attachMenu.toggle(true);

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

                            uploadImage(uri);
                        }
                    })
                    .create();

        }

        bottomSheetDialogFragment.show(getSupportFragmentManager());

    }

    private void uploadImage(final Uri uri)
    {
            // here is selected uri

            StorageReference imageRef = imageStorageReference.child(uri.getLastPathSegment());

            final Snackbar snackbar = Snackbar.make(RootView,"Uploading the image",Snackbar.LENGTH_LONG);
            snackbar.show();


            final Message loadmessage = new Message(null, myName,uri.toString(),uri.getLastPathSegment(),currentUser.getUid(),null);
            loadmessage.setTimeStamp(System.currentTimeMillis());

//            messages_adapter.messages.add(loadmessage);

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
                chatMessageListener = new ChatMessageListener();

            if(getfromdb) {

                Long tmp = dbLastTimestamp +1;
                chatQuery = chatReference.orderByChild("timeStamp").startAt(tmp);
//           .addChildEventListener(groupChatMessageListener);
            }
            else {
                chatQuery = chatReference.orderByChild("timeStamp");
            }

            chatQuery.addChildEventListener(chatMessageListener);

        }

    }


    @Override
    protected void onStop() {
        super.onStop();

        if(chatId!=null && chatMessageListener!=null) {
            chatQuery.removeEventListener(chatMessageListener);
            chatMessageListener = null;

        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Google API connection","Google API connection " + connectionResult.getErrorMessage());
    }



    class ChatMessageListener implements ChildEventListener{


        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

            final Message message = dataSnapshot.getValue(Message.class);

            final boolean m_getfromdb = getfromdb;

            if(message.getTimeStamp()!=null) {


                if (message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {

                            String contentphotourl = Util.saveImage(ChatActivity.this, message.getPhotoContentUrl(), message.getPhotoContentName());
                            String userphotourl = Util.saveImage(ChatActivity.this, message.getPhotoUrl(), message.getUid());
                            //// TODO: update the db message with local urls

                            if(!m_getfromdb) {
                                String groupphotourl = Util.saveImage(ChatActivity.this, friend.getPhotourl(), chatId);
                                chatViewModel.insertChat(new SavedChatsEntity(chatId,friend.getName(),groupphotourl,false,null,friendUid));
                                getfromdb = true;
                            }

                            message.setMessageid(dataSnapshot.getKey());

                            MessageEntity entity = Util.getEntityfromMessage(message, chatId, mContext);

                            entity.setPhotoContentUrl(contentphotourl);

                            entity.setPhotoUrl(userphotourl);

                            chatViewModel.insertMessage(entity);

                        }
                    });


                message.setMessageid(dataSnapshot.getKey());

                MessageEntity entity = Util.getEntityfromMessage(message, chatId, mContext);

                if(!getfromdb)
                {

                    chatViewModel.insertChat(new SavedChatsEntity(chatId,friend.getName(),friend.getPhotourl(),false,null,friendUid));
                    getfromdb = true;
                }

                chatViewModel.insertMessage(entity);


            }

        }




        @Override
        public void onChildChanged(final DataSnapshot dataSnapshot, String s) {

            final   Message message = dataSnapshot.getValue(Message.class);

            if(message.getTimeStamp()!=null)
            {
//
                if(message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {

                            String contentphotourl =  Util.saveImage(ChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());
                            String userphotourl =  Util.saveImage(ChatActivity.this,message.getPhotoUrl(),message.getUid());
                            //// TODO: update the db message with local urls

                            message.setMessageid(dataSnapshot.getKey());

                            MessageEntity entity = Util.getEntityfromMessage(message, chatId, mContext);

                            entity.setPhotoContentUrl(contentphotourl);

                            entity.setPhotoUrl(userphotourl);

                            chatViewModel.insertMessage(entity);
                        }
                    });


                message.setMessageid(dataSnapshot.getKey());

                MessageEntity entity = Util.getEntityfromMessage(message,chatId,mContext);

                chatViewModel.insertMessage(entity);

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
