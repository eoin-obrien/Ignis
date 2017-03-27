package io.videtur.ignis;

import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.videtur.ignis.model.Chat;
import io.videtur.ignis.model.Message;
import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;
import io.videtur.ignis.util.Util;

import static io.videtur.ignis.util.Constants.CHAT_REF;
import static io.videtur.ignis.util.Constants.MESSAGES_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.Util.formatLastOnlineTime;

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
    private EditText mMessageEditText;
    private Button mSendButton;
    private View mToolbarLayout;
    private ImageView mToolbarImage;
    private TextView mToolbarPrimaryText;
    private TextView mToolbarSecondaryText;

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
        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mToolbarImage = (ImageView) findViewById(R.id.toolbar_image);
        mToolbarPrimaryText = (TextView) findViewById(R.id.toolbar_primary_text);
        mToolbarSecondaryText = (TextView) findViewById(R.id.toolbar_secondary_text);
        mChatRecycler = (RecyclerView) findViewById(R.id.chat_recycler);
        mMessageEditText = (EditText) findViewById(R.id.message_edit_text);
        mSendButton = (Button) findViewById(R.id.send_button);

        // EditText and Button should start off disabled
        mMessageEditText.setEnabled(false);
        mSendButton.setEnabled(false);

        // set up listener on mSendButton to send message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = mMessageEditText.getText().toString();
                Message message = new Message(messageText, mUserKey, mUserName, mUserProfilePhotoUrl);
                mMessagesRef.push().setValue(message);
                mMessageEditText.setText("");
            }
        });

        // set up listener on EditText to enable/disable the send Button
        mMessageEditText.addTextChangedListener(new TextWatcher() {
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
                    mSendButton.setEnabled(false);
                } else {
                    mSendButton.setEnabled(true);
                }
            }
        });

        setUpChatRecycler();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);

        mUserKey = key;
        mUserName = user.getName();
        mUserProfilePhotoUrl = user.getPhotoUrl();

        setUpChatToolbar();

        // messages can be sent once the user is authenticated
        mMessageEditText.setEnabled(true);
    }

    private void setUpChatToolbar() {
        // get user ID from Chat model and fill the toolbar with those values
        mChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Chat chat = dataSnapshot.getValue(Chat.class);
                mToolbarLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, ContactInfoActivity.class);
                        intent.putExtra(ContactInfoActivity.ARG_CONTACT_KEY, chat.getChatUser(mUserKey));
                        startActivity(intent);
                    }
                });
                getDatabase().getReference(USERS_REF).child(chat.getChatUser(mUserKey))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User contact = dataSnapshot.getValue(User.class);
                                mToolbarPrimaryText.setText(contact.getName());
                                mToolbarSecondaryText.setText(formatLastOnlineTime(contact.getLastOnline()));
                                Glide.with(ChatActivity.this)
                                        .load(contact.getPhotoUrl())
                                        .into(mToolbarImage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpChatRecycler() {
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
