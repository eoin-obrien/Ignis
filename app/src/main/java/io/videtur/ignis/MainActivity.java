package io.videtur.ignis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

public class MainActivity extends IgnisAuthActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private BroadcastReceiver mConnectionReceiver;

    private Toolbar mToolbar;
    private ImageView mNavProfileImageView;
    private TextView mNavUserNameTextView;
    private TextView mNavUserEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO start NewMessageActivity
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        mNavProfileImageView = (ImageView) headerView.findViewById(R.id.nav_profile_image);
        mNavUserNameTextView = (TextView) headerView.findViewById(R.id.nav_user_name);
        mNavUserEmailTextView = (TextView) headerView.findViewById(R.id.nav_user_email);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerConnectionReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterConnectionReceiver();
    }

    @Override
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);
        Log.d(TAG, "user.getName:" + user.getName());
        Log.d(TAG, "user.getEmail:" + user.getEmail());
        Log.d(TAG, "user.getPhotoUrl:" + user.getPhotoUrl());
        Glide.with(this).load(user.getPhotoUrl()).fitCenter().into(mNavProfileImageView);
        mNavUserNameTextView.setText(user.getName());
        mNavUserEmailTextView.setText(user.getEmail());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new_message) {
            // TODO start NewMessageActivity
        } else if (id == R.id.nav_new_group) {
            // TODO start NewGroupActivity
        } else if (id == R.id.nav_contacts) {
            startActivity(new Intent(this, ContactsActivity.class));
        } else if (id == R.id.nav_invite_friends) {
            // TODO start Firebase invites intent
        } else if (id == R.id.nav_log_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void registerConnectionReceiver() {
        unregisterConnectionReceiver();
        mConnectionReceiver = new ConnectionStateReceiver();
        registerReceiver(mConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterConnectionReceiver() {
        if (mConnectionReceiver != null) {
            unregisterReceiver(mConnectionReceiver);
            mConnectionReceiver = null;
        }
    }

    private class ConnectionStateReceiver extends BroadcastReceiver {

        private static final String TAG = "ConnectionStateReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            if (connected) {
                Log.d(TAG, "Connected");
                mToolbar.setTitle(R.string.app_name);
            } else {
                Log.d(TAG, "Disconnected");
                mToolbar.setTitle(R.string.toolbar_title_disconnected);
            }
        }

    }
}
