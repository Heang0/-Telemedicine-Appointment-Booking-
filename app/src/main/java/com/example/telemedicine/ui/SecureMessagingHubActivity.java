package com.example.telemedicine.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telemedicine.R;
import com.example.telemedicine.model.ChatMessage;
import com.example.telemedicine.model.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SecureMessagingHubActivity extends AppCompatActivity {
    private ListView messagesListView;
    private EditText messageEditText;
    private Button sendButton;
    private TextView appointmentInfoTextView;
    private FirebaseFirestore db;
    private String appointmentId;
    private String userId;
    private String userRole; // "patient" or "doctor"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_messaging_hub);

        messagesListView = findViewById(R.id.listViewMessages);
        messageEditText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSendMessage);
        appointmentInfoTextView = findViewById(R.id.textViewAppointmentInfo);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentId = getIntent().getStringExtra("appointment_id");
        userRole = getIntent().getStringExtra("user_role");

        if (appointmentId != null) {
            appointmentInfoTextView.setText("Consultation: " + appointmentId);
        }

        // Load existing messages
        loadMessages();

        // Set up send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadMessages() {
        db.collection("chat_messages")
                .whereEqualTo("appointmentId", appointmentId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(SecureMessagingHubActivity.this, "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ChatMessage> messages = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot) {
                            ChatMessage message = document.toObject(ChatMessage.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                    }

                    // Update UI with messages
                    MessageAdapter adapter = new MessageAdapter(SecureMessagingHubActivity.this, messages);
                    messagesListView.setAdapter(adapter);
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create chat message
        ChatMessage message = new ChatMessage(messageText, userRole, appointmentId);

        // Save to Firestore
        db.collection("chat_messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageEditText.setText("");
                    Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}