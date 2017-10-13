package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.chatapp.ramji.buddyplans.Location;
import com.chatapp.ramji.buddyplans.Message;

/**
 * Created by ramji_v on 10/7/2017.
 */

@Entity
public class MessageEntity {

    public @PrimaryKey @NonNull String messageid;
    public String Text;
    public String PhotoContentUrl;
    public String PhotoContentName;
    public String userName;
    public long TimeStamp;
    public String PhotoUrl;
    public String Uid;
    @TypeConverters(LocationConverter.class)
    public Location location;
    public String chatId;

    public MessageEntity()
    {}

    public MessageEntity(String messageid,String Text,String PhotoContentUrl,String PhotoContentName,String userName,long TimeStamp,String PhotoUrl,String Uid,Location location,String chatId)
    {
        this.messageid = messageid;
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

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getPhotoContentUrl() {
        return PhotoContentUrl;
    }

    public void setPhotoContentUrl(String photoContentUrl) {
        PhotoContentUrl = photoContentUrl;
    }

    public String getPhotoContentName() {
        return PhotoContentName;
    }

    public void setPhotoContentName(String photoContentName) {
        PhotoContentName = photoContentName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getPhotoUrl() {
        return PhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        PhotoUrl = photoUrl;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}


