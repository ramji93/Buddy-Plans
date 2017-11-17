package com.chatapp.ramji.buddyplans;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 28-06-2017.
 */

public class MyApplication extends Application {

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean previousState;
    public boolean IsInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    public String userid;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                previousState = IsInBackground;
                MyApplication.this.IsInBackground = true;
                if(previousState == false && IsInBackground == true)
                {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("online").setValue(false);
                }

            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }
        previousState = IsInBackground;
        this.IsInBackground = false;

        if(previousState == true && IsInBackground == false)
        {
            FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("online").setValue(true);
        }

    }



}