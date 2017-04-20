package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by user on 10-03-2017.
 */

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserList_ViewHolder> {


    Context mContext;
    ArrayList<User> userList;


    @Override
    public UserList_ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.usersearch_list_item,parent,false);

        return new UserList_ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserList_ViewHolder holder, int position) {


        holder.userNameView.setText(userList.get(position).getUserName());


    }

    public UserSearchAdapter(Context context)

    {

         mContext =   context;
         userList = new ArrayList<User>();


    }




    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class UserList_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView userNameView;


        public UserList_ViewHolder(View view)
        {

            super(view);

            userNameView = (TextView) view.findViewById(R.id.username);

            view.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mContext,UserActivity.class);

            intent.putExtra("User",userList.get(getAdapterPosition()));

            mContext.startActivity(intent);



        }
    }


}
