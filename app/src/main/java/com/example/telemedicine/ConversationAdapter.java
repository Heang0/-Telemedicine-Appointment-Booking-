package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_ios, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView participantNameText;
        private TextView lastMessageText;
        private TextView timeText;
        private TextView unreadCountText;
        private TextView roleBadgeText;
        private View onlineStatusView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            participantNameText = itemView.findViewById(R.id.text_sender_name);
            lastMessageText = itemView.findViewById(R.id.text_last_message);
            timeText = itemView.findViewById(R.id.text_message_time);
            unreadCountText = itemView.findViewById(R.id.badge_unread);
            roleBadgeText = itemView.findViewById(R.id.text_role_badge);
            onlineStatusView = itemView.findViewById(R.id.view_online_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onConversationClick(conversations.get(position));
                }
            });
        }

        public void bind(Conversation conversation) {
            String participantName = conversation.getParticipantName() != null
                    ? conversation.getParticipantName()
                    : "User";
            String lastMessage = conversation.getLastMessage() != null
                    ? conversation.getLastMessage()
                    : "Start a conversation";

            participantNameText.setText(participantName);
            lastMessageText.setText(lastMessage);

            String role = conversation.getParticipantRole() != null ? conversation.getParticipantRole() : "";
            if (roleBadgeText != null) {
                if (!role.trim().isEmpty()) {
                    roleBadgeText.setText(role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase(Locale.getDefault()));
                    roleBadgeText.setVisibility(View.VISIBLE);
                } else {
                    roleBadgeText.setVisibility(View.GONE);
                }
            }
            if (onlineStatusView != null) {
                onlineStatusView.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            if (conversation.getTimestamp() != null) {
                timeText.setText(sdf.format(conversation.getTimestamp()));
            } else {
                timeText.setText("");
            }

            if (conversation.getUnreadCount() > 0) {
                unreadCountText.setText(String.valueOf(conversation.getUnreadCount()));
                unreadCountText.setVisibility(View.VISIBLE);
            } else {
                unreadCountText.setVisibility(View.GONE);
            }
        }
    }
}
