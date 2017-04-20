package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.util.ArrayList;
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

    public FriendListAdapter(Context context, List<Friend> objects) {
        super(context, R.layout.friend_list_item, objects);

        friends = objects;

        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View friendListItem;

       if(convertView == null)

       {
           LayoutInflater inflater = (LayoutInflater) context
                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

           friendListItem  = inflater.inflate(R.layout.friend_list_item, parent, false);

       }

        else

        friendListItem = convertView;

        TextView friendNameView = (TextView) friendListItem.findViewById(R.id.friendName);

        friendNameView.setText(friends.get(position).getName());

        return friendNameView;


    }

    @Override
    public void add(Friend object) {

        friends.add(object);


    }


}
