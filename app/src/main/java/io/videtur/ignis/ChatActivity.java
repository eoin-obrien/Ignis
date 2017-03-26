package io.videtur.ignis;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import io.videtur.ignis.model.Message;
import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.CHAT_REF;
import static io.videtur.ignis.util.Constants.MESSAGES_REF;

public class ChatActivity extends IgnisAuthActivity {

    private static final String TAG = "ChatActivity";

    public static final String ARG_CHAT_KEY = "arg_chat_key";

    private String mChatKey;
    private String mUserKey;
    private String mUserName;
    private String mUserProfilePhotoUrl;
    private boolean mIsGroupChat;

    private DatabaseReference mChatRef;
    private DatabaseReference mMessagesRef;

    private RecyclerView mChatRecycler;
    private FirebaseRecyclerAdapter<Message, MessageHolder> mChatAdapter;
    private LinearLayoutManager mLayoutManager;
    private EditText messageEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mChatKey = getIntent().getStringExtra(ARG_CHAT_KEY);
        assert mChatKey != null;

        mChatRef = getDatabase().getReference(CHAT_REF).child(mChatKey);
        mMessagesRef = getDatabase().getReference(MESSAGES_REF).child(mChatKey);

        // get View references
        mChatRecycler = (RecyclerView) findViewById(R.id.chat_recycler);
        messageEditText = (EditText) findViewById(R.id.message_edit_text);
        sendButton = (Button) findViewById(R.id.send_button);

        // EditText and Button should start off disabled
        messageEditText.setEnabled(false);
        sendButton.setEnabled(false);

        // set up listener on sendButton to send message
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                Message message = new Message(messageText, mUserKey, mUserName, mUserProfilePhotoUrl);
                mMessagesRef.push().setValue(message);
                messageEditText.setText("");
            }
        });

        // set up listener on EditText to enable/disable the send Button
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Send button should be enabled if there is a message to be sent
                if (s.toString().trim().isEmpty()) {
                    sendButton.setEnabled(false);
                } else {
                    sendButton.setEnabled(true);
                }
            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);

        mChatAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(Message.class,
                R.layout.list_item_message, MessageHolder.class, mMessagesRef) {
            @Override
            protected void populateViewHolder(MessageHolder viewHolder, Message model, int position) {
                viewHolder.mMessageText.setText(model.getText());
                viewHolder.mSenderName.setText(model.getSenderName());
                // TODO format timestamp
                // viewHolder.mTimestampText.setText(String.valueOf(model.getTimestampLong()));
                Glide.with(ChatActivity.this)
                        .load(model.getSenderPhotoUrl())
                        .into(viewHolder.mSenderProfileImage);
            }
        };
        mChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mChatAdapter.getItemCount();
                int lastCompletelyVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastCompletelyVisiblePosition == -1 || (positionStart >= (messageCount - 1)
                        && lastCompletelyVisiblePosition == (positionStart - 1))) {
                    mChatRecycler.scrollToPosition(positionStart);
                }
            }
        });

        mChatRecycler.setLayoutManager(mLayoutManager);
        mChatRecycler.setAdapter(mChatAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);

        mUserKey = key;
        mUserName = user.getName();
        mUserProfilePhotoUrl = user.getPhotoUrl();

        // messages can be sent once the user is authenticated
        messageEditText.setEnabled(true);
    }

    private static class MessageHolder extends RecyclerView.ViewHolder {
        private ImageView mSenderProfileImage;
        private TextView mMessageText;
        private TextView mSenderName;
        private TextView mTimestampText;

        public MessageHolder(View itemView) {
            super(itemView);

            mSenderProfileImage = (ImageView) itemView.findViewById(R.id.sender_profile_image);
            mMessageText = (TextView) itemView.findViewById(R.id.message_text);
            mSenderName = (TextView) itemView.findViewById(R.id.sender_text);
            mTimestampText = (TextView) itemView.findViewById(R.id.timestamp_text);
        }
    }
}
