package com.example.telemedicine;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String otherUserId; // The person we're chatting with
    private String currentUserId;

    private com.google.firebase.firestore.ListenerRegistration outgoingMessagesListener;
    private com.google.firebase.firestore.ListenerRegistration incomingMessagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_thread);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        // Get the other user ID from intent extras
        // In a real app, you would pass this when starting the activity
        otherUserId = getIntent().getStringExtra("other_user_id");
        if (otherUserId == null) {
            // For demo purposes, we'll use a placeholder
            otherUserId = "doctor1"; // This would normally come from the contact/appointment
        }

        initializeViews();

        // Setup toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupRecyclerView();
        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initializeViews() {
        recyclerMessages = findViewById(R.id.recycler_messages);
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        // Remove previous listener if exists
        if (outgoingMessagesListener != null) {
            outgoingMessagesListener.remove();
        }

        // Query messages between current user and other user
        outgoingMessagesListener = db.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", otherUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    messageList.add(message);
                                    break;
                                case MODIFIED:
                                    // Handle message updates if needed
                                    break;
                                case REMOVED:
                                    // Handle message removal if needed
                                    break;
                            }
                        }

                        // Also listen for messages from the other user to current user
                        loadIncomingMessages();
                    }
                });
    }

    private void loadIncomingMessages() {
        // Remove previous listener if exists
        if (incomingMessagesListener != null) {
            incomingMessagesListener.remove();
        }

        // Query messages from other user to current user
        incomingMessagesListener = db.collection("messages")
                .whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                messageList.add(message);
                                // Sort by timestamp to maintain order
                                messageList.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                                messageAdapter.notifyDataSetChanged();
                                recyclerMessages.scrollToPosition(messageList.size() - 1);
                                break;
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Create message object
        Message message = new Message(currentUserId, otherUserId, messageText, "text");

        // Save to Firestore
        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    // Clear the input field
                    editMessage.setText("");

                    // Optionally update the local list and scroll to bottom
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
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listeners to prevent memory leaks
        if (outgoingMessagesListener != null) {
            outgoingMessagesListener.remove();
        }
        if (incomingMessagesListener != null) {
            incomingMessagesListener.remove();
        }
    }
}