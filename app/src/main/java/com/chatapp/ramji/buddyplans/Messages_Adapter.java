package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by user on 12-03-2017.
 */

public class Messages_Adapter extends RecyclerView.Adapter<Messages_Adapter.Message_ViewHolder>

{
    private Context mContext;
    ArrayList<Message> messages;
    String myName;
    HashMap<String,Message> messageMap = new HashMap<String,Message>();

    private final int OTHERS = 1;
    private final int MINE = 2;



    public Messages_Adapter(Context context,String name)
    {

        mContext = context;
        messages = new ArrayList<Message>();
        myName = name;


    }


    @Override
    public Message_ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if(viewType == OTHERS)
            view = LayoutInflater.from(mContext).inflate(R.layout.message_list_item,parent,false);

        else
            view = LayoutInflater.from(mContext).inflate(R.layout.message_list_item_right,parent,false);


        return new Message_ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Message_ViewHolder holder, final int position) {


        holder.author.setText(messages.get(position).getUserName());

        if(messages.get(position).getPhotoUrl()!=null)
            Glide.with(mContext).load(messages.get(position).getPhotoUrl()).into(holder.userPhoto);

         if(messages.get(position).getPhotoContentUrl()!=null && messages.get(position).getText() == null)
         {
             holder.contentPhoto.setVisibility(View.VISIBLE);
             holder.messageContents.setVisibility(View.GONE);
             Glide.with(mContext).load(messages.get(position).getPhotoContentUrl()).into(holder.contentPhoto);
             holder.contentPhoto.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {

                     String path = Environment.getExternalStorageDirectory().getPath()+"/Buddyplans/pictures"+"/"+messages.get(position).getPhotoContentName();

                      File f=new File(path);

                     if(f.exists()) {
                         Intent intent = new Intent();
                         intent.setAction(Intent.ACTION_VIEW);
                         intent.setDataAndType(Uri.fromFile(f), "image/*");
                         mContext.startActivity(intent);
                     }

                 }
             });

         }
          else if(messages.get(position).getPhotoContentUrl()==null && messages.get(position).getText() != null)
         {
             holder.contentPhoto.setVisibility(View.GONE);
             holder.messageContents.setVisibility(View.VISIBLE);
             holder.messageContents.setText(messages.get(position).getText());
         }





    }


    @Override
    public int getItemViewType(int position) {

       if (messages.get(position).getUserName().equalsIgnoreCase(myName))
          return MINE;
        else
           return OTHERS;

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public void changeMessage(String key, Long newTimestamp)
    {

        Message oldmessage =  (Message) messageMap.get(key);

        int index = messages.indexOf(oldmessage);

        messages.get(index).setTimeStamp(newTimestamp);

    }




    class Message_ViewHolder extends RecyclerView.ViewHolder
    {

        EmojiconTextView messageContents;
        TextView author;
        CircularImageView userPhoto;
        ImageView contentPhoto;

        public Message_ViewHolder(View itemView) {
            super(itemView);


        messageContents = (EmojiconTextView) itemView.findViewById(R.id.message_content);
         author = (TextView) itemView.findViewById(R.id.author);
         userPhoto = (CircularImageView) itemView.findViewById(R.id.userPhoto);
         contentPhoto = (ImageView) itemView.findViewById(R.id.photoImageView);

        }


    }




}
