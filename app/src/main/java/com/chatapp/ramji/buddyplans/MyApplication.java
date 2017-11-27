package com.chatapp.ramji.buddyplans;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.images.ImageManager;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 28-06-2017.
 */

public class MyApplication extends Application {

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean previousState = true;
    public boolean IsInBackground = true;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    public String userid;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {


        Glide.get(getApplicationContext()).clearMemory();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(getApplicationContext()).clearDiskCache();
            }
        }).start();
        super.onCreate();


    }


    public void startActivityTransitionTimer() {
//        this.mActivityTransitionTimer = new Timer();
//        this.mActivityTransitionTimerTask = new TimerTask() {
//            public void run() {
                previousState = IsInBackground;
                MyApplication.this.IsInBackground = true;
                if(previousState != true && IsInBackground == true && userid != null )
                {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("online").setValue(false);
                }
//
//            }
//        };
//
//        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, 0,
//                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }
        previousState = IsInBackground;
        this.IsInBackground = false;

        if(previousState !=false && IsInBackground == false && userid != null)
        {
            FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("online").setValue(true);
        }

    }



}