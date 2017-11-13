package com.chatapp.ramji.buddyplans;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gun0912.tedbottompicker.TedBottomPicker;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static android.os.Build.VERSION_CODES.M;
import static java.security.AccessController.getContext;

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
    final int CALENDAR_REQUEST = 2;
    HandlerThread handlerThread;
    Handler mhandler;
    Intent shareIntent = null;
    Menu menu;
    ChatViewModel chatViewModel = null;
    Long dbLastTimestamp;
    LiveData<List<MessageEntity>> messages;
    Calendar remindCalendar;
    AlertDialog dialog = null;
    String profileDpUrl = null;
    boolean isConnected;


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

        dbLastTimestamp = chatViewModel.lastTimestamp;

        chatViewModel.lastTimestampLive.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                if (aLong != null)
                    dbLastTimestamp = aLong;
            }
        });

        if (chatViewModel.savedchat.size() > 0 && dbLastTimestamp != null) {
            getfromdb = true;

        }

        if (groupChatId != null && menu != null) {
           if(chatViewModel.savedchat.size()>0 ) {
               if(chatViewModel.savedchat.get(0).favourite==true) {
                   isfavourite = true;
                   menu.getItem(1).setIcon(R.drawable.fav_unselect);
                   menu.getItem(2).setVisible(true);
               }
            }

        }

        setSupportActionBar(groupChatToolbar);


        getSupportActionBar().setDisplayShowTitleEnabled(false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transition != null) {

            groupLogo.setTransitionName(transition);
        }

        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("image");
        shareIntent = (Intent) intent.getParcelableExtra("shareIntent");

        if (bitmap != null && transition != null) {
            groupLogo.setImageBitmap(bitmap);
            supportStartPostponedEnterTransition();
        } else
            Glide.with(this).load(groupheader.getPhotoUrl()).into(groupLogo);


        groupLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ZoomAnimation.zoom(v, groupLogo.getDrawable(), GroupChatActivity.this, false);

                String path = Environment.getExternalStorageDirectory().getPath()+"/Buddyplans/pictures"+"/"+groupheader.getChatId();

                File f=new File(path);

                if(f.exists()) {

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);

                    if(Build.VERSION.SDK_INT > M)
                    {
                        intent.setDataAndType(CustomFileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".my.package.name.provider", f),"image/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mContext.startActivity(intent);
                    }

                    else {
                        intent.setDataAndType(Uri.fromFile(f), "image/*");
                        mContext.startActivity(intent);
                    }
                }



            }
        });


        groupTitle.setText(groupheader.getName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);
        myName = currentUser.getUserName();
        profileDpUrl = sharedPreferences.getString("profiledp",currentUser.getProfileDP());

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


        messages_adapter = new Messages_Adapter(this, currentUser.getUid());
        groupMessagesView.setLayoutManager(new LinearLayoutManager(this) {
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

        if (shareIntent != null) {

            handleSharedIntent();

        }

        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {

            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    Util.saveImage(GroupChatActivity.this, groupheader.getPhotoUrl(), groupheader.getChatId());
                }
            });
        }


    }


    public void handleSharedIntent() {


        if (Intent.ACTION_SEND.equals(shareIntent.getAction()) && shareIntent.getType() != null) {
            if ("text/plain".equals(shareIntent.getType())) {
                // Handle text being sent

                String sharedText = shareIntent.getStringExtra(Intent.EXTRA_TEXT);
                groupMessageText.setText(sharedText);

                shareIntent = null;

            } else if (shareIntent.getType().contains("image")) {

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
                        } else {
                            ((Activity) mContext).finishAffinity();
                        }


                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                Log.d(this.getClass().getName(), "inside onsharedintent");

            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getId());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

                com.chatapp.ramji.buddyplans.Location location = new com.chatapp.ramji.buddyplans.Location(place.getLatLng().latitude, place.getLatLng().longitude);

                Message message = new Message(null, myName, null, null, currentUser.getUid(), location);

                if(profileDpUrl != null)
                message.setPhotoUrl(profileDpUrl);

                String messageKey = groupmessageReference.push().getKey();

                groupmessageReference.child(messageKey).setValue(message);

                groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                groupMessageText.setText("");

                String notificationText = groupheader.getName() + " : " + myName + " has uploaded a location";

                GroupNotification groupNotification = new GroupNotification(currentUser.getUid(), myName,groupheader.getGroupKey(), notificationText);

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
        if (groupChatId != null) {
            if(chatViewModel!=null) {
                if (chatViewModel.savedchat.size() > 0) {
                    if (chatViewModel.savedchat.get(0).favourite == true) {
                        isfavourite = true;
                        menu.getItem(1).setIcon(R.drawable.fav_unselect);
                        menu.getItem(2).setVisible(true);
                    }
                }
            }

        }

        this.menu = menu;
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.editgroup_menu:

                if(!Util.checkConnection(mContext))
                {


                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.nointernet)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

                    // Create the AlertDialog object and return it
                    builder.create().show();

                    return true;

                }
                Intent intent = new Intent(this, EditGroupActivity.class);
                intent.putExtra("group", groupheader);
                startActivity(intent);
               break;

            case R.id.mark_favourite:

                onFavouritePress();
                break;

            case R.id.add_reminder:

                addReminder();
                break;

            case R.id.exitgroup_menu:

                exitGroup();
                break;


            default:

                return super.onOptionsItemSelected(item);


        }

          return true;
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

                        GroupReminder reminder = new GroupReminder(currentUser.getUid(),groupheader.getGroupKey(),title,Long.toString(remLong),myName,groupheader.getName(),groupChatId);

                        firebaseDatabase.getReference().child("GroupReminders").push().setValue(reminder);

                        dialog.dismiss();

                        Message message = new Message(currentUser.getUserName() + " has added a group reminder ",null,null,null,null,null);

                        String messageKey = groupmessageReference.push().getKey();

                        groupmessageReference.child(messageKey).setValue(message);

                        groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                    }


                }

            }
        });



    }

    private void onFavouritePress()
    {

       if(isfavourite)
       {
           chatViewModel.setNotFavouriteChat(groupChatId);
           isfavourite = false;

       }

       else {
           chatViewModel.setFavouriteChat(groupChatId);
           isfavourite = false;
       }

        chatViewModel.refreshchat(groupChatId);

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
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {

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
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {

                        remindCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        remindCalendar.set(Calendar.MINUTE,minute);

                        datetimeText.setText(remindCalendar.get(Calendar.DAY_OF_MONTH) + "/" + (remindCalendar.get(Calendar.MONTH)+1) + "/" + remindCalendar.get(Calendar.YEAR) + " " +hourOfDay + ":" +  ((minute > 9) ? minute : "0"+minute ) +"  ");
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }



//    private void persistChat_new() {
//        //// TODO: add persistchat
////          Intent serviceIntent = new Intent(this, DownloadChatService.class);
////          ServiceData serviceData = new ServiceData(groupheader.getChatId(),groupheader.getName(),groupheader.getPhotoUrl(),messages_adapter.messages,groupheader.getGroupKey());
////          serviceIntent.putExtra("data",serviceData);
////          startService(serviceIntent);
//        menu.getItem(1).setIcon(R.drawable.fav_unselect);
//        Toast.makeText(mContext, "This chat is marked as favourite", Toast.LENGTH_LONG).show();
//
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_CALENDAR}, CALENDAR_REQUEST);
//
//            return;
//        } else {
//
//            ContentValues eventValues = new ContentValues();
//
//            eventValues.put("calendar_id", 3);
//            eventValues.put("title", "Meeting with dad");
//            //// TODO: 18-10-2017 description
//
//
//            Calendar c = Calendar.getInstance();
////            long startDate = c.getTimeInMillis();
//
//            c.add(Calendar.MINUTE, 10);
//
//            long startDate = c.getTimeInMillis();
//            long endDate = startDate + 1000 * 60 * 60;
//
//            eventValues.put("dtstart", startDate);
//            eventValues.put("dtend", endDate);
//            eventValues.put("allDay", false);
//            eventValues.put("eventStatus", 0);
//
//            TimeZone timeZone = TimeZone.getDefault();
//            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
//
//            eventValues.put(CalendarContract.Events.HAS_ALARM, 1);
//
//            ContentResolver cr = this.getContentResolver();
//
//            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, eventValues);
//
//            long eventID = Long.parseLong(uri.getLastPathSegment());
//
//            Log.d(GroupChatActivity.class.getName(),"event uri: "+ uri.toString());
//
//            ContentValues reminders = new ContentValues();
//            reminders.put(CalendarContract.Reminders.EVENT_ID, eventID);
//            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
//            reminders.put(CalendarContract.Reminders.MINUTES, 5);
//
//            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);
//
//            Log.d(GroupChatActivity.class.getName(),"reminder uri: "+ uri2.toString());
//
//        }
//
//
//    }




    @OnClick(R.id.attachlocation)
    public void startPlacePicker() {

        attachMenu.toggle(true);

        if (ContextCompat.checkSelfPermission(this,
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

    public void handlePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST
            );


        }


    }


    @OnClick(R.id.attachPhoto)
    public void attachPhoto() {

        attachMenu.toggle(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);

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


        final Snackbar snackbar = Snackbar.make(rootView,(isConnected ? "Uploading the image" : "Image will be uploaded once connection resumes"),(isConnected ? 10000 : 3000));
        snackbar.show();

        imageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                @SuppressWarnings("VisibleForTests") Message message = new Message(null, myName, taskSnapshot.getDownloadUrl().toString(), uri.getLastPathSegment(), currentUser.getUid(), null);

                if (profileDpUrl != null)
                    message.setPhotoUrl(profileDpUrl);

                String messageKey = groupmessageReference.push().getKey();

                groupmessageReference.child(messageKey).setValue(message);

                snackbar.dismiss();

                groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

                groupMessageText.setText("");

                String notificationText = groupheader.getName() + " : " + myName + " has uploaded a image";

                GroupNotification groupNotification = new GroupNotification(currentUser.getUid(), myName, groupheader.getGroupKey(), notificationText);

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

        if (requestCode == WRITE_REQUEST) {
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


        } else if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startPlacePicker();

            }


        } else if (requestCode == CALENDAR_REQUEST) {
            ContentValues eventValues = new ContentValues();

            eventValues.put("calendar_id", 1); // id, We need to choose from
            // our mobile for primary
            // its 1
            eventValues.put("title", "Meeting with dad");


            Calendar c = Calendar.getInstance();
            long startDate = c.getTimeInMillis();

            c.add(Calendar.MINUTE, 5);

            long endDate = c.getTimeInMillis();

            eventValues.put("dtstart", startDate);
            eventValues.put("dtend", endDate);

            TimeZone timeZone = TimeZone.getDefault();
            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());

            eventValues.put(CalendarContract.Events.HAS_ALARM, 1);

            ContentResolver cr = this.getContentResolver();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, eventValues);
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

        if(profileDpUrl!=null)
            message.setPhotoUrl(profileDpUrl);

        String messageKey = groupmessageReference.push().getKey();

        groupmessageReference.child(messageKey).setValue(message);

        groupmessageReference.child(messageKey).child("timeStamp").setValue(ServerValue.TIMESTAMP);

        groupMessageText.setText("");

        String notificationText;

        if(message.getText().length() <= 20)
            notificationText = groupheader.getName() + " ~ " + currentUser.getUserName() + " : " + message.getText();

        else
            notificationText = groupheader.getName() + " ~ " + currentUser.getUserName() + " : " + message.getText().substring(0,20) + "...";



        GroupNotification groupNotification = new GroupNotification(currentUser.getUid(),myName,groupheader.getGroupKey(),notificationText);

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

            Long tmp = dbLastTimestamp +1;
            groupQuery = groupmessageReference.orderByChild("timeStamp").startAt(tmp);
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

        }


    }

    class GroupChatMessageListener implements ChildEventListener {


        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

            final Message message = dataSnapshot.getValue(Message.class);

            final boolean m_getfromdb = getfromdb;

            if(message.getTimeStamp()!=null ) {

                if (message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {



                            String contentphotourl = Util.saveImage(GroupChatActivity.this, message.getPhotoContentUrl(), message.getPhotoContentName());
                            String userphotourl = Util.saveImage(GroupChatActivity.this, message.getPhotoUrl(), message.getUid());
                            //// TODO: update the db message with local urls

                            if(!m_getfromdb) {
                                String groupphotourl = Util.saveImage(GroupChatActivity.this, groupheader.getPhotoUrl(), groupChatId);
                                chatViewModel.insertChat(new SavedChatsEntity(groupChatId,groupheader.getName(),groupphotourl,false,groupheader.getGroupKey(),null));
                                getfromdb = true;
                            }

                            message.setMessageid(dataSnapshot.getKey());

                            MessageEntity entity = Util.getEntityfromMessage(message, groupChatId, mContext);

                            entity.setPhotoContentUrl(contentphotourl);

                            entity.setPhotoUrl(userphotourl);

                            chatViewModel.insertMessage(entity);

                        }
                    });

                else

                mhandler.post(new Runnable() {
                    @Override
                    public void run() {

                        String userphotourl = Util.saveImage(GroupChatActivity.this, message.getPhotoUrl(), message.getUid());
                        //// TODO: update the db message with local urls

                        if(!m_getfromdb) {
                            String groupphotourl = Util.saveImage(GroupChatActivity.this,groupheader.getPhotoUrl(), groupChatId);
                            chatViewModel.insertChat(new SavedChatsEntity(groupChatId,groupheader.getName(),groupphotourl,false,groupheader.getGroupKey(),null));
                            getfromdb = true;
                        }

                        message.setMessageid(dataSnapshot.getKey());

                        MessageEntity entity = Util.getEntityfromMessage(message, groupChatId, mContext);

                        entity.setPhotoUrl(userphotourl);

                        chatViewModel.insertMessage(entity);

                    }
                });

                message.setMessageid(dataSnapshot.getKey());

                MessageEntity entity = Util.getEntityfromMessage(message, groupChatId, mContext);

                if(!getfromdb)
                {

                    chatViewModel.insertChat(new SavedChatsEntity(groupChatId,groupheader.getName(),groupheader.getPhotoUrl(),false,groupheader.getGroupKey(),null));
                    getfromdb = true;
                }

                chatViewModel.insertMessage(entity);

            }

        }




        @Override
        public void onChildChanged(final DataSnapshot dataSnapshot, String s) {

          final   Message message = dataSnapshot.getValue(Message.class);

              if(message.getTimeStamp()!=null ) {

                if(message.getPhotoContentUrl() != null)

                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {

                          String contentphotourl =  Util.saveImage(GroupChatActivity.this,message.getPhotoContentUrl(),message.getPhotoContentName());
                          String userphotourl =  Util.saveImage(GroupChatActivity.this,message.getPhotoUrl(),message.getUid());
                            //// TODO: update the db message with local urls


                            message.setMessageid(dataSnapshot.getKey());

                            MessageEntity entity = Util.getEntityfromMessage(message, groupChatId, mContext);

                            entity.setPhotoContentUrl(contentphotourl);

                            entity.setPhotoUrl(userphotourl);

                            chatViewModel.insertMessage(entity);

                        }
                    });

                   message.setMessageid(dataSnapshot.getKey());

                   MessageEntity entity = Util.getEntityfromMessage(message,groupChatId,mContext);

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

    @Override
    protected void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }

    private void exitGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to exit the group?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        firebaseDatabase.getReference().child("GroupMemebers").child(groupheader.getGroupKey()).child(currentUser.getUid()).child("current").setValue(false);

                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        return;
                    }
                });

        builder.create().show();


    }

}
