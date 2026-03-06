package com.example.telemedicine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecureMessagingHubFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private RecyclerView recyclerConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private List<Conversation> allConversations;
    private EditText editSearchConversations;
    private ImageView btnNewMessage;
    private Chip chipAll;
    private Chip chipUnread;
    private Chip chipDoctors;
    private Chip chipPatients;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserRole;
    private com.google.firebase.firestore.ListenerRegistration conversationsListener;
    private com.google.firebase.firestore.ListenerRegistration contactsListener;
    private final Map<String, Conversation> conversationMap = new HashMap<>();
    private final Map<String, Conversation> contactMap = new HashMap<>();
    private String activeFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secure_messaging_hub_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        initializeViews(view);
        setupRecyclerView();
        loadCurrentUserRole();
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerConversations = view.findViewById(R.id.recycler_conversations);
        editSearchConversations = view.findViewById(R.id.edit_search_conversations);
        btnNewMessage = view.findViewById(R.id.btn_new_message);
        chipAll = view.findViewById(R.id.chip_all);
        chipUnread = view.findViewById(R.id.chip_unread);
        chipDoctors = view.findViewById(R.id.chip_doctors);
        chipPatients = view.findViewById(R.id.chip_patients);
    }

    private void setupRecyclerView() {
        recyclerConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        conversations = new ArrayList<>();
        allConversations = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversations, this);
        recyclerConversations.setAdapter(conversationAdapter);
    }

    private void loadCurrentUserRole() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User currentUser = documentSnapshot.toObject(User.class);
                    currentUserRole = currentUser != null ? currentUser.getRole() : UserRole.PATIENT.getRoleName();
                    loadConversations();
                    loadAppointmentContacts();
                })
                .addOnFailureListener(e -> {
                    currentUserRole = UserRole.PATIENT.getRoleName();
                    loadConversations();
                    loadAppointmentContacts();
                });
    }

    private void loadConversations() {
        if (currentUserId == null) {
            return;
        }

        if (conversationsListener != null) {
            conversationsListener.remove();
        }

        conversationsListener = db.collection("conversations")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) {
                        return;
                    }

                    conversationMap.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Conversation conversation = document.toObject(Conversation.class);
                        conversation.setId(document.getId());
                        populateConversationParticipant(conversation);
                        if (conversation.getParticipantId() != null) {
                            conversationMap.put(conversation.getParticipantId(), conversation);
                        }
                    }
                    rebuildConversationList();
                });
    }

    private void loadAppointmentContacts() {
        if (currentUserId == null) {
            return;
        }

        if (contactsListener != null) {
            contactsListener.remove();
        }

        String field = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole) ? "doctorId" : "patientId";
        contactsListener = db.collection("appointments")
                .whereEqualTo(field, currentUserId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) {
                        return;
                    }

                    contactMap.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Appointment appointment = document.toObject(Appointment.class);
                        Conversation contactConversation = buildConversationFromAppointment(appointment);
                        if (contactConversation != null && contactConversation.getParticipantId() != null) {
                            contactMap.put(contactConversation.getParticipantId(), contactConversation);
                        }
                    }
                    rebuildConversationList();
                });
    }

    private Conversation buildConversationFromAppointment(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        String otherParticipantId;
        String otherParticipantName;
        String otherParticipantRole;

        if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)) {
            otherParticipantId = appointment.getPatientId();
            otherParticipantName = appointment.getPatientName();
            otherParticipantRole = UserRole.PATIENT.getRoleName();
        } else {
            otherParticipantId = appointment.getDoctorId();
            otherParticipantName = appointment.getDoctorName();
            otherParticipantRole = UserRole.DOCTOR.getRoleName();
        }

        if (otherParticipantId == null || otherParticipantId.trim().isEmpty()) {
            return null;
        }

        Conversation conversation = new Conversation();
        conversation.setParticipantId(otherParticipantId);
        conversation.setParticipantName(otherParticipantName != null && !otherParticipantName.trim().isEmpty()
                ? otherParticipantName
                : "User");
        conversation.setParticipantRole(otherParticipantRole);
        conversation.setLastMessage("Start a conversation");
        conversation.setTimestamp(appointment.getAppointmentDate());
        conversation.setUnreadCount(0);
        return conversation;
    }

    private void populateConversationParticipant(Conversation conversation) {
        if (conversation.getParticipant1() != null && conversation.getParticipant1().equals(currentUserId)) {
            conversation.setParticipantId(conversation.getParticipant2());
            conversation.setParticipantName(conversation.getParticipant2Name() != null ? conversation.getParticipant2Name() : "User");
        } else {
            conversation.setParticipantId(conversation.getParticipant1());
            conversation.setParticipantName(conversation.getParticipant1Name() != null ? conversation.getParticipant1Name() : "User");
        }

        if (conversation.getParticipantRole() == null || conversation.getParticipantRole().trim().isEmpty()) {
            if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)) {
                conversation.setParticipantRole(UserRole.PATIENT.getRoleName());
            } else {
                conversation.setParticipantRole(UserRole.DOCTOR.getRoleName());
            }
        }
    }

    private void rebuildConversationList() {
        Map<String, Conversation> merged = new HashMap<>(contactMap);
        for (Map.Entry<String, Conversation> entry : conversationMap.entrySet()) {
            Conversation existing = merged.get(entry.getKey());
            Conversation actual = entry.getValue();
            if (existing != null && (actual.getParticipantRole() == null || actual.getParticipantRole().trim().isEmpty())) {
                actual.setParticipantRole(existing.getParticipantRole());
            }
            merged.put(entry.getKey(), actual);
        }

        allConversations = new ArrayList<>(merged.values());
        Collections.sort(allConversations, Comparator.comparing(
                Conversation::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        applyFilters();
    }

    private void setupEventListeners() {
        editSearchConversations.setOnEditorActionListener((v, actionId, event) -> {
            applyFilters();
            return true;
        });

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> setActiveFilter("all"));
        }
        if (chipUnread != null) {
            chipUnread.setOnClickListener(v -> setActiveFilter("unread"));
        }
        if (chipDoctors != null) {
            chipDoctors.setOnClickListener(v -> setActiveFilter("doctors"));
        }
        if (chipPatients != null) {
            chipPatients.setOnClickListener(v -> setActiveFilter("patients"));
        }

        if (btnNewMessage != null) {
            btnNewMessage.setOnClickListener(v -> openNewMessagePicker());
        }
    }

    private void setActiveFilter(String filter) {
        activeFilter = filter;
        updateFilterChips();
        applyFilters();
    }

    private void updateFilterChips() {
        if (chipAll != null) {
            chipAll.setChecked("all".equals(activeFilter));
        }
        if (chipUnread != null) {
            chipUnread.setChecked("unread".equals(activeFilter));
        }
        if (chipDoctors != null) {
            chipDoctors.setChecked("doctors".equals(activeFilter));
        }
        if (chipPatients != null) {
            chipPatients.setChecked("patients".equals(activeFilter));
        }
    }

    private void applyFilters() {
        List<Conversation> filteredConversations = new ArrayList<>();
        String query = editSearchConversations != null
                ? editSearchConversations.getText().toString().trim().toLowerCase()
                : "";

        for (Conversation conv : allConversations) {
            String participantName = conv.getParticipantName() != null ? conv.getParticipantName().toLowerCase() : "";
            String lastMessage = conv.getLastMessage() != null ? conv.getLastMessage().toLowerCase() : "";
            boolean matchesSearch = TextUtils.isEmpty(query)
                    || participantName.contains(query)
                    || lastMessage.contains(query);
            boolean matchesType = matchesFilter(conv);

            if (matchesSearch && matchesType) {
                filteredConversations.add(conv);
            }
        }

        conversations = filteredConversations;
        conversationAdapter.updateConversations(filteredConversations);
    }

    private boolean matchesFilter(Conversation conversation) {
        if ("unread".equals(activeFilter)) {
            return conversation.getUnreadCount() > 0;
        }
        if ("doctors".equals(activeFilter)) {
            return UserRole.DOCTOR.getRoleName().equalsIgnoreCase(conversation.getParticipantRole());
        }
        if ("patients".equals(activeFilter)) {
            return UserRole.PATIENT.getRoleName().equalsIgnoreCase(conversation.getParticipantRole());
        }
        return true;
    }

    private void openNewMessagePicker() {
        if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)) {
            openPatientPicker();
        } else {
            openDoctorPicker();
        }
    }

    private void openDoctorPicker() {
        db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Conversation> doctorChoices = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        User doctor = document.toObject(User.class);
                        if (!doctor.isVerified()) {
                            continue;
                        }
                        if (doctor.getUserId() == null || doctor.getUserId().trim().isEmpty()) {
                            continue;
                        }

                        Conversation conversation = new Conversation();
                        conversation.setParticipantId(doctor.getUserId());
                        conversation.setParticipantName(doctor.getFullName() != null ? doctor.getFullName() : "Doctor");
                        conversation.setParticipantRole(UserRole.DOCTOR.getRoleName());
                        conversation.setLastMessage("Start a conversation");
                        doctorChoices.add(conversation);
                    }
                    showNewMessageDialog("Choose doctor", doctorChoices, "No verified doctors found.");
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load doctors", Toast.LENGTH_SHORT).show());
    }

    private void openPatientPicker() {
        List<Conversation> patientChoices = new ArrayList<>();
        for (Conversation conversation : allConversations) {
            if (UserRole.PATIENT.getRoleName().equalsIgnoreCase(conversation.getParticipantRole())) {
                patientChoices.add(conversation);
            }
        }

        showNewMessageDialog("Choose patient", patientChoices, "No patients found. Create an appointment first.");
    }

    private void showNewMessageDialog(String title, List<Conversation> choices, String emptyMessage) {
        if (choices.isEmpty()) {
            Toast.makeText(getContext(), emptyMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[choices.size()];
        for (int i = 0; i < choices.size(); i++) {
            names[i] = choices.get(i).getParticipantName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(names, (dialog, which) -> onConversationClick(choices.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        // Open message thread with this conversation
        MessageThreadFragment fragment = MessageThreadFragment.newInstance(
            conversation.getParticipantId(),
            conversation.getParticipantName()
        );
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (conversationsListener != null) {
            conversationsListener.remove();
        }
        if (contactsListener != null) {
            contactsListener.remove();
        }
    }
}
