package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.chatapp.ramji.buddyplans.Location;

import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * Created by ramji_v on 10/7/2017.
 */

@Dao
@TypeConverters(Location.class)
public interface MessageDAO {

    @Query("SELECT * FROM MessageEntity where chatId = :chatid")
    public List<MessageEntity> getMessagesByChatid(String chatid);


    @Insert
    public void insertMessages(MessageEntity messageEntity);

    @Insert
    public void insertMultipleMessages(ArrayList<MessageEntity> messageEntities);



}
