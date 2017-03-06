package io.videtur.ignis.model;

import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class User {

    private static final String TAG = "User";

    private String mName;
    private String mEmail;
    private String mPhotoUrl;
    private long mLastOnline;
    private Map<String, Object> connections;

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

    public long getLastOnline() {
        return mLastOnline;
    }

    public void setLastOnline(long mLastOnline) {
        this.mLastOnline = mLastOnline;
    }

    public Map<String, Object> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, Object> connections) {
        this.connections = connections;
    }
}
