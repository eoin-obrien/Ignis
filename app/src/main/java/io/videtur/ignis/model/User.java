package io.videtur.ignis.model;

import com.google.firebase.auth.FirebaseUser;

public class User {

    private static final String TAG = "User";

    private String mName;
    private String mEmail;
    private String mPhotoUrl;

    public User() {
    }

    public User(FirebaseUser firebaseUser) {
        mName = firebaseUser.getDisplayName();
        mEmail = firebaseUser.getEmail();
        if (firebaseUser.getPhotoUrl() != null) {
            mPhotoUrl = firebaseUser.getPhotoUrl().toString();
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }
}
