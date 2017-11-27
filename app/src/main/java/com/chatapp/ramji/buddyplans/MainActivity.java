package com.chatapp.ramji.buddyplans;


import android.*;
import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
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
public class MainActivity extends BaseActivity  {

   // @BindView(R.id.welcome_message)
   // TextView userText;

    @BindView(R.id.mainTab) TabLayout mainTabLayout;

    @BindView(R.id.mainToolbar)
    Toolbar mainToolbar;

    @BindView(R.id.viewPager)
    ViewPager mViewpager;

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    TextView txtName;

    User currentuser;

    CircularImageView imgProfile;

    private View navHeader;

    private GroupsFragment groupsFragment;
    private FriendsFragment friendsFragment;

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

    int tab_index = 0;
    final int WRITE_REQUEST = 1;
    final int CALENDAR_REQUEST = 1;

    FirebaseUser user;
    HandlerThread handlerThread;
    Handler mhandler;
    Intent shareIntent;
    Menu menu;

    public static int navItemIndex = 0;

    ViewPagerAdapter adapter;

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter;



        if(viewPager.getAdapter()!=null)
        {
            adapter = (ViewPagerAdapter) viewPager.getAdapter();
            adapter.mFragmentList.clear();
            adapter.mFragmentTitleList.clear();

        }
        else {
            adapter = new ViewPagerAdapter(getSupportFragmentManager());
        }
        friendsFragment = new FriendsFragment();
        groupsFragment = new GroupsFragment();
        adapter.addFragment(friendsFragment, "FRIENDS");
        adapter.addFragment(groupsFragment, "BUDDIES GROUP");

        viewPager.setAdapter(adapter);


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        setSupportActionBar(mainToolbar);

        if(getIntent() != null ) {

            if(getIntent().getAction()==Intent.ACTION_SEND) {

                shareIntent = getIntent();
                Log.d(MainActivity.class.getName(), "share intent");
                navigationView.setVisibility(View.GONE);
                mainToolbar.setNavigationIcon(null);
            }
        }

        navHeader = navigationView.getHeaderView(0);

        txtName = (TextView) navHeader.findViewById(R.id.name);

        imgProfile = (CircularImageView) navHeader.findViewById(R.id.img_profile);

        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        handlerThread = new HandlerThread("handlerThread");

        handlerThread.start();

        mhandler = new Handler(handlerThread.getLooper());

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



                    List<AuthUI.IdpConfig> authProviders = new ArrayList<AuthUI.IdpConfig>();

                    authProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());

                    authProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false).setLogo(R.drawable.auth_logo)
                            .setProviders(authProviders).setTheme(R.style.AppTheme).build(), RC_SIGN_IN);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);

        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_CALENDAR}, CALENDAR_REQUEST);

        }

    }



    private void setUpNavigationView() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_profile:
                        navItemIndex = 1;

                        startActivity(new Intent(MainActivity.this,MyProfileActivity.class));
                        drawer.closeDrawers();

                        break;

                    case R.id.nav_favourites:
                         navItemIndex = 2;
                         startActivity(new Intent(MainActivity.this,FavouritesActivity.class));
                         drawer.closeDrawers();
                         break;

                    case R.id.nav_logout:
                        navItemIndex = 3;

                        drawer.closeDrawers();

                        SignoutFuction();

                        AuthUI.getInstance().signOut(MainActivity.this);

                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                        editor.remove("User");

                        editor.remove("profiledp");

                        editor.commit();




                    default:
                        navItemIndex = 0;

                }

//                if (menuItem.isChecked()) {
//                    menuItem.setChecked(false);
//                } else {
//                    menuItem.setChecked(true);
//                }
                menuItem.setChecked(false);
                return false;

            }

        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, mainToolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);


            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        if(shareIntent!=null)
        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);

        actionBarDrawerToggle.syncState();

    }

    public void refreshProfileImage()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String storedPhotoUrl = sharedPreferences.getString("profiledp",currentuser.getProfileDP());
        Glide.with(this).load(storedPhotoUrl).into(imgProfile);
    }


    public void SigninInitialize()
    {

        String facebook_id = null;

        currentuser = null;


        if(AccessToken.getCurrentAccessToken()!=null) {

            facebook_id = Profile.getCurrentProfile().getId().toString();


        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);



        if(!sharedPreferences.contains("User"))
        {

             currentuser = new User( (user.getPhotoUrl() == null) ? null : user.getPhotoUrl().toString() ,user.getDisplayName(),user.getEmail(),user.getUid());

            currentuser.setFb_id(facebook_id);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String currentUserString = gson.toJson(currentuser);
            editor.putString("User",currentUserString);
            editor.commit();

            Glide.with(this).load(currentuser.getProfileDP()).into(imgProfile);
        }

        else
        {
            Gson gson = new Gson();

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            currentuser = gson.fromJson(sharedPreferences.getString("User", ""), User.class);

            String storedPhotoUrl;
            storedPhotoUrl = sharedPreferences.getString("profiledp",currentuser.getProfileDP());
            Glide.with(this).load(storedPhotoUrl).into(imgProfile);

        }



        txtName.setText(mUsername);

        setUpNavigationView();

        setupViewPager(mViewpager);

        mainTabLayout.setupWithViewPager(mViewpager);

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0)
                {

                    if(shareIntent==null) {
                        searchMenuItem.setVisible(true);
                        addGroupMenuItem.setVisible(false);
                    }
                    tab_index = tab.getPosition();

                }

                else
                {
                    if(shareIntent==null) {
                        searchMenuItem.setVisible(false);
                        addGroupMenuItem.setVisible(true);
                    }
                    tab_index = tab.getPosition();

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

        final User finalCurrentuser = currentuser;
        UserCheckListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User currentUser = dataSnapshot.getValue(User.class);

                if(currentUser == null)
                {

                    mDatabaseReference.setValue(finalCurrentuser);

                }

                else

                {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

                    final SharedPreferences.Editor editor = sharedPreferences.edit();

                    if(currentUser.getProfileDP()!=null)

                    {
                        editor.putString("profiledp", currentUser.getProfileDP());

                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {

                               String url = Util.saveProfileImage(mContext,currentUser.getProfileDP(),mUid);
                                editor.putString("profiledp",url);
                                editor.commit();

                            }
                        });


                        Glide.with(mContext).load(currentUser.getProfileDP()).into(imgProfile);

                        editor.commit();
                    }

                }

                ((MyApplication) getApplication()).userid = mUid;
                mDatabaseReference.child("online").setValue(true);

                String token = FirebaseInstanceId.getInstance().getToken();

                if(token!= null)
                {

                    mDatabaseReference.child("instanceId").setValue(token);
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
        mFirebaseDatabase.getReference().child("Users").child(mUid).child("online").setValue(false);

        ((MyApplication) getApplication()).userid = null;

        mViewpager.setAdapter(null);

        ViewPagerAdapter pagerAdapter =  (ViewPagerAdapter) mViewpager.getAdapter();

        if(UserCheckListener != null)
            mDatabaseReference.removeEventListener(UserCheckListener);
//
//        try {
//            if(groupsFragment!= null)
//                groupsFragment.onDestroy();
//            if(friendsFragment!= null)
//                friendsFragment.onDestroy();
//        }
//       catch (Exception e)
//       {
//           e.printStackTrace();
//       }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
       fragmentTransaction.remove(groupsFragment);
       fragmentTransaction.remove(friendsFragment);
       fragmentTransaction.commit();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                ((MyApplication) getApplication()).stopActivityTransitionTimer();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(currentuser!=null)
        refreshProfileImage();




    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (mAuth != null)
//            mAuth.removeAuthStateListener(mAuthListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(shareIntent!=null)
            return true;
        else {
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.sign_out_menu:

                SignoutFuction();

                AuthUI.getInstance().signOut(this);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                editor.remove("User");

                editor.remove("profiledp");

                editor.commit();

                return true;

            case R.id.invite_menu:

                String message = getString(R.string.invite_message);

                Uri.Builder uribuilder = new Uri.Builder();
                uribuilder.scheme("https")
                        .authority("play.google.com")
                        .appendPath("store")
                        .appendPath("apps")
                        .appendPath("details")
                        .appendQueryParameter("id","com.chatapp.ramji.buddyplans");

                Intent appintent = new Intent(Intent.ACTION_SEND);
                appintent.setType("text/plain");
                appintent.putExtra(Intent.EXTRA_TEXT,message + uribuilder.build().toString());
                startActivity(Intent.createChooser(appintent,"Select an app to send invite"));
                return true;


            case R.id.add_group:

                if(!Util.checkConnection(mContext))
                {


                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.nointernet)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

                    // Create the AlertDialog object and return it
                    builder.create().show();

                    return true;

                }
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






