package io.videtur.ignis.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Chat {

    private String mChatProfilePhoto;
    private String mChatName;
    private String mLastMessageText;
    private String mLastMessageSenderName;
    private String mLastMessageTimestamp;
    private Map<String, Object> mMembers;

    public Chat() {
        // empty constructor for Firebase
    }

    public Chat(Map<String, Object> chatMembers) {
        this.mMembers = chatMembers;
    }

    public Chat(String mChatProfilePhoto, String mChatName) {
        this.mChatProfilePhoto = mChatProfilePhoto;
        this.mChatName = mChatName;
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

    public Map<String, Object> getMembers() {
        return mMembers;
    }

    public void setMembers(Map<String, Object> members) {
        this.mMembers = members;
    }
}
