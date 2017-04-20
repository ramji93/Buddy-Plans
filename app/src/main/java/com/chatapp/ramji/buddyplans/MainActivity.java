package com.chatapp.ramji.buddyplans;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class MainActivity extends AppCompatActivity {

   // @BindView(R.id.welcome_message)
   // TextView userText;

    @BindView(R.id.mainTab) TabLayout mainTabLayout;

    @BindView(R.id.mainToolbar)
    Toolbar mainToolbar;

    @BindView(R.id.viewPager)
    ViewPager mViewpager;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabaseReference;
    FirebaseDatabase mFirebaseDatabase;
    private ValueEventListener UserCheckListener;

    MenuItem searchMenuItem;
    MenuItem addGroupMenuItem;

    String TAG = "Login Activity";
    Context mContext = (Context) this;
    private int RC_SIGN_IN = 1;
    private String mUsername = null;
    private String mUid = null;

    FirebaseUser user;

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GroupsFragment(), "BUDDIES GROUP");
        adapter.addFragment(new FriendsFragment(), "FRIENDS");

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        setSupportActionBar(mainToolbar);

        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();



                if (user != null) {
                    Log.d("main activity","user id is "+user.getUid());
                    Log.d("main activity","user photo url is "+user.getPhotoUrl());
                    mUsername = user.getDisplayName();
                    mUid = user.getUid();

                    SigninInitialize();


                  //  userText.setText("welcome " + mUsername);



                } else {

                    SignoutFuction();

                    List<AuthUI.IdpConfig> authProviders = new ArrayList<AuthUI.IdpConfig>();

                    authProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false)
                            .setProviders(authProviders).build(), RC_SIGN_IN);
                }
            }
        };




    }


    public void SigninInitialize()
    {


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if(!sharedPreferences.contains("Uid"))
        {

            User currentuser = new User( (user.getPhotoUrl() == null) ? null : user.getPhotoUrl().toString() ,user.getDisplayName(),user.getEmail(),user.getUid());

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String currentUserString = gson.toJson(currentuser);
            editor.putString("User",currentUserString);
            editor.commit();

        }

        setupViewPager(mViewpager);
        mainTabLayout.setupWithViewPager(mViewpager);

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0)
                {
                    searchMenuItem.setVisible(false);
                    addGroupMenuItem.setVisible(true);

                }

                else
                {
                    searchMenuItem.setVisible(true);
                    addGroupMenuItem.setVisible(false);

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mDatabaseReference = mFirebaseDatabase.getReference().child("Users").child(mUid);

        UserCheckListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                if(currentUser == null)
                {

                    mDatabaseReference.setValue(new User((user.getPhotoUrl() == null) ? null : user.getPhotoUrl().toString(),user.getDisplayName(),user.getEmail(),mUid));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabaseReference.addListenerForSingleValueEvent(UserCheckListener);





    }


    public void SignoutFuction()
    {

        if(UserCheckListener != null)
        mDatabaseReference.removeEventListener(UserCheckListener);




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth != null)
            mAuth.removeAuthStateListener(mAuthListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchMenuItem = menu.findItem(R.id.search);
        addGroupMenuItem = menu.findItem(R.id.add_group);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.sign_out_menu:

                AuthUI.getInstance().signOut(this);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                editor.remove("User");

                editor.commit();

                return true;


            case R.id.add_group:

                Intent intent = new Intent(this,GroupCreateActivity.class);
                this.startActivity(intent);
                return true;


            default:

                return super.onOptionsItemSelected(item);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }






}






