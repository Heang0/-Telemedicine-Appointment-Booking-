package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoConsultationFragment extends Fragment {

    private ImageButton btnToggleMic;
    private ImageButton btnEndCall;
    private ImageButton btnToggleCamera;
    private ImageButton btnChat;
    private LinearLayout chatPanel;
    private LinearLayout chatInputLayout;
    private RecyclerView recyclerChatMessages;
    private EditText editChatMessage;
    private Button btnSendMessage;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_consultation, container, false);

        initializeViews(view);
        setupChat();
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        btnToggleMic = view.findViewById(R.id.btn_toggle_mic);
        btnEndCall = view.findViewById(R.id.btn_end_call);
        btnToggleCamera = view.findViewById(R.id.btn_toggle_camera);
        btnChat = view.findViewById(R.id.btn_chat);
        chatPanel = view.findViewById(R.id.chat_panel);
        chatInputLayout = view.findViewById(R.id.chat_input_layout);
        recyclerChatMessages = view.findViewById(R.id.recycler_chat_messages);
        editChatMessage = view.findViewById(R.id.edit_chat_message);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
    }

    private void setupChat() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(chatMessages);
        recyclerChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChatMessages.setAdapter(chatAdapter);

        // Add a welcome message
        chatMessages.add(new ChatMessage("Hello! Welcome to your consultation with Dr. Smith.", "doctor"));
        chatAdapter.notifyDataSetChanged();
    }

    private void setupEventListeners() {
        btnToggleMic.setOnClickListener(v -> {
            // Toggle microphone state
            boolean isMuted = (Integer) btnToggleMic.getTag() != null && (Boolean) btnToggleMic.getTag();
            if (isMuted) {
                btnToggleMic.setImageResource(R.drawable.ic_mic_on);
                btnToggleMic.setTag(false);
                Toast.makeText(getContext(), "Microphone unmuted", Toast.LENGTH_SHORT).show();
            } else {
                btnToggleMic.setImageResource(R.drawable.ic_mic_off);
                btnToggleMic.setTag(true);
                Toast.makeText(getContext(), "Microphone muted", Toast.LENGTH_SHORT).show();
            }
        });

        btnToggleCamera.setOnClickListener(v -> {
            // Toggle camera state
            boolean isCameraOff = (Integer) btnToggleCamera.getTag() != null && (Boolean) btnToggleCamera.getTag();
            if (isCameraOff) {
                btnToggleCamera.setImageResource(R.drawable.ic_camera_on);
                btnToggleCamera.setTag(false);
                Toast.makeText(getContext(), "Camera on", Toast.LENGTH_SHORT).show();
            } else {
                btnToggleCamera.setImageResource(R.drawable.ic_camera_off);
                btnToggleCamera.setTag(true);
                Toast.makeText(getContext(), "Camera off", Toast.LENGTH_SHORT).show();
            }
        });

        btnEndCall.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ending consultation...", Toast.LENGTH_SHORT).show();
            // In a real app, this would end the video call
        });

        btnChat.setOnClickListener(v -> {
            // Toggle chat panel visibility
            if (chatPanel.getVisibility() == View.GONE) {
                chatPanel.setVisibility(View.VISIBLE);
                chatInputLayout.setVisibility(View.VISIBLE);
            } else {
                chatPanel.setVisibility(View.GONE);
                chatInputLayout.setVisibility(View.GONE);
            }
        });

        btnSendMessage.setOnClickListener(v -> sendMessage());

        // Allow sending message with Enter key
        editChatMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = editChatMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            ChatMessage chatMessage = new ChatMessage(message, "patient");
            chatAdapter.addMessage(chatMessage);
            editChatMessage.setText("");

            // Simulate doctor's response after a delay
            simulateDoctorResponse();
        }
    }

    private void simulateDoctorResponse() {
        // In a real app, this would come from the doctor
        // For simulation, we'll add a delayed response
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait 2 seconds
                requireActivity().runOnUiThread(() -> {
                    ChatMessage response = new ChatMessage("Thank you for sharing that information. How long have you been experiencing these symptoms?", "doctor");
                    chatAdapter.addMessage(response);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}