package com.chatapp.ramji.buddyplans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatReference;
    @BindView(R.id.chat_messages)
    RecyclerView chatMessagesView;
    private Friend friend;
    private String friendUid;
    private String myUid;
    private String chatId;
    @BindView(R.id.send_button)
    Button sendButton;
    @BindView(R.id.message_input)
    EditText messageInput;
    private String myName;
    Messages_Adapter messages_adapter;
    ChatMessageListener chatMessageListener;
    @BindView(R.id.chat_toolbar)
    Toolbar toolbar;
    @BindView(R.id.chat_title)
    TextView chatTitle;



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

        User currentUser;

        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentUser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);
        myName = currentUser.getUserName();
        chatTitle.setText(friend.getName());
        myUid = currentUser.getUid();
        friendUid = friend.getUid();
        chatId = friend.getChatid();

         messages_adapter = new Messages_Adapter(this,myName);
        chatMessagesView.setLayoutManager(new LinearLayoutManager(this){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                super.smoothScrollToPosition(recyclerView, state, position);
            }
        });

        chatMessagesView.setAdapter(messages_adapter);





//        if(friend.getChatid() == null)
//            createChatId();


    }


     public void createChatId()
     {

         DatabaseReference ChatReference = firebaseDatabase.getReference().child("Messages");

          chatId = ChatReference.push().getKey();

         friend.setChatid(chatId);

         firebaseDatabase.getReference().child("Friends").child(myUid).child(friendUid).setValue(friend);

         firebaseDatabase.getReference().child("Friends").child(friendUid).child(myUid).child("chatid").setValue(chatId);


     }

    @OnClick(R.id.send_button)
    public void SendMessage()
    {



        boolean addListener = false;


        if(friend.getChatid() == null) {
            createChatId();
            addListener = true;
        }






         DatabaseReference messageReference = firebaseDatabase.getReference().child("Messages").child(chatId);


        Message message = new Message(messageInput.getText().toString(),myName,null,null);

            messageReference.push().setValue(message);

        if(addListener)
        {
            chatMessageListener = new ChatMessageListener();
            chatReference = firebaseDatabase.getReference().child("Messages").child(chatId);
            chatReference.addChildEventListener(chatMessageListener);

        }

        messageInput.setText("");

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

    class ChatMessageListener implements ChildEventListener{


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            messages_adapter.messages.add(message);

            messages_adapter.notifyDataSetChanged();

            chatMessagesView.scrollToPosition(messages_adapter.getItemCount()-1);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            messages_adapter.messages.add(message);

            messages_adapter.notifyDataSetChanged();

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
