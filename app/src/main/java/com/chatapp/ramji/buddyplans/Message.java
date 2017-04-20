package com.chatapp.ramji.buddyplans;

import java.util.Map;

/**
 * Created by user on 22-02-2017.
 */

public class Message {

    private String Text;
    private String PhotoContentUrl;
    private String PhotoContentName;
    private String userName;
    private Long TimeStamp;
    private String PhotoUrl;

    public String getPhotoContentName() {
        return PhotoContentName;
    }

    public void setPhotoContentName(String photoContentName) {
        PhotoContentName = photoContentName;
    }

    public String getPhotoContentUrl() {
        return PhotoContentUrl;
    }

    public void setPhotoContentUrl(String photoContentUrl) {
        PhotoContentUrl = photoContentUrl;
    }

    public String getPhotoUrl() {
        return PhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        PhotoUrl = photoUrl;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    public Message()
    {

    }


    public Message(String content, String userName, String photoContentUrl,String photoContentName)
    {
        this.Text = content;
        this.userName = userName;
        this.PhotoContentUrl = photoContentUrl;
        this.PhotoContentName = photoContentName;
    }



}
