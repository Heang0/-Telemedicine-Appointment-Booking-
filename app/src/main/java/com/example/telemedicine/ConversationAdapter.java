package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
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
        private TextView securityIndicator;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            participantNameText = itemView.findViewById(R.id.participant_name);
            lastMessageText = itemView.findViewById(R.id.last_message);
            timeText = itemView.findViewById(R.id.message_time);
            unreadCountText = itemView.findViewById(R.id.unread_count);
            securityIndicator = itemView.findViewById(R.id.security_indicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onConversationClick(conversations.get(position));
                }
            });
        }

        public void bind(Conversation conversation) {
            participantNameText.setText(conversation.getParticipantName());
            lastMessageText.setText(conversation.getLastMessage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            timeText.setText(sdf.format(conversation.getLastMessageTime()));
            
            if (conversation.getUnreadCount() > 0) {
                unreadCountText.setText(String.valueOf(conversation.getUnreadCount()));
                unreadCountText.setVisibility(View.VISIBLE);
            } else {
                unreadCountText.setVisibility(View.GONE);
            }
            
            if (conversation.isEncrypted()) {
                securityIndicator.setText("🔒");
                securityIndicator.setContentDescription("Encrypted");
            } else {
                securityIndicator.setText("🔓");
                securityIndicator.setContentDescription("Not Encrypted");
            }
        }
    }
}