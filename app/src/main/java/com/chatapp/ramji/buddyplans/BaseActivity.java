package com.chatapp.ramji.buddyplans;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by ramji_v on 11/17/2017.
 */

public class BaseActivity extends AppCompatActivity {


    @Override
    public void onResume() {

        super.onResume();

        MyApplication myApp = (MyApplication) this.getApplication();

        myApp.stopActivityTransitionTimer();

    }

    @Override
    public void onPause() {
        super.onPause();
        ((MyApplication) this.getApplication()).startActivityTransitionTimer();
    }

}
