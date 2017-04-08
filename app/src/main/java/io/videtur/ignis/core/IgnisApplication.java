package io.videtur.ignis.core;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Application class for setting up Firebase Database persistence.
 */
public class IgnisApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase database persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
