package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageThreadFragment extends Fragment {

    private static final String ARG_OTHER_USER_ID = "other_user_id";
    private static final String ARG_OTHER_USER_NAME = "other_user_name";

    private RecyclerView recyclerMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private EditText editMessage;
    private View btnSend;
    private TextView textEmptyState;

    private String otherUserId;
    private String otherUserName;
    private String currentUserId;
    private String currentUserName;
    private String conversationId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration messageListener;

    public static MessageThreadFragment newInstance(String otherUserId, String otherUserName) {
        MessageThreadFragment fragment = new MessageThreadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OTHER_USER_ID, otherUserId);
        args.putString(ARG_OTHER_USER_NAME, otherUserName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            otherUserId = getArguments().getString(ARG_OTHER_USER_ID);
            otherUserName = getArguments().getString(ARG_OTHER_USER_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_message_thread, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        currentUserName = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : null;

        initializeViews(view);
        setupRecyclerView();
        generateConversationId();
        loadCurrentUserProfile();
        loadMessages();
        setupSendButton();

        return view;
    }

    private void initializeViews(View view) {
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        editMessage = view.findViewById(R.id.edit_message);
        btnSend = view.findViewById(R.id.btn_send);
        textEmptyState = view.findViewById(R.id.text_empty_state);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(otherUserName);
            toolbar.setSubtitle("Secure direct message");
            if (toolbar.getNavigationIcon() != null) {
                android.graphics.drawable.Drawable navigationIcon = DrawableCompat.wrap(toolbar.getNavigationIcon().mutate());
                DrawableCompat.setTint(navigationIcon, ContextCompat.getColor(requireContext(), R.color.ios_text_primary));
                toolbar.setNavigationIcon(navigationIcon);
            }
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void generateConversationId() {
        // Create a unique conversation ID from both user IDs
        if (currentUserId != null && otherUserId != null) {
            conversationId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;
        }
    }

    private void loadCurrentUserProfile() {
        if (currentUserId == null) {
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null && currentUser.getFullName() != null && !currentUser.getFullName().trim().isEmpty()) {
                        currentUserName = currentUser.getFullName().trim();
                    }
                });
    }

    private void loadMessages() {
        if (conversationId == null) {
            return;
        }

        messageListener = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        messages.clear();
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message message = dc.getDocument().toObject(Message.class);
                                messages.add(message);
                            }
                        }
                        
                        if (messages.isEmpty()) {
                            textEmptyState.setVisibility(View.VISIBLE);
                            recyclerMessages.setVisibility(View.GONE);
                        } else {
                            textEmptyState.setVisibility(View.GONE);
                            recyclerMessages.setVisibility(View.VISIBLE);
                            messageAdapter.notifyDataSetChanged();
                            recyclerMessages.scrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    private void setupSendButton() {
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create message object
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", currentUserId);
        messageData.put("receiverId", otherUserId);
        messageData.put("message", messageText);
        messageData.put("timestamp", new Date());
        messageData.put("isRead", false);

        // Ensure the parent conversation exists before writing the first message.
        Map<String, Object> conversationData = buildConversationMetadata(messageText);

        db.collection("conversations")
                .document(conversationId)
                .set(conversationData)
                .addOnSuccessListener(aVoid -> {
                    db.collection("conversations")
                            .document(conversationId)
                            .collection("messages")
                            .add(messageData)
                            .addOnSuccessListener(documentReference -> {
                                editMessage.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to start conversation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> buildConversationMetadata(String lastMessage) {
        List<String> participants = buildSortedParticipants();
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("participant1", currentUserId);
        conversationData.put("participant2", otherUserId);
        conversationData.put("participant1Name", currentUserName != null ? currentUserName : "User");
        conversationData.put("participant2Name", otherUserName != null ? otherUserName : "User");
        conversationData.put("lastMessage", lastMessage);
        conversationData.put("timestamp", new Date());
        conversationData.put("participants", participants);
        return conversationData;
    }

    private List<String> buildSortedParticipants() {
        List<String> participants = new ArrayList<>(Arrays.asList(currentUserId, otherUserId));
        participants.removeIf(participant -> participant == null || participant.trim().isEmpty());
        Collections.sort(participants);
        return participants;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}
