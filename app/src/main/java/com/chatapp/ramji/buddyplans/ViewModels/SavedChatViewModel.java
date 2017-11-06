package com.chatapp.ramji.buddyplans.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.chatapp.ramji.buddyplans.db.AppDatabase;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;

import java.util.List;

/**
 * Created by user on 16-10-2017.
 */

public class SavedChatViewModel extends AndroidViewModel {

    private AppDatabase db;

    public LiveData<List<SavedChatsEntity>> savedChats_group;

    public LiveData<List<SavedChatsEntity>> savedChats_friend;

    public SavedChatViewModel(Application application) {

        super(application);

        db = AppDatabase.getDatabase(application);
    }

    public void getSavedGroupChats(){

        savedChats_group = db.savedchatsModel().getGroupChatsSaved();
    }

    public void getSavedFriendChats(){

        savedChats_friend = db.savedchatsModel().getFriendChatsSaved();

    }

    public void setChatActive(String chatid)
    {
        List<SavedChatsEntity> chats = db.savedchatsModel().getSavedChatwithid(chatid);
        if(chats.size()>0){
        SavedChatsEntity chat = chats.get(0);
        chat.active = true;
        db.savedchatsModel().updateFavouriteChat(chat);
    }

    }

    public void setChatInactive(String chatid)
    {
        List<SavedChatsEntity> chats = db.savedchatsModel().getSavedChatwithid(chatid);
        if(chats.size()>0) {
            SavedChatsEntity chat = chats.get(0);
            chat.active = false;
            db.savedchatsModel().updateFavouriteChat(chat);
        }

    }


}
