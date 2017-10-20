package com.chatapp.ramji.buddyplans.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.chatapp.ramji.buddyplans.GroupChatActivity;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ramji_v on 10/20/2017.
 */

public class AddEventReminderService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AddEventReminderService(String name) {
        super(name);
    }

    public AddEventReminderService() {
        super("AddEventReminderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {


        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        Long eventTime = intent.getLongExtra("time",0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        ContentValues eventValues = new ContentValues();

        eventValues.put("calendar_id", 3);
        eventValues.put("title", title);
        eventValues.put("description",description);
        //// TODO: 18-10-2017 description



//            long startDate = c.getTimeInMillis();



        long startDate = eventTime;
        long endDate = startDate + 1000 * 60 * 60;

        eventValues.put("dtstart", startDate);
        eventValues.put("dtend", endDate);
        eventValues.put("allDay", false);
        eventValues.put("eventStatus", 0);

        TimeZone timeZone = TimeZone.getDefault();
        eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());

        eventValues.put(CalendarContract.Events.HAS_ALARM, 1);

        ContentResolver cr = this.getContentResolver();


        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, eventValues);

        long eventID = Long.parseLong(uri.getLastPathSegment());

        Log.d(GroupChatActivity.class.getName(),"event uri: "+ uri.toString());

        ContentValues reminders = new ContentValues();
        reminders.put(CalendarContract.Reminders.EVENT_ID, eventID);
        reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        reminders.put(CalendarContract.Reminders.MINUTES, 5);

        Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);

        Log.d(GroupChatActivity.class.getName(),"reminder uri: "+ uri2.toString());


    }
}
