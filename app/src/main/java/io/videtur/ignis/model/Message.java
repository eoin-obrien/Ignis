package io.videtur.ignis.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Message {

    private String text;
    private String senderKey;
    private String senderName;
    private String senderPhotoUrl;
    private Map<String, Object> readReceipts;
    private Map<String, Object> deliveryReceipts;
    private Map<String, Object> timestamp;

    public Message() {
    }

    public Message(String text, String senderKey, String senderName, String senderPhotoUrl) {
        this.text = text;
        this.senderKey = senderKey;
        this.senderName = senderName;
        this.senderPhotoUrl = senderPhotoUrl;
        this.timestamp = new HashMap<>();
        this.timestamp.put("TIMESTAMP", ServerValue.TIMESTAMP);
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public Map<String, Object> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, Object> timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public long getTimestampLong() {
        return (long) timestamp.get("TIMESTAMP");
    }

    public Map<String, Object> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(Map<String, Object> readReceipts) {
        this.readReceipts = readReceipts;
    }

    public Map<String, Object> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public void setDeliveryReceipts(Map<String, Object> deliveryReceipts) {
        this.deliveryReceipts = deliveryReceipts;
    }
}
