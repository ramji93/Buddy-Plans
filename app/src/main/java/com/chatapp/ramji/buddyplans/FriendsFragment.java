package com.chatapp.ramji.buddyplans;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
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

    public static FriendListAdapter friendListAdapter;

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

        if(getActivity().getIntent() != null)
        shareIntent = getActivity().getIntent();

        MainActivity mainActivity = (MainActivity)  getActivity();

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

        FriendLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),ChatActivity.class);
                ImageView imageView = (ImageView) view.findViewById(R.id.friend_item_photo);
                Friend friend = (Friend) parent.getItemAtPosition(position);


                if(friend.getPhotourl()!=null) {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    intent.putExtra("image", bitmap);

                }
                intent.putExtra("Friend",friend);

                if(shareIntent!= null)
                {
                    intent.putExtra("shareIntent",shareIntent);
                    shareIntent = null;
                }

                startActivity(intent);

            }
        });


        friendListQuery = friendReference.orderByKey();
        if(mFriendsListener!=null)
            friendListQuery.addChildEventListener(mFriendsListener);

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

            Friend friend = (Friend) dataSnapshot.getValue(Friend.class);

            friendListAdapter.add(friend);

            friendListAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            Friend friend = (Friend) dataSnapshot.getValue(Friend.class);

           Friend friend1 = friendListAdapter.friendHashMap.get(friend.getUid());

            friendListAdapter.friends.remove(friend1);

            friendListAdapter.friendHashMap.remove(friend.getUid());

            friendListAdapter.add(friend);

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


}
