package com.example.telemedicine;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessageThreadActivity extends AppCompatActivity {

    private RecyclerView recyclerMessages;
    private TextInputEditText editMessage;
    private ImageButton btnSend;
    private ImageView btnBack;
    private TextView textChatUserName;
    private TextView textUserOnlineStatus;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String otherUserId;
    private String otherUserName;
    private String currentUserId;

    private com.google.firebase.firestore.ListenerRegistration outgoingMessagesListener;
    private com.google.firebase.firestore.ListenerRegistration incomingMessagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_thread);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();

        otherUserId = getIntent().getStringExtra("other_user_id");
        otherUserName = getIntent().getStringExtra("other_user_name");
        
        if (otherUserId == null) {
            otherUserId = "doctor1";
        }
        if (otherUserName == null) {
            otherUserName = "Chat";
        }

        initializeViews();
        setupRecyclerView();
        resolveOtherUserProfile();
        loadMessages();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerMessages = findViewById(R.id.recycler_messages);
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back_chat);
        textChatUserName = findViewById(R.id.text_chat_user_name);
        textUserOnlineStatus = findViewById(R.id.text_user_online_status);
        
        textChatUserName.setText(getDisplayName(otherUserName));
        textUserOnlineStatus.setText("Secure direct message");
        textUserOnlineStatus.setVisibility(android.view.View.VISIBLE);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void resolveOtherUserProfile() {
        if (otherUserId == null || otherUserId.trim().isEmpty()) {
            textChatUserName.setText(getDisplayName(otherUserName));
            return;
        }

        db.collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                        otherUserName = user.getFullName().trim();
                    }
                    textChatUserName.setText(getDisplayName(otherUserName));
                })
                .addOnFailureListener(e -> textChatUserName.setText(getDisplayName(otherUserName)));
    }

    private void loadMessages() {
        if (outgoingMessagesListener != null) {
            outgoingMessagesListener.remove();
        }

        outgoingMessagesListener = db.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", otherUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Message message = dc.getDocument().toObject(Message.class);
                            if (!messageList.contains(message)) {
                                messageList.add(message);
                            }
                        }
                    }
                    loadIncomingMessages();
                });
    }

    private void loadIncomingMessages() {
        if (incomingMessagesListener != null) {
            incomingMessagesListener.remove();
        }

        incomingMessagesListener = db.collection("messages")
                .whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Message message = dc.getDocument().toObject(Message.class);
                            if (!messageList.contains(message)) {
                                messageList.add(message);
                                messageList.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
                                messageAdapter.notifyDataSetChanged();
                                recyclerMessages.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        Message message = new Message(currentUserId, otherUserId, messageText);

        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    editMessage.setText("");
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerMessages.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MessageThreadActivity.this,
                        "Failed to send message: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (outgoingMessagesListener != null) {
            outgoingMessagesListener.remove();
        }
        if (incomingMessagesListener != null) {
            incomingMessagesListener.remove();
        }
    }

    private String getDisplayName(String name) {
        return name != null && !name.trim().isEmpty() ? name.trim() : "Chat";
    }
}
