package io.videtur.ignis.core;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.videtur.ignis.R;
import io.videtur.ignis.ui.ChatActivity;
import io.videtur.ignis.ui.MainActivity;

import static io.videtur.ignis.core.Constants.DELIVERY_RECEIPTS_CHILD;
import static io.videtur.ignis.core.Constants.LED_COLOR;
import static io.videtur.ignis.core.Constants.MESSAGES_REF;
import static io.videtur.ignis.core.Constants.NOTIFICATION_ID;
import static io.videtur.ignis.core.Constants.RESTART_BROADCAST;
import static io.videtur.ignis.core.Constants.UNDELIVERED_CHILD;
import static io.videtur.ignis.core.Constants.UNREAD_CHILD;
import static io.videtur.ignis.core.Constants.USERS_REF;
import static io.videtur.ignis.core.Util.getKeyFromEmail;

/**
 * Service for notifying the user when new messages are received.
 */
public class NotificationService extends Service {

    private static final String TAG = "NotificationService";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUnreadMessagesRef;
    private DatabaseReference mUndeliveredMessagesRef;

    private ValueEventListener mUnreadMessagesListener;

    private Resources mRes;
    private String mUserKey;

    private NotificationManager mNotificationManager;

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");

        mRes = getApplicationContext().getResources();

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

                    mUserKey = getKeyFromEmail(firebaseAuth.getCurrentUser().getEmail());
                    mDatabase = FirebaseDatabase.getInstance();
                    mUnreadMessagesRef = mDatabase.getReference(USERS_REF).child(mUserKey).child(UNREAD_CHILD);
                    mUndeliveredMessagesRef = mDatabase.getReference(USERS_REF).child(mUserKey).child(UNDELIVERED_CHILD);

                    // Listen for unread messages
                    mUnreadMessagesListener = mUnreadMessagesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot unreadDataSnapshot) {
                            // Get a snapshot of undelivered messages for reference
                            mUndeliveredMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot undeliveredDataSnapshot) {
                                    // Update the notification
                                    notifyMessages(unreadDataSnapshot, undeliveredDataSnapshot);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    if (databaseError != null) {
                                        Log.e(TAG, databaseError.getMessage());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (databaseError != null) {
                                Log.e(TAG, databaseError.getMessage());
                            }
                        }
                    });
                }
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        mAuth.removeAuthStateListener(mAuthStateListener);
        // Restart service if destroyed
        Intent broadcastIntent = new Intent(RESTART_BROADCAST);
        sendBroadcast(broadcastIntent);
    }

    private void notifyMessages(DataSnapshot unreadSnapshot, DataSnapshot undeliveredDataSnapshot) {
        long chatCount = unreadSnapshot.getChildrenCount();
        long totalMessageCount = 0;
        Map<String, Object> updates = new HashMap<>();

        // Get the total number of unread messages
        for (DataSnapshot chatSnapshot : unreadSnapshot.getChildren()) {
            totalMessageCount += chatSnapshot.getChildrenCount();
        }

        // Mark all undelivered messages as delivered
        for (DataSnapshot chatSnapshot : undeliveredDataSnapshot.getChildren()) {
            for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                updates.put("/" + USERS_REF + "/" + mUserKey + "/" + UNDELIVERED_CHILD + "/" + chatSnapshot.getKey()
                        + "/" + messageSnapshot.getKey(), null);
                updates.put("/" + MESSAGES_REF + "/" + chatSnapshot.getKey() + "/" + messageSnapshot.getKey()
                        + "/" + DELIVERY_RECEIPTS_CHILD + "/" + mUserKey, ServerValue.TIMESTAMP);
            }
        }

        if (totalMessageCount >= 1) {
            // Build and send the notification
            PendingIntent pendingIntent;
            if (chatCount == 1) {
                pendingIntent = getIntentForSingleChat(unreadSnapshot.getChildren().iterator().next().getKey());
            } else {
                pendingIntent = getIntentForMultipleChats();
            }
            NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(getNotificationText(chatCount, totalMessageCount))
                    .setContentInfo(String.valueOf(totalMessageCount))
                    .setSmallIcon(R.drawable.ic_stat_message)
                    .setLights(LED_COLOR, 1000, 1000);
            // If there were some undelivered messages, then play a sound and vibrate
            if (updates.size() > 0) {
                mNotifyBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
                mNotifyBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            }
            mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
            // Push delivery receipts to Firebase
            mDatabase.getReference().updateChildren(updates);
        } else {
            // No unread messages, so remove the notification
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private String getNotificationText(long chatCount, long messageCount) {
        return mRes.getQuantityString(R.plurals.number_of_unread_messages, (int) messageCount, messageCount)
                + " " + mRes.getQuantityString(R.plurals.number_of_unread_chats, (int) chatCount, chatCount);
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
