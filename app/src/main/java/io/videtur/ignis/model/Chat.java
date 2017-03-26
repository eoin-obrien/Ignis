package io.videtur.ignis.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Chat {

    private Map<String, Object> mChatNames;
    private Map<String, Object> mChatProfilePhotos;
    private String mChatProfilePhoto;
    private String mChatName;
    private String mLastMessageText;
    private String mLastMessageSenderName;
    private String mLastMessageTimestamp;

    public Chat() {
        // empty constructor for Firebase
    }

    public Chat(Map<String, Object> mChatNames, Map<String, Object> mChatProfilePhotos) {
        this.mChatNames = mChatNames;
        this.mChatProfilePhotos = mChatProfilePhotos;
    }

    public Chat(String mChatProfilePhoto, String mChatName) {
        this.mChatProfilePhoto = mChatProfilePhoto;
        this.mChatName = mChatName;
    }

    @Exclude
    public String getChatName(String userKey) {
        if (mChatNames != null) {
            return (String) mChatNames.get(userKey);
        } else {
            return mChatName;
        }
    }

    @Exclude
    public String getChatProfilePhoto(String userKey) {
        if (mChatProfilePhotos != null) {
            return (String) mChatProfilePhotos.get(userKey);
        } else {
            return mChatProfilePhoto;
        }
    }

    public Map<String, Object> getChatNames() {
        return mChatNames;
    }

    public void setChatNames(Map<String, Object> chatNames) {
        this.mChatNames = chatNames;
    }

    public Map<String, Object> getChatProfilePhotos() {
        return mChatProfilePhotos;
    }

    public void setChatProfilePhotos(Map<String, Object> chatProfilePhotos) {
        this.mChatProfilePhotos = chatProfilePhotos;
    }

    public String getChatProfilePhoto() {
        return mChatProfilePhoto;
    }

    public void setChatProfilePhoto(String chatProfilePhoto) {
        this.mChatProfilePhoto = chatProfilePhoto;
    }

    public String getChatName() {
        return mChatName;
    }

    public void setChatName(String chatName) {
        this.mChatName = chatName;
    }

    public String getLastMessageText() {
        return mLastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.mLastMessageText = lastMessageText;
    }

    public String getLastMessageSenderName() {
        return mLastMessageSenderName;
    }

    public void setLastMessageSenderName(String lastMessageSenderName) {
        this.mLastMessageSenderName = lastMessageSenderName;
    }

    public String getLastMessageTimestamp() {
        return mLastMessageTimestamp;
    }

    public void setLastMessageTimestamp(String lastMessageTimestamp) {
        this.mLastMessageTimestamp = lastMessageTimestamp;
    }
}
