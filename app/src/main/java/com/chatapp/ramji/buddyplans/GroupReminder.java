package com.chatapp.ramji.buddyplans;

/**
 * Created by user on 23-10-2017.
 */

public class GroupReminder {

    public String senderId;

    public String groupId;

    public String title;

    public String timestamp;

    public String sendername;

    public String groupchat;

    public String chatid;


    public GroupReminder() {


    }

    public GroupReminder(String senderId, String groupId, String title, String timestamp, String sendername, String groupchat, String chatid) {

        this.senderId = senderId;
        this.groupId = groupId;
        this.title = title;
        this.timestamp = timestamp;
        this.sendername = sendername;
        this.groupchat = groupchat;
        this.chatid = chatid;

    }


}
