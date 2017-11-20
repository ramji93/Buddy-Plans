package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 07-04-2017.
 */

public class GroupListAdapter extends ArrayAdapter<Groupheader> {


    Context mcontext;

    List<Groupheader> groups;

    HashMap<String,Groupheader> grouphashmap;

    Boolean isConnected;

    public GroupListAdapter(Context context, List<Groupheader> objects) {
        super(context, R.layout.group_list_item , objects);

        mcontext = context;
        groups = objects;

        grouphashmap = new HashMap<String, Groupheader>();

        isConnected = Util.checkConnection(context);
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View groupItemView;

        if(convertView == null)
        {

            LayoutInflater inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            groupItemView  = inflater.inflate(R.layout.group_list_item, parent, false);

        }

        else

        {

            ((TextView) convertView.findViewById(R.id.timestamp)).setText("");

            ((TextView) convertView.findViewById(R.id.lastmessage)).setText("");

            groupItemView = convertView;


        }

        TextView groupNameView = (TextView) groupItemView.findViewById(R.id.group_item_name);

        CircularImageView groupPhotoView = (CircularImageView) groupItemView.findViewById(R.id.group_item_photo);



        ViewCompat.setTransitionName(groupPhotoView,groups.get(position).getName());

        groupNameView.setText(groups.get(position).getName());

        if(groups.get(position).getPhotoUrl()!=null)

        Glide.with(mcontext).load(groups.get(position).getPhotoUrl()).asBitmap().diskCacheStrategy(isConnected ? DiskCacheStrategy.RESULT : DiskCacheStrategy.NONE).into(groupPhotoView);

        TextView lastmessage = (TextView) groupItemView.findViewById(R.id.lastmessage);

        if(groups.get(position).getLastMessage() != null)

        lastmessage.setText(groups.get(position).getLastMessage());

        TextView timestamp = (TextView) groupItemView.findViewById(R.id.timestamp);

        if(groups.get(position).getLastMessageTimestap() != null)

        timestamp.setText(Util.getDate(groups.get(position).getLastMessageTimestap()));

        return groupItemView;

    }

    @Override
    public void add(Groupheader object) {

        groups.add(object);
        grouphashmap.put(object.getGroupKey(),object);




    }


}
