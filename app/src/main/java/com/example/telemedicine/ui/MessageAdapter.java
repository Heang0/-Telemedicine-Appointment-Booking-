package com.example.telemedicine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.telemedicine.R;
import com.example.telemedicine.model.ChatMessage;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    private Context context;
    private List<ChatMessage> messages;

    public MessageAdapter(Context context, List<ChatMessage> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        ChatMessage message = messages.get(position);
        
        TextView senderTextView = view.findViewById(R.id.textViewSender);
        TextView messageTextView = view.findViewById(R.id.textViewMessage);
        TextView timeTextView = view.findViewById(R.id.textViewTime);
        TextView typeTextView = view.findViewById(R.id.textViewMessageType);

        senderTextView.setText(message.getSender().toUpperCase());
        messageTextView.setText(message.getMessage());
        timeTextView.setText("Sent: " + message.getTimestamp().toString());
        typeTextView.setText(message.getMessageType());

        // Different styling based on sender
        if ("patient".equals(message.getSender())) {
            senderTextView.setTextColor(context.getResources().getColor(R.color.blue_500));
        } else {
            senderTextView.setTextColor(context.getResources().getColor(R.color.green_500));
        }

        return view;
    }
}