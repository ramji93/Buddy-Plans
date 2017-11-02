package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ramji_v on 10/7/2017.
 */
@Entity
public class SavedChatsEntity {

    public @PrimaryKey @NonNull String chatid;
    public String chatName;
    public String chatProfileImageurl;
    public boolean favourite;
    public String groupKey;
    public String friendUid;
    public boolean active;

    public SavedChatsEntity()
    {

    }

    public SavedChatsEntity(String chatid,String chatName,String chatProfileImageurl,boolean favourite,String groupKey,String friendUid)
    {
      this.chatid = chatid;
      this.chatName = chatName;
      this.chatProfileImageurl = chatProfileImageurl;
      this.favourite = favourite;
      this.groupKey = groupKey;
      this.friendUid = friendUid;
      this.active = true;
    }

}



