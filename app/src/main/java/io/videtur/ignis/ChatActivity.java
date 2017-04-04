package io.videtur.ignis;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static io.videtur.ignis.util.Constants.CHATS_REF;
import static io.videtur.ignis.util.Constants.MESSAGES_REF;
import static io.videtur.ignis.util.Constants.MESSAGE_FROM_USER;
import static io.videtur.ignis.util.Constants.MESSAGE_TO_USER;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.FirebaseUtil.markMessageAsRead;
import static io.videtur.ignis.util.FirebaseUtil.sendMessage;
import static io.videtur.ignis.util.Util.formatTimestamp;

public class ChatActivity extends IgnisAuthActivity {

    private static final String TAG = "ChatActivity";

    public static final String ARG_CHAT_KEY = "arg_chat_key";

    private String mChatKey;
    private String mUserKey;
    private String mUserName;
    private String mUserProfilePhotoUrl;
    private Chat mChat;

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
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mChatKey = getIntent().getStringExtra(ARG_CHAT_KEY);
        assert mChatKey != null;

        mChatRef = getDatabase().getReference(CHATS_REF).child(mChatKey);
        mMessagesRef = getDatabase().getReference(MESSAGES_REF).child(mChatKey);

        // get View references
        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mToolbarImage = (ImageView) findViewById(R.id.toolbar_image);
        mToolbarPrimaryText = (TextView) findViewById(R.id.toolbar_primary_text);
        mToolbarSecondaryText = (TextView) findViewById(R.id.toolbar_secondary_text);
        mChatRecycler = (RecyclerView) findViewById(R.id.chat_recycler);
        mMessageEditText = (EditText) findViewById(R.id.message_edit_text);
        mSendButton = (Button) findViewById(R.id.send_button);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        // set up listener on FAB to return to the bottom of the chat
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatRecycler.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        });

        // hide FAB initially
        mFab.setVisibility(View.GONE);

        // show FAB if scrolling down and not at last message
        mChatRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastItem = mLayoutManager.findLastVisibleItemPosition();
                if (dy > 0 && lastItem < mChatAdapter.getItemCount() - 1) {
                    mFab.setVisibility(View.VISIBLE);
                } else {
                    mFab.setVisibility(View.GONE);
                }
            }
        });

        // EditText and Button should start off disabled
        mMessageEditText.setEnabled(false);
        mSendButton.setEnabled(false);

        // set up listener on mSendButton to send message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageText = mMessageEditText.getText().toString();
                Message message = new Message(messageText, mUserKey, mUserName, mUserProfilePhotoUrl);
                sendMessage(getDatabase(), message, mChat, mChatKey, mUserKey);
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);

        mUserKey = key;
        mUserName = user.getName();
        mUserProfilePhotoUrl = user.getPhotoUrl();

        // messages can be sent once the user is authenticated
        if (mChat == null) {
            mChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mChat = dataSnapshot.getValue(Chat.class);
                    setUpChatRecycler();
                    setUpChatToolbar();
                    mMessageEditText.setEnabled(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (databaseError != null) {
                        Log.e(TAG, databaseError.getMessage());
                    }
                }
            });
        }
    }

    private void setUpChatToolbar() {
        // get user ID from Chat model and fill the toolbar with those values
        final String contactKey;
        if (!mChat.getMembers().keySet().toArray()[0].equals(mUserKey)) {
            contactKey = (String) mChat.getMembers().keySet().toArray()[0];
        } else {
            contactKey = (String) mChat.getMembers().keySet().toArray()[1];
        }
        mToolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ContactInfoActivity.class);
                intent.putExtra(ContactInfoActivity.ARG_CONTACT_KEY, contactKey);
                startActivity(intent);
            }
        });
        getDatabase().getReference(USERS_REF).child(contactKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User contact = dataSnapshot.getValue(User.class);
                        mToolbarPrimaryText.setText(contact.getName());
                        mToolbarSecondaryText.setText(formatTimestamp(contact.getLastOnline(),
                                getResources().getString(R.string.last_online_timestamp_same_day),
                                getResources().getString(R.string.last_online_timestamp_same_week),
                                getResources().getString(R.string.last_online_timestamp_default)));
                        Glide.with(ChatActivity.this)
                                .load(contact.getPhotoUrl())
                                .into(mToolbarImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (databaseError != null) {
                            Log.e(TAG, databaseError.getMessage());
                        }
                    }
                });
    }

    private void setUpChatRecycler() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);

        mChatAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(Message.class,
                R.layout.list_item_message, MessageHolder.class, mMessagesRef) {
            @Override
            public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType == MESSAGE_FROM_USER) {
                    View messageView = LayoutInflater.from(ChatActivity.this)
                            .inflate(R.layout.item_message_from_user, parent, false);
                    return new MessageHolder(messageView);
                } else {
                    View messageView = LayoutInflater.from(ChatActivity.this)
                            .inflate(R.layout.item_message_to_user, parent, false);
                    return new MessageHolder(messageView);
                }
            }

            @Override
            public int getItemViewType(int position) {
                Message message = getItem(position);
                if (message.getSenderKey().equals(mUserKey)) {
                    return MESSAGE_FROM_USER;
                } else {
                    return MESSAGE_TO_USER;
                }
            }

            @Override
            protected void populateViewHolder(MessageHolder viewHolder, Message model, int position) {
                String messageKey = this.getRef(position).getKey();
                viewHolder.mMessageText.setText(model.getText());
                viewHolder.mTimestampText.setText(formatTimestamp(model.getTimestampLong(),
                        getResources().getString(R.string.message_timestamp_same_day),
                        getResources().getString(R.string.message_timestamp_same_week),
                        getResources().getString(R.string.message_timestamp_default)));

                if (viewHolder.mReadReceipt != null) {
                    if (model.getReadReceipts() != null) {
                        viewHolder.mReadReceipt.setImageResource(R.drawable.ic_message_seen);
                    } else if (model.getDeliveryReceipts() != null) {
                        viewHolder.mReadReceipt.setImageResource(R.drawable.ic_message_delivered);
                    } else {
                        viewHolder.mReadReceipt.setImageResource(R.drawable.ic_message_pending);
                    }
                }

                // Mark message as read
                if (!model.getSenderKey().equals(mUserKey) && (model.getReadReceipts() == null
                        || !model.getReadReceipts().containsKey(mUserKey))) {
                    markMessageAsRead(getDatabase(), mUserKey, mChatKey, messageKey);
                }
            }
        };
        mChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int messageCount = mChatAdapter.getItemCount();
                int lastCompletelyVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastCompletelyVisiblePosition == NO_POSITION || (positionStart >= (messageCount - 1)
                        && lastCompletelyVisiblePosition == (positionStart - 1))) {
                    mChatRecycler.scrollToPosition(positionStart);
                }
            }
        });

        mChatRecycler.setLayoutManager(mLayoutManager);
        mChatRecycler.setAdapter(mChatAdapter);
    }

    private static class MessageHolder extends RecyclerView.ViewHolder {
        private final TextView mMessageText;
        private final TextView mTimestampText;
        private final ImageView mReadReceipt;

        MessageHolder(View itemView) {
            super(itemView);
            mMessageText = (TextView) itemView.findViewById(R.id.message_text);
            mTimestampText = (TextView) itemView.findViewById(R.id.timestamp_text);
            mReadReceipt = (ImageView) itemView.findViewById(R.id.chat_read_receipt);
        }
    }
}
