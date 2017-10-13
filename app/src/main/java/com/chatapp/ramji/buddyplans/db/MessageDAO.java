package com.chatapp.ramji.buddyplans.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.chatapp.ramji.buddyplans.Location;

import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by ramji_v on 10/7/2017.
 */

@Dao
@TypeConverters(LocationConverter.class)
public interface MessageDAO {

//    @Query("SELECT * FROM MessageEntity WHERE MessageEntity.chatId = :chatid")
    @Query("SELECT * FROM MessageEntity where chatId = :chatid")
    public LiveData<List<MessageEntity>> getMessagesByChatid(String chatid);

    @Query("SELECT MAX(TimeStamp) FROM MessageEntity where chatId = :chatid")
    public LiveData<Long> getLastTimestamp(String chatid);

    @Insert(onConflict = REPLACE)
    public void insertMessages(MessageEntity messageEntity);

    @Insert(onConflict = REPLACE)
    public void insertMultipleMessages(ArrayList<MessageEntity> messageEntities);



}
