package io.videtur.ignis;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

    private ImageView mNavProfileImageView;
    private TextView mNavUserNameTextView;
    private TextView mNavUserEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
    public void onUserDataChange(User user) {
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
            // TODO start ContactsActivity
        } else if (id == R.id.nav_invite_friends) {
            // TODO start Firebase invites intent
        } else if (id == R.id.nav_log_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
