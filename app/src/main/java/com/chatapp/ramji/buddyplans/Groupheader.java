package com.chatapp.ramji.buddyplans;

import java.io.Serializable;

/**
 * Created by user on 05-04-2017.
 */

public class Groupheader implements Serializable{

    private String name;

    private String chatId;

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    private String lastMessage;

    private Long lastMessageTimestap;

    private String groupKey;


    public  Groupheader(String name,String chatId)
    {

        this.name = name;

        this.chatId = chatId;

    }

    public  Groupheader()
    {

    }


    public Long getLastMessageTimestap() {
        return lastMessageTimestap;
    }

    public void setLastMessageTimestap(Long lastMessageTimestap) {
        this.lastMessageTimestap = lastMessageTimestap;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
