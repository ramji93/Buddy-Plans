package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

/**
 * Created by user on 21-03-2017.
 */

public class NewGroupListAdapter  extends RecyclerView.Adapter<NewGroupListAdapter.FriendViewHolder> {

    ArrayList<Friend> friendArrayList;
    Context mContext;
    ArrayList<Friend> selectedFriendsList;


    public NewGroupListAdapter(Context context)
    {
        mContext = context;
        friendArrayList = new ArrayList<Friend>();
        selectedFriendsList = new ArrayList<Friend>();

    }



    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

     View view =   LayoutInflater.from(mContext).inflate(R.layout.newgroup_list_item,parent,false);

        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {

        Friend friend = friendArrayList.get(position);

        holder.friendName.setText(friend.getName());

        Glide.with(mContext).load(friend.getPhotourl()).into(holder.profilePhoto);





    }

    @Override
    public int getItemCount() {
        return friendArrayList.size();
    }


    public void add(Friend friend)
    {
        friendArrayList.add(friend);



    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener{

        CheckBox checkBox;
        CircularImageView profilePhoto;
        TextView friendName;


        public FriendViewHolder(View itemView) {
            super(itemView);

            checkBox = (CheckBox) itemView.findViewById(R.id.list_item_checkbox);
            profilePhoto = (CircularImageView) itemView.findViewById(R.id.list_item_photo);
            friendName = (TextView) itemView.findViewById(R.id.list_item_freind_name);
            checkBox.setOnCheckedChangeListener(this);

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

              if(isChecked)
              {

                 selectedFriendsList.add(friendArrayList.get(getAdapterPosition()));


              }

            else
              {

                  selectedFriendsList.remove(friendArrayList.get(getAdapterPosition()));

              }


        }
    }


}
