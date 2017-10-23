package com.chatapp.ramji.buddyplans;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import com.chatapp.ramji.buddyplans.service.AddEventReminderService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 09-07-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {

            Map<String,String> data = remoteMessage.getData();
            String title = data.get("title");
            String eventTimeString = data.get("time");
            String sender = data.get("sender");
           // String chatid = reminderData.get("chatid");

            String group = data.get("groupchat");

            Long eventTime = Long.parseLong(eventTimeString);
            Log.d(MyFirebaseMessagingService.class.getSimpleName(),"inside onmeonMessageReceived()");

            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            cal.setTimeInMillis(eventTime);

            String timeString;

            if(day==cal.get(Calendar.DAY_OF_MONTH) && month==cal.get(Calendar.MONTH) && year==cal.get(Calendar.YEAR))

            timeString = "At " + cal.get(Calendar.HOUR_OF_DAY)+":"+ cal.get(Calendar.MINUTE)+ " today";

            else

            timeString = "On " + cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.YEAR)+" "+ cal.get(Calendar.HOUR_OF_DAY)+":"+ cal.get(Calendar.MINUTE);

            Intent intent = new Intent(this, AddEventReminderService.class);
            intent.putExtra("title",title);
            intent.putExtra("description","Set by "+sender);
            intent.putExtra("time",eventTime);

            PendingIntent reminderintent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);


            Notification n = new Notification.Builder(this).setContentTitle(sender + " sent an reminder")
                      .setContentText(title + timeString)
                      .setSmallIcon(R.drawable.ic_whatshot_black_24dp)
                      .setAutoCancel(true)
                      .addAction(R.drawable.add_reminder,"Add Reminder",reminderintent).build();

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify((int) Calendar.getInstance().getTimeInMillis(), n);


        }



    }
}
