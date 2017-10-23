package com.chatapp.ramji.buddyplans.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.chatapp.ramji.buddyplans.db.AppDatabase;
import com.chatapp.ramji.buddyplans.db.MessageEntity;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;

import java.util.List;

/**
 * Created by ramji_v on 10/12/2017.
 */

public class ChatViewModel  extends AndroidViewModel {

    private AppDatabase db;

    public List<SavedChatsEntity> savedchat;

    public Long lastTimestamp;

    public LiveData<Long> lastTimestampLive;

    public LiveData<List<MessageEntity>> messages;


    public ChatViewModel(Application application) {

        super(application);

        db = AppDatabase.getDatabase(application);
    }

    public void getSavedChat(String chatid)
    {
        savedchat = db.savedchatsModel().getSavedChatwithid(chatid);

        lastTimestamp = db.messageModel().getLastTimestamp(chatid);

        lastTimestampLive =  db.messageModel().getLastTimestampLive(chatid);


    }


    public void refreshchat(String chatid)
    {
        savedchat =  db.savedchatsModel().getSavedChatwithid(chatid);
    }

    public void getmessages(String chatid)
    {

        messages = db.messageModel().getMessagesByChatid(chatid);
    }

    public void insertMessage(MessageEntity messageEntity)
    {
          db.messageModel().insertMessages(messageEntity);
    }

    public void insertChat(SavedChatsEntity savedChatsEntity)
    {
        db.savedchatsModel().insertChats(savedChatsEntity);
    }


    public void setFavouriteChat(String chatid)
    {
       List<SavedChatsEntity> chats = db.savedchatsModel().getSavedChatwithid(chatid);
       SavedChatsEntity chat = chats.get(0);
       chat.favourite = true;
       db.savedchatsModel().updateFavouriteChat(chat);

    }

    public void setNotFavouriteChat(String chatid)
    {
        List<SavedChatsEntity> chats = db.savedchatsModel().getSavedChatwithid(chatid);
        SavedChatsEntity chat = chats.get(0);
        chat.favourite = false;
        db.savedchatsModel().updateFavouriteChat(chat);

    }


}
