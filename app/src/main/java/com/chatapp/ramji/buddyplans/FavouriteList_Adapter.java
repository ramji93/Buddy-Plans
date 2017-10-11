package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;

import java.io.File;
import java.util.List;

/**
 * Created by user on 09-10-2017.
 */

public class FavouriteList_Adapter extends RecyclerView.Adapter<FavouriteList_Adapter.FavriteViewHolder> {

    Context mcontext;
    String[] groupnames;
    List<SavedChatsEntity> savedChatsEntities;


    public FavouriteList_Adapter(Context context,List<SavedChatsEntity> entities)
    {
        mcontext = context;
        this.savedChatsEntities = entities;

    }

    @Override
    public FavriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext).inflate(R.layout.favourite_list_item,parent,false);

        FavriteViewHolder viewHolder = new FavriteViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FavriteViewHolder holder, int position) {

        //Glide.with(mcontext).load(savedChatsEntities.get(position).chatProfileImageurl).into(holder.imageView);

        File fdir = new File(savedChatsEntities.get(position).chatProfileImageurl);

        if(fdir.exists())
        Glide.with(mcontext).load(fdir).into(holder.imageView);

        holder.groupName.setText(savedChatsEntities.get(position).chatName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return savedChatsEntities.size();
    }

 class FavriteViewHolder extends RecyclerView.ViewHolder
 {
      ImageView imageView;

      TextView groupName;

     public FavriteViewHolder(View itemView) {
         super(itemView);

         imageView = (ImageView) itemView.findViewById(R.id.fav_item_image);
         groupName = (TextView) itemView.findViewById(R.id.fav_item_text);

     }
 }



}
