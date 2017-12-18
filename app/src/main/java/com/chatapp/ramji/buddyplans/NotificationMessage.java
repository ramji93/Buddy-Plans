package com.chatapp.ramji.buddyplans;

/**
 * Created by user on 10-07-2017.
 */

public class NotificationMessage {

   public String from;

    public String to;

    public String text;

    public String sendername;

    public String chatid;

    public NotificationMessage()
    {



    }


    public NotificationMessage(String fromparam,String nameparam, String toparam,String chatparam, String textparam)
    {

        from = fromparam;

        to = toparam;

        sendername = nameparam;

        text = textparam;

        chatid = chatparam;

    }

}
