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
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by ramji_v on 10/7/2017.
 */

@Dao
public interface SavedChatsEntityDAO {

    @Query("SELECT * FROM SavedChatsEntity WHERE SavedChatsEntity.current = 'true'")
    public LiveData<List<SavedChatsEntity>> getSavedChat();

    @Query("SELECT * FROM SavedChatsEntity WHERE SavedChatsEntity.chatid = :chatid")
    public List<SavedChatsEntity> getSavedChatwithid(String chatid);

    @Query("SELECT * FROM SavedChatsEntity WHERE SavedChatsEntity.groupKey IS NOT NULL")
//    @Query("SELECT * FROM SavedChatsEntity WHERE SavedChatsEntity.chatid = '-KnguNtDh138_IrLLrfK'")
    public LiveData<List<SavedChatsEntity>> getGroupChatsSaved();

    @Query("SELECT * FROM SavedChatsEntity WHERE SavedChatsEntity.groupKey IS NULL")
    public LiveData<List<SavedChatsEntity>> getFriendChatsSaved();

    @Insert(onConflict = REPLACE)
    public void insertChats(SavedChatsEntity chatEntity);


}
