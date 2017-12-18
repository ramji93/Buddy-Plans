package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by user on 13-05-2017.
 */

public class GroupCreateSelectedListAdapter extends RecyclerView.Adapter<GroupCreateSelectedListAdapter.GC_SelectedFriendViewHolder> {

    ArrayList<Friend> selectedlist;
    Context mcontext;

    public GroupCreateSelectedListAdapter(Context context) {

        mcontext = context;
        selectedlist = new ArrayList<Friend>();

    }

    @Override
    public GC_SelectedFriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mcontext).inflate(R.layout.group_create_selected_item, parent, false);

        return new GC_SelectedFriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GC_SelectedFriendViewHolder holder, int position) {

        final int pos = position;
        Glide.with(mcontext).load(selectedlist.get(position).getPhotourl()).into(holder.imageView);
        holder.selectedFriendName.setText(selectedlist.get(position).getName());


    }

    @Override
    public int getItemCount() {
        return selectedlist.size();
    }

    public class GC_SelectedFriendViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.selected_item_circleimage)
        CircularImageView imageView;
        @BindView(R.id.selected_item)
        ViewGroup deleteView;
        @BindView(R.id.selected_friend_name)
        TextView selectedFriendName;

        public GC_SelectedFriendViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);
            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DeleteSelectedItemListener deleteSelectedItemListener = (DeleteSelectedItemListener) mcontext;
                    deleteSelectedItemListener.ondeleteselecteditem(selectedlist.get(getAdapterPosition()));
                    selectedlist.remove(getAdapterPosition());
                    notifyDataSetChanged();


                }
            });


        }
    }

    public interface DeleteSelectedItemListener {

        public void ondeleteselecteditem(Friend friend);


    }


}
