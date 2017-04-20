package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 07-04-2017.
 */

public class GroupListAdapter extends ArrayAdapter<Groupheader> {


    Context mcontext;

    List<Groupheader> groups;

    public GroupListAdapter(Context context, List<Groupheader> objects) {
        super(context, R.layout.group_list_item , objects);

        mcontext = context;
        groups = objects;

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

            groupItemView = convertView;

        TextView groupNameView = (TextView) groupItemView.findViewById(R.id.group_item_name);

        groupNameView.setText(groups.get(position).getName());

        return groupItemView;

    }

    @Override
    public void add(Groupheader object) {

        groups.add(object);


    }


}
