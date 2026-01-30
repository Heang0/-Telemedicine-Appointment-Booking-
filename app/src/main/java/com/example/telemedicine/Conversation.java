package com.example.telemedicine;

import java.util.Date;

public class Conversation {
    private String participantName;
    private String lastMessage;
    private Date lastMessageTime;
    private int unreadCount;
    private boolean isEncrypted;

    public Conversation(String participantName, String lastMessage, Date lastMessageTime, int unreadCount, boolean isEncrypted) {
        this.participantName = participantName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.isEncrypted = isEncrypted;
    }

    // Getters
    public String getParticipantName() { return participantName; }
    public String getLastMessage() { return lastMessage; }
    public Date getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isEncrypted() { return isEncrypted; }

    // Setters
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void setEncrypted(boolean encrypted) { isEncrypted = encrypted; }
}