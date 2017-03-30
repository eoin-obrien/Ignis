package io.videtur.ignis.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private static final String TAG = "Message";

    private String mText;
    private String mSenderKey;
    private String mSenderName;
    private String mSenderPhotoUrl;
    private Map<String, Object> mReadReceipts;
    private Map<String, Object> mDeliveryReceipts;

    private Map<String, Object> mTimestamp;

    public Message() {
    }

    public Message(String text, String senderKey, String senderName, String senderPhotoUrl) {
        this.mText = text;
        this.mSenderKey = senderKey;
        this.mSenderName = senderName;
        this.mSenderPhotoUrl = senderPhotoUrl;
        this.mTimestamp = new HashMap<>();
        this.mTimestamp.put("TIMESTAMP", ServerValue.TIMESTAMP);
    }


    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getSenderKey() {
        return mSenderKey;
    }

    public void setSenderKey(String senderKey) {
        mSenderKey = senderKey;
    }

    public String getSenderName() {
        return mSenderName;
    }

    public void setSenderName(String senderName) {
        mSenderName = senderName;
    }

    public String getSenderPhotoUrl() {
        return mSenderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        mSenderPhotoUrl = senderPhotoUrl;
    }

    public Map<String, Object> getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Map<String, Object> timestamp) {
        mTimestamp = timestamp;
    }

    @Exclude
    public long getTimestampLong() {
        return (long) mTimestamp.get("TIMESTAMP");
    }

    public Map<String, Object> getReadReceipts() {
        return mReadReceipts;
    }

    public void setReadReceipts(Map<String, Object> readReceipts) {
        this.mReadReceipts = readReceipts;
    }

    public Map<String, Object> getDeliveryReceipts() {
        return mDeliveryReceipts;
    }

    public void setDeliveryReceipts(Map<String, Object> deliveryReceipts) {
        this.mDeliveryReceipts = deliveryReceipts;
    }
}
