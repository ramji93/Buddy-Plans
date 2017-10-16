package com.chatapp.ramji.buddyplans.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.chatapp.ramji.buddyplans.db.AppDatabase;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;

/**
 * Created by user on 16-10-2017.
 */

public class SavedChatViewModel extends AndroidViewModel {

    private AppDatabase db;

    public LiveData<SavedChatsEntity> savedChats_group;

    public LiveData<SavedChatsEntity> savedChats_friend;

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


}
