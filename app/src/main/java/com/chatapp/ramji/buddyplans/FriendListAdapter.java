package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by user on 26-02-2017.
 */

public class FriendListAdapter extends ArrayAdapter<Friend>

{
    Context context;

    List<Friend> friends;

    HashMap<String, Friend> friendHashMap;

    Boolean isConnected;

    public FriendListAdapter(Context context, List<Friend> objects) {
        super(context, R.layout.friend_list_item, objects);

        friends = objects;

        this.context = context;

        friendHashMap = new HashMap<String, Friend>();

        isConnected = Util.checkConnection(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View friendListItem;

        if (convertView == null)

        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            friendListItem = inflater.inflate(R.layout.friend_list_item, parent, false);

        } else

        {

            ((TextView) convertView.findViewById(R.id.timestamp)).setText("");

            ((TextView) convertView.findViewById(R.id.lastmessage)).setText("");

            friendListItem = convertView;


        }

        TextView friendNameView = (TextView) friendListItem.findViewById(R.id.friendName);

        CircularImageView imageView = (CircularImageView) friendListItem.findViewById(R.id.friend_item_photo);

        ViewCompat.setTransitionName(imageView, friends.get(position).getName());

        Glide.with(context).load(friends.get(position).getPhotourl()).asBitmap().diskCacheStrategy(isConnected ? DiskCacheStrategy.RESULT : DiskCacheStrategy.NONE).into(imageView);

        friendNameView.setText(friends.get(position).getName());

        TextView lastmessage = (TextView) friendListItem.findViewById(R.id.lastmessage);

        if (friends.get(position).getLastMessage() != null)

            lastmessage.setText(friends.get(position).getLastMessage());

        TextView timestamp = (TextView) friendListItem.findViewById(R.id.timestamp);

        if (friends.get(position).getLastMessageTimestap() != null)

            timestamp.setText(Util.getDate(friends.get(position).getLastMessageTimestap()));


        return friendListItem;


    }

    @Override
    public void add(Friend object) {

        friends.add(object);

        friendHashMap.put(object.getUid(), object);


    }


    public boolean checkFriendExists(String userUid) {

        return friendHashMap.containsKey(userUid);

    }


}
