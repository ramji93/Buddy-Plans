package com.chatapp.ramji.buddyplans;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.chatapp.ramji.buddyplans.service.AddEventReminderService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

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
            // TODO: get sender uid to display image   
            String senderid = data.get("senderid");
            String chatid = data.get("chatid");
            String imgpath =  Environment.getExternalStorageDirectory().getPath()+"/Buddyplans/pictures"+"/"+chatid;

            Bitmap bitmap = null;
            try {

                bitmap = Glide.with(this).load(imgpath).asBitmap().into(100,100).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


            String group = data.get("groupchat");

            Long eventTime = Long.parseLong(eventTimeString);
            Log.d(MyFirebaseMessagingService.class.getSimpleName(),"inside onMessageReceived()");

            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            cal.setTimeInMillis(eventTime);

            String timeString;

            int minute = cal.get(Calendar.MINUTE);

            if(day==cal.get(Calendar.DAY_OF_MONTH) && month==cal.get(Calendar.MONTH) && year==cal.get(Calendar.YEAR))

            timeString = " At " + cal.get(Calendar.HOUR_OF_DAY)+":"+ ((minute > 9) ? minute : "0"+minute ) + " today";

            else

            timeString = " On " + cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.YEAR)+" "+ cal.get(Calendar.HOUR_OF_DAY)+":"+ ((minute > 9) ? minute : "0"+minute );

            int notificationId = new Random().nextInt();

            Intent intent = new Intent(this, AddEventReminderService.class);
            intent.putExtra("title",title);
            intent.putExtra("description","sent by " + sender + ((group!=null ) ? " in " + group : "" ));
            intent.putExtra("time",eventTime);
            intent.putExtra("notification id", notificationId);

            PendingIntent reminderintent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(this).setContentTitle(((group!=null ) ?  "Group Reminder" : "Reminder") + " from " + sender)
                      .setContentText(title + timeString + " ~ " + "sent by " + sender + ((group!=null ) ? " in " + group : "" ))
                      .setSmallIcon(R.drawable.ic_whatshot_black_24dp)
                      .setAutoCancel(true)
                      .addAction(R.drawable.add_reminder,"Save Reminder",reminderintent);

            if(bitmap!= null)
            builder.setLargeIcon(bitmap);

            Notification n =  builder.build();

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


            notificationManager.notify(notificationId, n);

        }



    }
}
