package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chatapp.ramji.buddyplans.db.MessageEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by user on 12-03-2017.
 */

public class Messages_Adapter extends RecyclerView.Adapter<Messages_Adapter.Message_ViewHolder>

{
    private Context mContext;
    ArrayList<MessageEntity> messages;
    String myuid;
    HashMap<String, Message> messageMap = new HashMap<String, Message>();
    private DatabaseReference mDatabaseReference;
    FirebaseDatabase mFirebaseDatabase;
    private ValueEventListener FriendCheckListener;
    private String Uid;
    //    ArrayList<String> imageLoadedUsers;
    private int ClickPosition;

    private final int OTHERS = 1;
    private final int MINE = 2;
    private final int SYSTEM = 3;

    public Messages_Adapter(Context context, String uid) {

        mContext = context;
        messages = new ArrayList<MessageEntity>();
        myuid = uid;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        User currentuser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);
        Uid = currentuser.getUid();
//        imageLoadedUsers = new ArrayList<String>();


        FriendCheckListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Friend authorfriend = dataSnapshot.getValue(Friend.class);

                if (authorfriend != null) {
                    if (authorfriend.isActive()) {

                        Intent intent = new Intent(mContext, ChatActivity.class);

                        if (authorfriend.getChatid() != null) {
                            String path = Environment.getExternalStorageDirectory().getPath() + mContext.getString(R.string.appsegment) + "/" + authorfriend.getChatid();
                            File f = new File(path);
                            if (f.exists())
                                authorfriend.setPhotourl(path);
                            else
                                authorfriend.setPhotourl(messages.get(ClickPosition).getPhotoUrl());
                        } else
                            authorfriend.setPhotourl(messages.get(ClickPosition).getPhotoUrl());

                        intent.putExtra("Friend", authorfriend);
                        mContext.startActivity(intent);

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }


    @Override
    public Message_ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == OTHERS)
            view = LayoutInflater.from(mContext).inflate(R.layout.message_list_item, parent, false);

        else if (viewType == MINE)
            view = LayoutInflater.from(mContext).inflate(R.layout.message_list_item_right, parent, false);

        else {
            view = LayoutInflater.from(mContext).inflate(R.layout.sysmessage_list_item, parent, false);

        }

        return new Message_ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final Message_ViewHolder holder, final int position) {


        if (getItemViewType(position) == SYSTEM) {
            holder.messageContents.setText(messages.get(position).getText());
            holder.timeView.setText(Util.getDate(messages.get(position).getTimeStamp()));
            return;
        }


        if (mContext instanceof ChatActivity) {
            holder.author.setVisibility(View.GONE);
            holder.userPhoto.setVisibility(View.GONE);
        } else {

            holder.author.setText(messages.get(position).getUserName());

            if (messages.get(position).getPhotoUrl() != null) {

//                if(!imageLoadedUsers.contains(messages.get(position).getUid())) {
//                    Glide.with(mContext).load(messages.get(position).getPhotoUrl()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.userPhoto);
//                    imageLoadedUsers.add(messages.get(position).getUid());
//                }
//                else
//                    Glide.with(mContext).load(messages.get(position).getPhotoUrl()).into(holder.userPhoto);


                Glide.with(mContext).load("file://" + messages.get(position).getPhotoUrl()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.userPhoto);

            }
        }


        if (messages.get(position).getPhotoContentUrl() != null && messages.get(position).getText() == null && messages.get(position).getLocation() == null) {

            Uri uri = Uri.parse(messages.get(position).getPhotoContentUrl());

            holder.contentPhoto.setVisibility(View.VISIBLE);
            holder.mapView.setVisibility(View.GONE);
            holder.messageContents.setVisibility(View.GONE);
            Glide.with(mContext).load(messages.get(position).getPhotoContentUrl()).into(holder.contentPhoto);
            holder.contentPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String path = Environment.getExternalStorageDirectory().getPath() + "/Buddyplans/pictures" + "/" + messages.get(position).getPhotoContentName();

                    File f = new File(path);

                    if (f.exists()) {

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);

                        if (Build.VERSION.SDK_INT > M) {
                            intent.setDataAndType(CustomFileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".my.package.name.provider", f), "image/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            mContext.startActivity(intent);
                        } else {
                            intent.setDataAndType(Uri.fromFile(f), "image/*");
                            mContext.startActivity(intent);
                        }
                    }

                }
            });

        } else if (messages.get(position).getPhotoContentUrl() == null && messages.get(position).getText() != null && messages.get(position).getLocation() == null) {
            holder.contentPhoto.setVisibility(View.GONE);
            holder.mapView.setVisibility(View.GONE);
            holder.messageContents.setVisibility(View.VISIBLE);
            holder.messageContents.setText(messages.get(position).getText());
        } else if (messages.get(position).getPhotoContentUrl() == null && messages.get(position).getText() == null && messages.get(position).getLocation() != null) {
            holder.contentPhoto.setVisibility(View.GONE);
            holder.mapView.setVisibility(View.VISIBLE);
            holder.messageContents.setVisibility(View.GONE);
            holder.messageContents.setText(messages.get(position).getText());
            Location location = messages.get(position).getLocation();
            final double lat = location.getLatitude();
            final double lon = location.getLongitude();

            new AsyncTask<Location, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Location... params) {

                    Location loc = params[0];
                    double lat = loc.getLatitude();
                    double lon = loc.getLongitude();
                    Bitmap mapbitmap = null;
                    try {
                        mapbitmap = Glide.with(mContext).load("https://maps.googleapis.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=15&size=600x300&maptype=normal").asBitmap().into(100, 100).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    return mapbitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    holder.mapView.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
                }
            }.execute(location);


            holder.mapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon + "(Location provided by " + messages.get(position).getUserName() + ")"));
                    mContext.startActivity(intent);
                }
            });

        }

        holder.timeView.setText(Util.getDate(messages.get(position).getTimeStamp()));

        if (getItemViewType(position) == OTHERS && holder.author.getVisibility() == View.VISIBLE) {

            holder.author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String author_uid = messages.get(position).getUid();

                    mDatabaseReference = mFirebaseDatabase.getReference("Friends").child(Uid).child(author_uid);

                    ClickPosition = position;

                    mDatabaseReference.addListenerForSingleValueEvent(FriendCheckListener);

                }
            });

        }


    }


    @Override
    public int getItemViewType(int position) {

        if (messages.get(position).getUid() != null) {

            if (messages.get(position).getUid().equalsIgnoreCase(myuid))
                return MINE;
            else
                return OTHERS;

        } else

            return SYSTEM;

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public void changeMessage(String key, Long newTimestamp) {

        Message oldmessage = (Message) messageMap.get(key);

        int index = messages.indexOf(oldmessage);

        messages.get(index).setTimeStamp(newTimestamp);

    }


    class Message_ViewHolder extends RecyclerView.ViewHolder {

        EmojiconTextView messageContents;
        TextView author;
        CircularImageView userPhoto;
        ImageView contentPhoto;
        TextView timeView;
        ImageButton mapView;


        public Message_ViewHolder(View itemView) {
            super(itemView);


            messageContents = (EmojiconTextView) itemView.findViewById(R.id.message_content);
            author = (TextView) itemView.findViewById(R.id.author);
            userPhoto = (CircularImageView) itemView.findViewById(R.id.userPhoto);
            contentPhoto = (ImageView) itemView.findViewById(R.id.photoImageView);
            timeView = (TextView) itemView.findViewById(R.id.message_time);
            mapView = (ImageButton) itemView.findViewById(R.id.mapView);


        }


    }


}
