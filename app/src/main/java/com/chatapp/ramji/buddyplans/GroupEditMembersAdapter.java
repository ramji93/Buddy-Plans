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
 * Created by user on 23-06-2017.
 */

public class GroupEditMembersAdapter extends RecyclerView.Adapter<GroupEditMembersAdapter.MemberViewHolder>  {


    ArrayList<User> memberlist;

    Context mcontext;

    GroupEditMembersAdapter( Context context)
    {
        memberlist = new ArrayList<User>();
        mcontext = context;
    }


    @Override
    public GroupEditMembersAdapter.MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mcontext).inflate(R.layout.group_create_selected_item,parent,false);

        return new MemberViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, final int position) {
        final int pos = position;
        Glide.with(mcontext).load(memberlist.get(position).getProfileDP()).into(holder.imageView);

        holder.selectedFriendName.setText(memberlist.get(position).getUserName());

        if(memberlist.get(position).geteMail() != null)
        {
         holder.deleteView.setVisibility(View.GONE);
        }
        else
        {

            holder.entireView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DeleteSelectedItemListener deleteSelectedItemListener = (DeleteSelectedItemListener) mcontext;
                    deleteSelectedItemListener.ondeleteselecteditem(memberlist.get(position));
                    memberlist.remove(position);
                    notifyDataSetChanged();


                }
            });



        }



    }


    @Override
    public int getItemCount() {
        return memberlist.size();
    }




    class MemberViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.selected_item_circleimage)
        CircularImageView imageView;
        @BindView(R.id.selected_item)
        ViewGroup entireView;
        @BindView(R.id.selected_friend_name)
        TextView selectedFriendName;
        @BindView(R.id.delete_frame)
        ViewGroup deleteView;


        public MemberViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this,itemView);




        }



    }

    public void add(User user)
    {
        memberlist.add(user);
    }


    public interface DeleteSelectedItemListener{

        public void ondeleteselecteditem(User user);



    }


}
