package io.videtur.ignis.model;

import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

/**
 * Data model object for users.
 */
@SuppressWarnings("unused")
public class User {

    private String name;
    private String email;
    private String photoUrl;
    private long lastOnline;
    private Map<String, Object> connections;
    private Map<String, Object> chats;
    private Map<String, Object> undelivered;
    private Map<String, Object> unread;

    public User() {
        // Empty constructor for Firebase
    }

    public User(FirebaseUser firebaseUser) {
        name = firebaseUser.getDisplayName();
        email = firebaseUser.getEmail();
        if (firebaseUser.getPhotoUrl() != null) {
            photoUrl = firebaseUser.getPhotoUrl().toString();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String mEmail) {
        this.email = mEmail;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.photoUrl = mPhotoUrl;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long mLastOnline) {
        this.lastOnline = mLastOnline;
    }

    public Map<String, Object> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, Object> connections) {
        this.connections = connections;
    }

    public Map<String, Object> getChats() {
        return chats;
    }

    public void setChats(Map<String, Object> chats) {
        this.chats = chats;
    }

    public Map<String, Object> getUnread() {
        return unread;
    }

    public void setUnread(Map<String, Object> unread) {
        this.unread = unread;
    }

    public Map<String, Object> getUndelivered() {
        return undelivered;
    }

    public void setUndelivered(Map<String, Object> undelivered) {
        this.undelivered = undelivered;
    }
}
