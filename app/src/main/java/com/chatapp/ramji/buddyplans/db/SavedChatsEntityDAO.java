package com.chatapp.ramji.buddyplans.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.chatapp.ramji.buddyplans.Location;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * Created by ramji_v on 10/7/2017.
 */

@Dao
public interface SavedChatsEntityDAO {

//    @Query("SELECT * FROM SavedChatsEntity WHERE SavedChatsEntity.current = true")
    @Query("SELECT * FROM SavedChatsEntity")
    public LiveData<List<SavedChatsEntity>> getSavedChat();

    @Insert(onConflict = IGNORE)
    public void insertChats(SavedChatsEntity chatEntity);


}
