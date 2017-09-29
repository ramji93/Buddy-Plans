package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by user on 11-05-2017.
 */

public class GroupCreateFriendListAdapter extends RecyclerView.Adapter<GroupCreateFriendListAdapter.GC_FriendItem_ViewHolder> {

   ArrayList<Friend> friendList;
    Context mContext;


    public GroupCreateFriendListAdapter(Context context)
    {
        mContext = context;
        friendList = new ArrayList<Friend>();
    }


    @Override
    public GC_FriendItem_ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.group_create_friend_item,parent,false);


        return new GC_FriendItem_ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GC_FriendItem_ViewHolder holder, int position) {

        Glide.with(mContext)
                .load(friendList.get(position).getPhotourl())
                .into(holder.friend_photo);

        holder.friend_name.setText(friendList.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class GC_FriendItem_ViewHolder extends  RecyclerView.ViewHolder
    {

        @BindView(R.id.group_create_friend_photo)
        CircularImageView friend_photo;
        @BindView(R.id.group_create_friend_name)
        TextView friend_name;



        public GC_FriendItem_ViewHolder(View itemView) {


            super(itemView);
            ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FriendSelectListener friendSelectListener = (FriendSelectListener) mContext;
                    friendSelectListener.onFriendSelect(friendList.get(getAdapterPosition()));
                    friendList.remove(getAdapterPosition());
                    notifyDataSetChanged();

                }
            });

        }


    }

    public void add(Friend friend)
    {

        friendList.add(friend);

    }


    public interface FriendSelectListener
    {

        public void onFriendSelect(Friend friend);



    }


}
