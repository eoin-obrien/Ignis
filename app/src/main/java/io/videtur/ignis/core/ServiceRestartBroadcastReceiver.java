package io.videtur.ignis.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceRestartBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        context.startService(new Intent(context, NotificationService.class));
    }
}
