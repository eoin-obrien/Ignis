package io.videtur.ignis.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Chat {

    private Map<String, Object> mChatUsers;
    private String mChatProfilePhoto;
    private String mChatName;
    private String mLastMessageText;
    private String mLastMessageSenderName;
    private String mLastMessageTimestamp;

    public Chat() {
        // empty constructor for Firebase
    }

    public Chat(Map<String, Object> chatUsers) {
        this.mChatUsers = chatUsers;
    }

    public Chat(String mChatProfilePhoto, String mChatName) {
        this.mChatProfilePhoto = mChatProfilePhoto;
        this.mChatName = mChatName;
    }

    @Exclude
    public String getChatUser(String userKey) {
        return (String) mChatUsers.get(userKey);
    }

    public Map<String, Object> getChatUsers() {
        return mChatUsers;
    }

    public void setChatUsers(Map<String, Object> chatUsers) {
        this.mChatUsers = chatUsers;
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
