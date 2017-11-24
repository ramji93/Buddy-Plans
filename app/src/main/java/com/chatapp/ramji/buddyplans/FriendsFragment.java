package com.chatapp.ramji.buddyplans;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.chatapp.ramji.buddyplans.ViewModels.SavedChatViewModel;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    FirebaseUser mFirebaseUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference friendReference;
    private String Uid;
    Query friendListQuery;
    FriendsListener mFriendsListener;
    ListView FriendLists;
    View rootView;
    Intent shareIntent = null;
    FriendComparator friendComparator = new FriendComparator();

    public static FriendListAdapter friendListAdapter;
    SavedChatViewModel viewModel;
    Boolean tranisition;

    public FriendsFragment() {
        // Required empty public constructor



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity mainActivity = (MainActivity) context;
//        mFirebaseUser = mainActivity.user;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        Uid =  mFirebaseUser.getUid();



    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity().getIntent() != null ) {

            if(getActivity().getIntent().getAction()==Intent.ACTION_SEND) {

                shareIntent = getActivity().getIntent();
                Log.d(FriendsFragment.class.getName(), "share intent");
            }
        }

        MainActivity mainActivity = (MainActivity)  getActivity();

        viewModel = ViewModelProviders.of(this).get(SavedChatViewModel.class);

        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        User currentUser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);

        Uid = currentUser.getUid();

        friendReference = mFirebaseDatabase.getReference().child("Friends").child(Uid);

        mFriendsListener = new FriendsListener();


        friendListAdapter = new FriendListAdapter(getContext(),new ArrayList<Friend>());



        ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.friendsfragment);
        rootView =  LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_friends, container, false);




        FriendLists = (ListView) rootView.findViewById(R.id.list);


        FriendLists.setAdapter(friendListAdapter);

        friendListAdapter.sort(new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
                if(o1.getLastMessageTimestap() > o2.getLastMessageTimestap())
                    return 1;
                else if(o1.getLastMessageTimestap() < o2.getLastMessageTimestap())
                    return -1;
                else
                    return 0;

            }
        });

        FriendLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),ChatActivity.class);
                ImageView imageView = (ImageView) view.findViewById(R.id.friend_item_photo);
                Friend friend = (Friend) parent.getItemAtPosition(position);


                if(friend.getPhotourl()!=null && imageView.getDrawable()!=null) {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    intent.putExtra("image", bitmap);
                    intent.putExtra("transition",ViewCompat.getTransitionName(imageView));
                    tranisition = true;
                }

                else
                    tranisition = false;
                intent.putExtra("Friend",friend);

                if(shareIntent!= null)
                {
                    intent.putExtra("shareIntent",shareIntent);
//                    shareIntent = null;
                    tranisition = false;
                }

                if(tranisition) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation
                            (
                                    getActivity(),
                                    imageView,
                                    ViewCompat.getTransitionName(imageView));
                    startActivity(intent, options.toBundle());
                }
                  else
                {
                    if(shareIntent!=null)
                    getActivity().finish();
//                    startActivity(intent);
                    TaskStackBuilder.create(getContext())
                            .addNextIntentWithParentStack(intent)
                            .startActivities();
                }

            }
        });


        friendListQuery = friendReference.orderByChild("lastMessageTimestap");
        if(mFriendsListener!=null)
            friendListQuery.addChildEventListener(mFriendsListener);

        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected)
        {


            if(mFriendsListener!=null) {
                friendListQuery.removeEventListener(mFriendsListener);
                mFriendsListener=null;
            }

            viewModel.getSavedFriendChats();

            viewModel.savedChats_friend.observe(this, new Observer<List<SavedChatsEntity>>() {
                @Override
                public void onChanged(@Nullable List<SavedChatsEntity> savedChatsEntities) {

                    if(savedChatsEntities.size()>0) {

                        friendListAdapter.clear();

                        for (SavedChatsEntity savedChatsEntity : savedChatsEntities) {
                            Friend friend = new Friend(savedChatsEntity.chatName,savedChatsEntity.chatProfileImageurl,true,savedChatsEntity.friendUid);
                            friend.setChatid(savedChatsEntity.chatid);
                            friendListAdapter.add(friend);
                        }

                        friendListAdapter.notifyDataSetChanged();
                    }
                }
            });

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        setHasOptionsMenu(false);


        // Inflate the layout for this fragment


        return rootView;


    }


    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mFriendsListener!=null) {
            friendListQuery.removeEventListener(mFriendsListener);
            mFriendsListener=null;
        }

    }

    private class FriendsListener implements ChildEventListener
    {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final Friend friend = (Friend) dataSnapshot.getValue(Friend.class);

            if(friend.isActive()) {
                FirebaseDatabase.getInstance().getReference("Users").child(friend.getUid()).child("profileDP").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String photourl = dataSnapshot.getValue(String.class);
                        friend.setPhotourl(photourl);
                        friendListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                friendListAdapter.add(friend);

                friendListAdapter.sort(friendComparator);

                friendListAdapter.notifyDataSetChanged();

                viewModel.setChatActive(friend.getChatid());

            }

            else {

                //// TODO: make active false in db and make active as offline criteria
                viewModel.setChatInactive(friend.getChatid());

            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            final Friend friend = (Friend) dataSnapshot.getValue(Friend.class);



           Friend friend1 = friendListAdapter.friendHashMap.get(friend.getUid());

            if (friend1 != null) {
                friendListAdapter.friends.remove(friend1);
                friend.setPhotourl(friend1.getPhotourl());

            }

            friendListAdapter.friendHashMap.remove(friend.getUid());

            if(friend.isActive()) {
                friendListAdapter.add(friend);
                friendListAdapter.sort(friendComparator);
                viewModel.setChatActive(friend.getChatid());
            }

            else
            {
                //// TODO: make active false in db and make active as offline criteria
                viewModel.setChatInactive(friend.getChatid());


            }

            friendListAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }


    class FriendComparator implements Comparator<Friend>{

        @Override
        public int compare(Friend o1, Friend o2) {

            if(o2.getLastMessageTimestap() == null)
                return -1;
            else if( o1.getLastMessageTimestap() == null)
                return 1;
            else if(o1.getLastMessageTimestap() > o2.getLastMessageTimestap())
                return -1;
            else if(o1.getLastMessageTimestap() < o2.getLastMessageTimestap())
                return 1;
            else
                return 0;
        }
    }

}
