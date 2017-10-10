package com.chatapp.ramji.buddyplans;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class FavouritesActivity extends AppCompatActivity {

    RecyclerView favouriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Favourite Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favouriteList = (RecyclerView) findViewById(R.id.favourite_list);
        favouriteList.setLayoutManager(new GridLayoutManager(this,this.getResources().getInteger(R.integer.fav_column_count)));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        favouriteList.addItemDecoration(itemDecoration);

        String[] strings = {"first group","second group","third group","fourth group","fifth group"};

        FavouriteList_Adapter adapter = new FavouriteList_Adapter(this,strings);
        favouriteList.setAdapter(adapter);

    }

}
