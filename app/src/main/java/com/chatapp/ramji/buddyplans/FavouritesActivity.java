package com.chatapp.ramji.buddyplans;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chatapp.ramji.buddyplans.ViewModels.FavouriteChatsViewModel;
import com.chatapp.ramji.buddyplans.db.SavedChatsEntity;

import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    RecyclerView favouriteList;
    FavouriteChatsViewModel favouriteChatsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Favourite Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favouriteChatsViewModel = ViewModelProviders.of(this).get(FavouriteChatsViewModel.class);

        favouriteChatsViewModel.chats.observe(this, new Observer<List<SavedChatsEntity>>() {
            @Override
            public void onChanged(@Nullable List<SavedChatsEntity> savedChatsEntities) {

                favouriteList.setAdapter(new FavouriteList_Adapter(FavouritesActivity.this,savedChatsEntities));

            }
        });

        favouriteList = (RecyclerView) findViewById(R.id.favourite_list);
        favouriteList.setLayoutManager(new GridLayoutManager(this,this.getResources().getInteger(R.integer.fav_column_count)));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        favouriteList.addItemDecoration(itemDecoration);

//        String[] strings = {"first group","second group","third group","fourth group","fifth group"};
//
//        FavouriteList_Adapter adapter = new FavouriteList_Adapter(this,strings);
//        favouriteList.setAdapter(adapter);

    }

}