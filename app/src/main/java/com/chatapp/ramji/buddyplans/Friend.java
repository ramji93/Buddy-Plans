package com.chatapp.ramji.buddyplans;

import java.io.Serializable;

/**
 * Created by user on 26-02-2017.
 */

public class Friend implements Serializable {

    private String name;

    private String photourl;

    private String lastMessage;

    private Long lastMessageTimestap;

    private boolean active;

    private String uid;

    private String chatid;

    public String getChatid() {
        return chatid;
    }

    public void setChatid(String chatid) {
        this.chatid = chatid;
    }


    public Friend(String nameparam, String photourlparam, boolean active, String uid) {

        this.name = nameparam;
        this.photourl = photourlparam;
        this.active = active;
        this.uid = uid;


    }

    public Friend() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastMessageTimestap() {
        return lastMessageTimestap;
    }

    public void setLastMessageTimestap(Long lastMessageTimestamp) {
        this.lastMessageTimestap = lastMessageTimestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
