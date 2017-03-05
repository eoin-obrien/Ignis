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
import com.google.firebase.database.ValueEventListener;

import io.videtur.ignis.R;
import io.videtur.ignis.SignInActivity;
import io.videtur.ignis.model.User;

import static io.videtur.ignis.util.Constants.USERS_REF;

public abstract class IgnisAuthActivity extends AppCompatActivity
        implements AuthStateListener {

    private static final String TAG = "IgnisAuthActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserListenerRef;
    private ValueEventListener mUserListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUsersRef = mDatabase.getReference(USERS_REF);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(this);
    }

    @Override
    public final void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());

            // Setup key and database reference for the user
            String userKey = Util.getKeyFromEmail(firebaseUser.getEmail());
            final DatabaseReference userRef = mUsersRef.child(userKey);

            // Remove old user ValueEventListener
            if (mUserListener != null) {
                mUserListenerRef.removeEventListener(mUserListener);
            }

            // Fetch the user from the database
            mUserListenerRef = userRef;
            mUserListener = userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
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

    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showToast(R.string.sign_out_successful);
                        finish();
                    }
                });
    }

    public void showToast(int stringResource) {
        Toast.makeText(this, stringResource, Toast.LENGTH_SHORT).show();
    }

}
