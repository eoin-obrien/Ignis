package io.videtur.ignis.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import io.videtur.ignis.R;
import io.videtur.ignis.SignInActivity;
import io.videtur.ignis.model.User;

import static io.videtur.ignis.util.Constants.CONNECTED_REF;
import static io.videtur.ignis.util.Constants.CONNECTIONS_CHILD;
import static io.videtur.ignis.util.Constants.LAST_ONLINE_CHILD;
import static io.videtur.ignis.util.Constants.USERS_REF;

public abstract class IgnisAuthActivity extends AppCompatActivity
        implements AuthStateListener {

    private static final String TAG = "IgnisAuthActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserListenerRef;
    private DatabaseReference mCurrentConnectionRef;
    private DatabaseReference mCurrentLastOnlineRef;
    private DatabaseReference mConnectedRef;
    private ValueEventListener mUserListener;
    private ValueEventListener mConnectedListener;
    private String mPresentUserKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUsersRef = mDatabase.getReference(USERS_REF);
        mConnectedRef = mDatabase.getReference(CONNECTED_REF);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start listening for authentication state changes
        mAuth.addAuthStateListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop listening for authentication state changes
        mAuth.removeAuthStateListener(this);
        removeUserStateListener();

        // Mark user as no longer present in this activity
        removeUserPresenceListener();
        dropCurrentConnection();
    }

    @Override
    public final void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());

            // Setup key and database reference for the user
            final String userKey = Util.getKeyFromEmail(firebaseUser.getEmail());
            final DatabaseReference userRef = mUsersRef.child(userKey);

            // Remove old user ValueEventListener
            removeUserStateListener();

            // Fetch the user from the database
            mUserListenerRef = userRef;
            mUserListener = userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Setup user presence system for the user
                        if (mConnectedListener == null || !mPresentUserKey.equals(userKey)) {
                            addUserPresenceListener(userKey);
                        }
                        // Fire event
                        onUserDataChange(dataSnapshot.getValue(User.class));
                    } else {
                        // Initialize the user in the database
                        final User user = new User(firebaseUser);
                        userRef.setValue(user, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                onUserDataChange(user);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.getMessage());
                }
            });
        } else {
            Log.d(TAG, "onAuthStateChanged:signed_out");
            // Not authenticated, start the SignInActivity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    public void onUserDataChange(User user) {
        Log.d(TAG, "onUserDataChange");
    }

    public void signOut() {
        // Remove state listeners
        mAuth.removeAuthStateListener(this);
        removeUserStateListener();

        // Mark user as no longer present in this activity
        removeUserPresenceListener();
        dropCurrentConnection();

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showToast(R.string.sign_out_successful);
                        startActivity(new Intent(IgnisAuthActivity.this, SignInActivity.class));
                        finish();
                    }
                });
    }

    public void showToast(int stringResource) {
        Toast.makeText(this, stringResource, Toast.LENGTH_SHORT).show();
    }

    private void dropCurrentConnection() {
        if (mCurrentConnectionRef != null) {
            mCurrentConnectionRef.removeValue();
            mCurrentConnectionRef = null;
            mCurrentLastOnlineRef.setValue(ServerValue.TIMESTAMP);
            mCurrentLastOnlineRef = null;
        }
    }

    private void removeUserStateListener() {
        if (mUserListener != null) {
            mUserListenerRef.removeEventListener(mUserListener);
            mUserListener = null;
            mUserListenerRef = null;
        }
    }

    private void removeUserPresenceListener() {
        if (mConnectedListener != null) {
            mConnectedRef.removeEventListener(mConnectedListener);
            mConnectedListener = null;
        }
    }

    private void addUserPresenceListener(String userKey) {
        // Remove old user presence listener
        removeUserPresenceListener();

        // References related to user presence
        final DatabaseReference userConnectionsRef = mUsersRef.child(userKey).child(CONNECTIONS_CHILD);
        mCurrentLastOnlineRef = mUsersRef.child(userKey).child(LAST_ONLINE_CHILD);
        mPresentUserKey = userKey;

        // Listen for changes in client connection
        mConnectedListener = mConnectedRef.addValueEventListener(new ValueEventListener() {
            private static final String TAG = "UserPresenceSystem";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isConnected = dataSnapshot.getValue(Boolean.class);
                Log.d(TAG, "connected:" + isConnected);
                if (isConnected) {
                    // Drop old connection from this device before adding a new one
                    dropCurrentConnection();

                    // Push this device to the user's list of connections
                    mCurrentConnectionRef = userConnectionsRef.push();
                    mCurrentConnectionRef.setValue(Boolean.TRUE);

                    // When this device disconnects, drop the connection
                    mCurrentConnectionRef.onDisconnect().removeValue();

                    // When this device disconnects, update the last online time
                    mCurrentLastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

}
