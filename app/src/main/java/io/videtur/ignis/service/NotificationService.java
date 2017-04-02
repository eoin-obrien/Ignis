package io.videtur.ignis.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import io.videtur.ignis.ChatActivity;
import io.videtur.ignis.MainActivity;
import io.videtur.ignis.R;

import static io.videtur.ignis.util.Constants.MESSAGES_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.Util.getKeyFromEmail;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final int NOTIFICATION_ID = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mChatsRef;
    private DatabaseReference mMessagesRef;
    private DatabaseReference mUnreadMessagesRef;

    private ValueEventListener mUnreadMessagesListener;

    private Context mContext;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;

    public NotificationService(Context applicationContext) {
        super();
        mContext = applicationContext;
    }

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");

        // Set up notifications
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Set up auth listener
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (mUnreadMessagesRef != null) {
                        mUnreadMessagesRef.removeEventListener(mUnreadMessagesListener);
                    }

                    String userKey = getKeyFromEmail(firebaseAuth.getCurrentUser().getEmail());
                    mDatabase = FirebaseDatabase.getInstance();
                    mMessagesRef = mDatabase.getReference(MESSAGES_REF);
                    mUnreadMessagesRef = mDatabase.getReference(USERS_REF).child(userKey).child("unread");

                    mUnreadMessagesListener = mUnreadMessagesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            notifyMessages(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);


        // TODO set up listener on unread messages
        // TODO mark unread messages as delivered
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        mAuth.removeAuthStateListener(mAuthStateListener);
        // TODO restart service if destroyed
        // Intent broadcastIntent = new Intent("BroadcastReceiver");
        // sendBroadcast(broadcastIntent);
    }

    private void notifyMessages(DataSnapshot unreadSnapshot) {
        long chatCount = unreadSnapshot.getChildrenCount();
        long totalMessageCount = 0;

        for (DataSnapshot chatSnapshot : unreadSnapshot.getChildren()) {
            totalMessageCount += chatSnapshot.getChildrenCount();
            // TODO mark messages as delivered
            // TODO vibrate and play sound if not delivered
            for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                Log.d(TAG, chatSnapshot.getKey() + ":" + messageSnapshot.getKey());
            }
        }
        String notificationText = "";
        if (totalMessageCount >= 1) {
            if (totalMessageCount == 1) {
                notificationText += "1 new message";
            } else {
                notificationText += totalMessageCount + " new messages";
            }
            PendingIntent pendingIntent;
            if (chatCount == 1) {
                notificationText += " in " + chatCount + " chat";
                pendingIntent = getIntentForSingleChat(unreadSnapshot.getChildren().iterator().next().getKey());
            } else {
                notificationText += " in " + chatCount + " chats";
                pendingIntent = getIntentForMultipleChats();
            }
            mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(notificationText)
                    .setSmallIcon(R.mipmap.ic_launcher);
            mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private PendingIntent getIntentForSingleChat(String chatKey) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.ARG_CHAT_KEY, chatKey);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(ChatActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getIntentForMultipleChats() {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
