package com.example.telemedicine;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserId;
    private SimpleDateFormat dateFormat;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private TextView textTime;
        private LinearLayout layoutContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
            layoutContainer = itemView.findViewById(R.id.layout_container);
        }

        public void bind(Message message) {
            textMessage.setText(message.getMessageText());
            textTime.setText(dateFormat.format(message.getTimestamp()));

            // Set layout based on whether message is from current user
            if (message.getSenderId().equals(currentUserId)) {
                // Outgoing message
                layoutContainer.setGravity(Gravity.END);
                textMessage.setBackgroundResource(R.drawable.outgoing_message_bg);
                textMessage.setTextColor(itemView.getResources().getColor(android.R.color.white));
            } else {
                // Incoming message
                layoutContainer.setGravity(Gravity.START);
                textMessage.setBackgroundResource(R.drawable.incoming_message_bg);
                textMessage.setTextColor(itemView.getResources().getColor(android.R.color.black));
            }
        }
    }
}