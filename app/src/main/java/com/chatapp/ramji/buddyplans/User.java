package com.chatapp.ramji.buddyplans;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by user on 22-02-2017.
 */

public class User implements Serializable {

    private String profileDP;
    private String userName;
    private String eMail;
    private String uid;
    private String fb_id;


    public  User(String profiledp,String username,String email,String uid)
    {
        this.profileDP = profiledp;
        this.userName = username;
        this.eMail = email;
        this.uid = uid;

    }

    public User()
    {

    }

    public String getFb_id() {
        return fb_id;
    }

    public void setFb_id(String fb_id) {
        this.fb_id = fb_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileDP() {
        return profileDP;
    }

    public void setProfileDP(String profileDP) {
        this.profileDP = profileDP;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }
}
