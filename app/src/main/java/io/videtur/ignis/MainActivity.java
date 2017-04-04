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
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseIndexListAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import io.videtur.ignis.model.Chat;
import io.videtur.ignis.model.Message;
import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.CHATS_REF;
import static io.videtur.ignis.util.Constants.MESSAGES_REF;
import static io.videtur.ignis.util.Constants.REQUEST_INVITE;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.Util.formatChatTimestamp;

public class MainActivity extends IgnisAuthActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private BroadcastReceiver mConnectionReceiver;

    private Toolbar mToolbar;
    private ImageView mNavProfileImageView;
    private TextView mNavUserNameTextView;
    private TextView mNavUserEmailTextView;
    private ListView mChatList;

    private FirebaseIndexListAdapter<Chat> mChatsAdapter;
    private DatabaseReference mChatsRef;
    private DatabaseReference mUserChatsRef;
    private DatabaseReference mMessagesRef;
    private DatabaseReference mUsersRef;
    private String mUserKey;

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
                // start NewMessageActivity
                Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
                startActivity(intent);
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

        mChatList = (ListView) findViewById(R.id.chat_list);
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
    public void onUserDataChange(String key, final User user) {
        super.onUserDataChange(key, user);

        mUserKey = key;

        Log.d(TAG, "user.getName:" + user.getName());
        Log.d(TAG, "user.getEmail:" + user.getEmail());
        Log.d(TAG, "user.getPhotoUrl:" + user.getPhotoUrl());
        Glide.with(this).load(user.getPhotoUrl()).fitCenter().into(mNavProfileImageView);
        mNavUserNameTextView.setText(user.getName());
        mNavUserEmailTextView.setText(user.getEmail());

        mUserChatsRef = getDatabase().getReference("/users/" + key + "/chats");
        mMessagesRef = getDatabase().getReference(MESSAGES_REF);
        mChatsRef = getDatabase().getReference(CHATS_REF);
        mUsersRef = getDatabase().getReference(USERS_REF);
        mChatsAdapter = new FirebaseIndexListAdapter<Chat>(this, Chat.class, R.layout.item_chat,
                mUserChatsRef.orderByValue(), mChatsRef) {
            @Override
            protected void populateView(View v, Chat chat, final int position) {
                final ImageView chatImage = (ImageView) v.findViewById(R.id.chat_image);
                final ImageView chatReadReceipt = (ImageView) v.findViewById(R.id.chat_read_receipt);
                final TextView chatName = (TextView) v.findViewById(R.id.chat_name);
                final TextView chatLastMessage = (TextView) v.findViewById(R.id.chat_last_message);
                final TextView chatTimestamp = (TextView) v.findViewById(R.id.chat_timestamp);
                final TextView chatUnreadCount = (TextView) v.findViewById(R.id.chat_unread_count);
                final String contactKey;
                String chatKey = mChatsAdapter.getRef(position).getKey();

                if (user.getUnread() != null && user.getUnread().containsKey(chatKey)) {
                    int unreadCount = ((Map) user.getUnread().get(chatKey)).size();
                    chatUnreadCount.setText(String.valueOf(unreadCount));
                    chatUnreadCount.setVisibility(View.VISIBLE);
                } else {
                    chatUnreadCount.setVisibility(View.GONE);
                }

                Log.d(TAG, "lastMessage:" + chat.getLastMessage());
                if (chat.getLastMessage() != null) {
                    DatabaseReference lastMessageRef = mMessagesRef
                            .child(chatKey)
                            .child(chat.getLastMessage());
                    lastMessageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Message message = dataSnapshot.getValue(Message.class);
                            chatLastMessage.setText(message.getText());
                            chatTimestamp.setText(formatChatTimestamp(message.getTimestampLong()));
                            if (mUserKey.equals(message.getSenderKey())) {
                                if (message.getReadReceipts() != null) {
                                    chatReadReceipt.setImageResource(R.drawable.ic_message_seen);
                                } else if (message.getDeliveryReceipts() != null) {
                                    chatReadReceipt.setImageResource(R.drawable.ic_message_delivered);
                                } else {
                                    chatReadReceipt.setImageResource(R.drawable.ic_message_pending);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (databaseError != null) {
                                Log.e(TAG, databaseError.getMessage());
                            }
                        }
                    });
                }

                // Display other user's name and photo beside the chat
                if (!chat.getMembers().keySet().toArray()[0].equals(mUserKey)) {
                    contactKey = (String) chat.getMembers().keySet().toArray()[0];
                } else {
                    contactKey = (String) chat.getMembers().keySet().toArray()[1];
                }
                mUsersRef.child(contactKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User contact = dataSnapshot.getValue(User.class);
                        chatName.setText(contact.getName());
                        Glide.with(MainActivity.this)
                                .load(contact.getPhotoUrl())
                                .into(chatImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (databaseError != null) {
                            Log.e(TAG, databaseError.getMessage());
                        }
                    }
                });

                // clicking on a list item should start the corresponding chat
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String chatKey = mChatsAdapter.getRef(position).getKey();
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra(ChatActivity.ARG_CHAT_KEY, chatKey);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public Chat getItem(int position) {
                return super.getItem(getCount() - (position + 1));
            }
        };
        mChatList.setAdapter(mChatsAdapter);
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
            startActivity(new Intent(this, NewMessageActivity.class));
        } else if (id == R.id.nav_contacts) {
            startActivity(new Intent(this, ContactsActivity.class));
        } else if (id == R.id.nav_invite_friends) {
            startInvitesActivity();
        } else if (id == R.id.nav_log_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startInvitesActivity() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                showToast(R.string.invitation_failed);
            }
        }
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
