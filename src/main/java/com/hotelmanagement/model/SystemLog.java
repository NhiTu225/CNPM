package com.hotelmanagement.model;

import java.time.LocalDateTime;

public class SystemLog {
    private int id;
    private String userEmail;
    private String action;
    private LocalDateTime timestamp;

    public SystemLog() {
    }

    public SystemLog(int id, String userEmail, String action, LocalDateTime timestamp) {
        this.id = id;
        this.userEmail = userEmail;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
