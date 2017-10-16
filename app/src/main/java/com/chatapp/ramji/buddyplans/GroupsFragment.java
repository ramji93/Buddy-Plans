package com.chatapp.ramji.buddyplans;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.chatapp.ramji.buddyplans.ViewModels.FavouriteChatsViewModel;
import com.chatapp.ramji.buddyplans.ViewModels.SavedChatViewModel;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    FirebaseUser mFirebaseUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference groupReference;
    private String Uid;
    Query groupListQuery;
    GroupListener mGroupListener;
    ListView GroupLists;

    GroupListAdapter groupListAdapter;

    View rootView;
    Intent shareIntent = null;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity mainActivity = (MainActivity)  getActivity();

        if(getActivity().getIntent() != null)
            shareIntent = getActivity().getIntent();

        Gson gson = new Gson();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        User currentUser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);

        Uid = currentUser.getUid();

        //GroupsFragment container = (GroupsFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.groupfragment);

        ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.groupfragment);
        rootView =  LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_groups, container, false);


        GroupLists = (ListView) rootView.findViewById(R.id.grouplist);

        groupListAdapter = new GroupListAdapter(getContext(),new ArrayList<Groupheader>());

        GroupLists.setAdapter(groupListAdapter);

        GroupLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),GroupChatActivity.class);
                Groupheader group = (Groupheader) parent.getItemAtPosition(position);

                ImageView imageView = (ImageView) view.findViewById(R.id.group_item_photo);



                if(imageView.getDrawable()!=null) {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    intent.putExtra("image", bitmap);
                }
                intent.putExtra("transition",ViewCompat.getTransitionName(imageView));
                intent.putExtra("group",group);

                if(shareIntent!= null)
                {
                    intent.putExtra("shareIntent",shareIntent);
                    shareIntent = null;
                }

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation
                        (
                                getActivity(),
                                imageView,
                                ViewCompat.getTransitionName(imageView));
                startActivity(intent,options.toBundle());

            }
        });


        groupReference = mFirebaseDatabase.getReference().child("GroupChat");

        mGroupListener = new GroupListener();

        groupListQuery = groupReference.orderByKey();

        if(mGroupListener!=null)
            groupListQuery.addChildEventListener(mGroupListener);



        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected)
        {

            SavedChatViewModel viewModel = ViewModelProviders.of(this).get(SavedChatViewModel.class);

            viewModel.getSavedGroupChats();

            viewModel.savedChats_group.observe(this, new Observer<SavedChatsEntity>() {
                @Override
                public void onChanged(@Nullable SavedChatsEntity savedChatsEntity) {
                    Groupheader group = new Groupheader(savedChatsEntity.chatName,savedChatsEntity.chatid,savedChatsEntity.chatProfileImageurl);
                    group.setGroupKey(savedChatsEntity.groupKey);
                    groupListAdapter.add(group);
                    groupListAdapter.notifyDataSetChanged();

                }
            });

        }



    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity mainActivity = (MainActivity) context;
       // mFirebaseUser = mainActivity.user;
        mFirebaseDatabase = FirebaseDatabase.getInstance();


//        if(mFirebaseUser!=null)
//        Uid =  mFirebaseUser.getUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        setHasOptionsMenu(false);
        // Inflate the layout for this fragment

//        groupReference = mFirebaseDatabase.getReference().child("GroupChat");

//        mGroupListener = new GroupListener();
        // Inflate the layout for this fragment





        return rootView;



    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        groupListQuery = groupReference.orderByKey();
//
//        if(mGroupListener!=null)
//            groupListQuery.addChildEventListener(mGroupListener);
//
//    }
//
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if(mGroupListener!=null) {
//            groupListQuery.removeEventListener(mGroupListener);
//            mGroupListener=null;
//        }
//
//    }

    @Override
    public void onStart() {
        super.onStart();

//        groupListQuery = groupReference.orderByKey();
//
//        if(mGroupListener!=null)
//            groupListQuery.addChildEventListener(mGroupListener);


    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

    }

    @Override
    public void onStop() {
        super.onStop();
//        if(mGroupListener!=null) {
//            groupListQuery.removeEventListener(mGroupListener);
//            mGroupListener=null;
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGroupListener!=null) {
            groupListQuery.removeEventListener(mGroupListener);
            mGroupListener=null;
        }
    }

    private class GroupListener implements ChildEventListener
    {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {




            final Groupheader group = (Groupheader) dataSnapshot.getValue(Groupheader.class);

            DatabaseReference userCheckRef = mFirebaseDatabase.getReference("GroupMemebers").child(dataSnapshot.getKey()).child(Uid);

            group.setGroupKey(dataSnapshot.getKey());

            userCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {

                        groupListAdapter.add(group);

                        groupListAdapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            final Groupheader group = (Groupheader) dataSnapshot.getValue(Groupheader.class);

            DatabaseReference userCheckRef = mFirebaseDatabase.getReference("GroupMemebers").child(dataSnapshot.getKey()).child(Uid);

            group.setGroupKey(dataSnapshot.getKey());

            userCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {

//                        groupListAdapter.add(group);
//
//                        groupListAdapter.notifyDataSetChanged();

//                        Friend friend = (Friend) dataSnapshot.getValue(Friend.class);
//
//                        Friend friend1 = friendListAdapter.friendHashMap.get(friend.getUid());
//
//                        friendListAdapter.friends.remove(friend1);
//
//                        friendListAdapter.friendHashMap.remove(friend.getUid());
//
//                        friendListAdapter.add(friend);
//
//                        friendListAdapter.notifyDataSetChanged();

                         Groupheader group1 =  groupListAdapter.grouphashmap.get(group.getGroupKey());

                         groupListAdapter.groups.remove(group1);

                         groupListAdapter.grouphashmap.remove(group.getGroupKey());

                         groupListAdapter.add(group);

                         groupListAdapter.notifyDataSetChanged();



                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



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
}
