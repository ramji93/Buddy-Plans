package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by ramji_v on 10/7/2017.
 */
@Entity
public class SavedChatsEntity {

    public @PrimaryKey String chatid;
    public String chatName;
    public String chatProfileImageurl;
    public boolean current;

    public SavedChatsEntity()
    {

    }

    public SavedChatsEntity(String chatid,String chatName,String chatProfileImageurl,boolean current)
    {
      this.chatid = chatid;
      this.chatName = chatName;
      this.chatProfileImageurl = chatProfileImageurl;
      this.current = current;

    }

}



