package com.chatapp.ramji.buddyplans.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Database;

import com.chatapp.ramji.buddyplans.db.AppDatabase;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;

import java.util.List;

/**
 * Created by user on 09-10-2017.
 */

public class FavouriteChatsViewModel extends AndroidViewModel {

    private AppDatabase db;

    public LiveData<List<SavedChatsEntity>> chats;

    public FavouriteChatsViewModel(Application application) {
        super(application);

        db = AppDatabase.getInMemoryDatabase(application);

        chats = db.savedchatsModel().getSavedChat();
    }

}
