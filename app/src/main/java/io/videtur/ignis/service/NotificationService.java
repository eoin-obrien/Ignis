package io.videtur.ignis.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by eoin on 30/03/17.
 */

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";

    private FirebaseAuth mAuth;
    private DatabaseReference mChatsRef;
    private DatabaseReference mMessagesRef;
    private DatabaseReference mUnreadMessagesRef;

    public NotificationService(Context applicationContext) {
        super();
    }

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        // TODO set up listener on unread messages
        // TODO mark unread messages as delivered
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        // TODO restart service if destroyed
        // Intent broadcastIntent = new Intent("BroadcastReceiver");
        // sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
