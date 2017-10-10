package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by user on 09-10-2017.
 */

public class FavouriteList_Adapter extends RecyclerView.Adapter<FavouriteList_Adapter.FavriteViewHolder> {

    Context mcontext;
    String[] groupnames;

    public FavouriteList_Adapter(Context context,String[] strings)
    {
        mcontext = context;
        groupnames = strings;

    }

    @Override
    public FavriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext).inflate(R.layout.favourite_list_item,parent,false);

        FavriteViewHolder viewHolder = new FavriteViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FavriteViewHolder holder, int position) {

        holder.groupName.setText(groupnames[position]);

    }


    @Override
    public int getItemCount() {
        return groupnames.length;
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
