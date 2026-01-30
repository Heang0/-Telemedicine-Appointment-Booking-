package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SecureMessagingHubFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private RecyclerView recyclerConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private EditText editSearchConversations;
    private FloatingActionButton fabNewMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secure_messaging_hub, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadSampleData();
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerConversations = view.findViewById(R.id.recycler_conversations);
        editSearchConversations = view.findViewById(R.id.edit_search_conversations);
        fabNewMessage = view.findViewById(R.id.fab_new_message);
    }

    private void setupRecyclerView() {
        recyclerConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        conversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations, this);
        recyclerConversations.setAdapter(conversationAdapter);
    }

    private void loadSampleData() {
        Calendar cal = Calendar.getInstance();

        // Sample conversations
        cal.add(Calendar.MINUTE, -5);
        conversations.add(new Conversation(
            "Dr. Sarah Smith",
            "How are you feeling after taking the medication?",
            cal.getTime(),
            1,
            true
        ));

        cal.add(Calendar.HOUR, -1);
        conversations.add(new Conversation(
            "Dr. Michael Johnson",
            "Your lab results are ready for review.",
            cal.getTime(),
            0,
            true
        ));

        cal.add(Calendar.HOUR, -3);
        conversations.add(new Conversation(
            "CVS Pharmacy",
            "Your prescription is ready for pickup.",
            cal.getTime(),
            2,
            true
        ));

        cal.add(Calendar.DAY_OF_MONTH, -1);
        conversations.add(new Conversation(
            "Dr. Emily Rodriguez",
            "Don't forget your appointment tomorrow at 10 AM.",
            cal.getTime(),
            0,
            true
        ));

        cal.add(Calendar.DAY_OF_MONTH, -2);
        conversations.add(new Conversation(
            "Dr. James Wilson",
            "Please fill out the attached questionnaire.",
            cal.getTime(),
            0,
            true
        ));

        conversationAdapter.notifyDataSetChanged();
    }

    private void setupEventListeners() {
        fabNewMessage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Starting new secure message...", Toast.LENGTH_SHORT).show();
        });

        editSearchConversations.setOnEditorActionListener((v, actionId, event) -> {
            String query = editSearchConversations.getText().toString().toLowerCase();
            filterConversations(query);
            return true;
        });
    }

    private void filterConversations(String query) {
        List<Conversation> filteredConversations = new ArrayList<>();
        for (Conversation conv : conversations) {
            if (conv.getParticipantName().toLowerCase().contains(query) ||
                conv.getLastMessage().toLowerCase().contains(query)) {
                filteredConversations.add(conv);
            }
        }
        conversationAdapter.updateConversations(filteredConversations);
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        Toast.makeText(getContext(), "Opening conversation with " + conversation.getParticipantName(), Toast.LENGTH_SHORT).show();
    }
}