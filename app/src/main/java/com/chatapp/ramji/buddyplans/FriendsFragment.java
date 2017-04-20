package com.chatapp.ramji.buddyplans;


import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    FriendListAdapter friendListAdapter;

    public FriendsFragment() {
        // Required empty public constructor



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity mainActivity = (MainActivity) context;
        mFirebaseUser = mainActivity.user;
        mFirebaseDatabase = mainActivity.mFirebaseDatabase;
        Uid =  mFirebaseUser.getUid();

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        setHasOptionsMenu(false);

        friendReference = mFirebaseDatabase.getReference().child("Friends").child(Uid);

         mFriendsListener = new FriendsListener();
        // Inflate the layout for this fragment

        friendListAdapter = new FriendListAdapter(getContext(),new ArrayList<Friend>());




        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        FriendLists = (ListView) rootView.findViewById(R.id.list);


        FriendLists.setAdapter(friendListAdapter);

        FriendLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),ChatActivity.class);
                Friend friend = (Friend) parent.getItemAtPosition(position);
                intent.putExtra("Friend",friend);
                startActivity(intent);

            }
        });

        return rootView;


    }


    @Override
    public void onResume() {
        super.onResume();
        friendListQuery = friendReference.orderByKey();
        if(mFriendsListener!=null)
        friendListQuery.addChildEventListener(mFriendsListener);

    }

    @Override
    public void onPause() {
        super.onPause();
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
