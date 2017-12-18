package com.chatapp.ramji.buddyplans;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ramji_v on 10/7/2017.
 */

public class ServiceData implements Serializable {

    public String chatid;

    public String chatName;

    public String chatProfileImageUrl;

    public String groupKey;

    public ArrayList<Message> messages;

    public ServiceData(String chatid, String chatName, String chatProfileImageUrl, ArrayList<Message> messages, String groupKey) {
        this.chatid = chatid;
        this.chatName = chatName;
        this.chatProfileImageUrl = chatProfileImageUrl;
        this.messages = messages;
        this.groupKey = groupKey;

    }

}
