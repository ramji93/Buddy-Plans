package com.chatapp.ramji.buddyplans;

/**
 * Created by user on 17-07-2017.
 */

public class GroupNotification {

public String senderId;

public String senderName;

public String groupId;

public String chatId;

public String groupName;

public String text;


    public GroupNotification()
    {


    }

    public GroupNotification(String senderId,String senderName,String groupId,String chatId,String groupName,String text)
    {

        this.senderId = senderId;
        this.senderName = senderName;
        this.groupId = groupId;
        this.chatId = chatId;
        this.groupName = groupName;
        this.text = text;


    }


}
