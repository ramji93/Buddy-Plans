package com.chatapp.ramji.buddyplans;

/**
 * Created by user on 10-07-2017.
 */

public class NotificationMessage {

   public String from;

    public String to;

    public String text;

    public NotificationMessage()
    {



    }


    public NotificationMessage(String fromparam, String toparam, String textparam)
    {

        from = fromparam;

        to = toparam;

        text = textparam;

    }

}
