package com.example.telemedicine;

import java.util.Date;
import java.util.List;

public class Conversation {
    private String id;
    private String participant1;
    private String participant2;
    private String participantId; // The other participant
    private String participant1Name;
    private String participant2Name;
    private String participantName; // Display name
    private String lastMessage;
    private Date timestamp;
    private int unreadCount;
    private List<String> participants;
    private String participantRole;

    public Conversation() {
        this.unreadCount = 0;
    }

    public Conversation(String participant1, String participant2, String participant1Name, String participant2Name) {
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.participant1Name = participant1Name;
        this.participant2Name = participant2Name;
        this.unreadCount = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getParticipant1() { return participant1; }
    public String getParticipant2() { return participant2; }
    public String getParticipantId() { return participantId; }
    public String getParticipant1Name() { return participant1Name; }
    public String getParticipant2Name() { return participant2Name; }
    public String getParticipantName() { return participantName; }
    public String getLastMessage() { return lastMessage; }
    public Date getTimestamp() { return timestamp; }
    public int getUnreadCount() { return unreadCount; }
    public List<String> getParticipants() { return participants; }
    public String getParticipantRole() { return participantRole; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setParticipant1(String participant1) { this.participant1 = participant1; }
    public void setParticipant2(String participant2) { this.participant2 = participant2; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public void setParticipant1Name(String participant1Name) { this.participant1Name = participant1Name; }
    public void setParticipant2Name(String participant2Name) { this.participant2Name = participant2Name; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setParticipantRole(String participantRole) { this.participantRole = participantRole; }
}
