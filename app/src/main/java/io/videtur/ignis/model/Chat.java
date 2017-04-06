package io.videtur.ignis.model;

import java.util.Map;

@SuppressWarnings("unused")
public class Chat {

    private String chatProfilePhoto;
    private String chatName;
    private String lastMessage;
    private Map<String, Object> members;

    public Chat() {
        // empty constructor for Firebase
    }

    public Chat(Map<String, Object> chatMembers) {
        this.members = chatMembers;
    }

    public Chat(String chatProfilePhoto, String chatName) {
        this.chatProfilePhoto = chatProfilePhoto;
        this.chatName = chatName;
    }

    public String getChatProfilePhoto() {
        return chatProfilePhoto;
    }

    public void setChatProfilePhoto(String chatProfilePhoto) {
        this.chatProfilePhoto = chatProfilePhoto;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Map<String, Object> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Object> members) {
        this.members = members;
    }
}
