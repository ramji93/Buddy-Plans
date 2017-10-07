package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;

import com.chatapp.ramji.buddyplans.Location;
import com.chatapp.ramji.buddyplans.Message;

/**
 * Created by ramji_v on 10/7/2017.
 */

@Entity
@TypeConverters(LocationConverter.class)
public class MessageEntity {

    public @PrimaryKey(autoGenerate = true)  int id;
    public String Text;
    public String PhotoContentUrl;
    public String PhotoContentName;
    public String userName;
    public long TimeStamp;
    public String PhotoUrl;
    public String Uid;
    public Location location;
    public String chatId;

    public MessageEntity()
    {}

    public MessageEntity(String Text,String PhotoContentUrl,String PhotoContentName,String userName,long TimeStamp,String PhotoUrl,String Uid,Location location,String chatId)
    {
        this.Text = Text;
        this.PhotoContentUrl = PhotoContentUrl;
        this.PhotoContentName = PhotoContentName;
        this.userName = userName;
        this.TimeStamp = TimeStamp;
        this.PhotoUrl = PhotoUrl;
        this.Uid = Uid;
        this.location = location;
        this.chatId = chatId;

    }



}


