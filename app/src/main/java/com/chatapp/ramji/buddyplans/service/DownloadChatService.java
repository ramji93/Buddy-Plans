package com.chatapp.ramji.buddyplans.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chatapp.ramji.buddyplans.Location;
import com.chatapp.ramji.buddyplans.Message;
import com.chatapp.ramji.buddyplans.ServiceData;
import com.chatapp.ramji.buddyplans.Util;
import com.chatapp.ramji.buddyplans.db.AppDatabase;
import com.chatapp.ramji.buddyplans.db.MessageEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramji_v on 10/7/2017.
 */

public class DownloadChatService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DownloadChatService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(DownloadChatService.class.getName(),"onHandleIntent begins");
       ServiceData data = (ServiceData) intent.getSerializableExtra("data");
       String chatId = data.chatid;
        List<Message> messages = data.messages;
        AppDatabase mDB = AppDatabase.getInMemoryDatabase(this);

        ArrayList<MessageEntity> dbmessageList = new ArrayList<MessageEntity>();

        for(Message message : messages) {

            MessageEntity dbmessage;
            String dbphotoContentUrl=null;
            String dbphotoContentName=null;
            String dbText=null;
            String dbUserName=null;
            long dbTimestamp;
            String dbuserid=null;
            String dbUserPhotoUrl=null;
            Location dbLocation=null;
            String dbChatid=null;


            if(message.getPhotoContentUrl()!=null)
            {
                dbphotoContentUrl =  Util.saveImage(this,message.getPhotoContentUrl(),message.getPhotoContentName());
                dbphotoContentName = message.getPhotoContentName();
            }

            if(message.getText()!=null)
            {
                dbText = message.getText();
            }

            dbUserName = message.getUserName();

            dbTimestamp = message.getTimeStamp();

            dbuserid = message.getUid();

            if(message.getPhotoUrl()!= null)
            {
                dbUserPhotoUrl = Util.saveImage(this,message.getPhotoUrl(),message.getUid());
            }

            if(message.getLocation()!=null)
            {
                dbLocation = message.getLocation();
            }

            if(chatId!=null) {
                dbChatid = chatId;
            }

          //  dbmessage = new MessageEntity(dbText,dbphotoContentUrl,dbphotoContentName,dbUserName,dbTimestamp,dbUserPhotoUrl,dbuserid,dbLocation,dbChatid);

          //  dbmessageList.add(dbmessage);

        }

        String dbChatname = data.chatName;
        String dbChatProfileImageUrl = null;
        if(data.chatProfileImageUrl != null) {
            dbChatProfileImageUrl  = Util.saveImage(this,data.chatProfileImageUrl,chatId);
        }

//        mDB.savedchatsModel().insertChats(new SavedChatsEntity(chatId,dbChatname,dbChatProfileImageUrl,true));
//        mDB.messageModel().insertMultipleMessages(dbmessageList);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedchats = sharedPreferences.getString("savedchats","");
        if(!savedchats.contains(chatId))
        {
         SharedPreferences.Editor editor = sharedPreferences.edit();
            StringBuffer stringBuffer = new StringBuffer(savedchats);
            stringBuffer.append(" "+chatId+" ");
            editor.putString("savedchats",stringBuffer.toString());
            editor.commit();
        }


    }



}
