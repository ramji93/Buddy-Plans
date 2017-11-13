package com.chatapp.ramji.buddyplans;

/**
 * Created by user on 22-10-2017.
 */

public class Reminder {

    public String from;

    public String to;

    public String title;

    public String timestamp;

    public String sendername;

    public String chatid;

    public Reminder() {


    }

    public Reminder(String from, String to, String title, String timestamp, String sendername,String chatid) {

        this.from = from;
        this.to = to;
        this.title = title;
        this.timestamp = timestamp;
        this.sendername = sendername;
        this.chatid = chatid;

    }



}

