package com.chatapp.ramji.buddyplans;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        // Inflate the layout for this fragment

        groupReference = mFirebaseDatabase.getReference().child("GroupChat");

        mGroupListener = new GroupListener();
        // Inflate the layout for this fragment

        groupListAdapter = new GroupListAdapter(getContext(),new ArrayList<Groupheader>());


        View rootView =  inflater.inflate(R.layout.fragment_groups, container, false);

        GroupLists = (ListView) rootView.findViewById(R.id.grouplist);


        GroupLists.setAdapter(groupListAdapter);

        GroupLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),GroupChatActivity.class);
                Groupheader group = (Groupheader) parent.getItemAtPosition(position);
                intent.putExtra("group",group);
                startActivity(intent);

            }
        });

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        groupListQuery = groupReference.orderByKey();

        if(mGroupListener!=null)
            groupListQuery.addChildEventListener(mGroupListener);

    }


    @Override
    public void onPause() {
        super.onPause();
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

            userCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {

                        group.setGroupKey(dataSnapshot.getKey());

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
