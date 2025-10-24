package com.example.expense_tracker_app.notification;

import com.example.expense_tracker_app.type.NotificationType;

public class NotificationItem {
    private String id;
    private String title;
    private String message;
    private String time;
    private boolean isRead;
    private NotificationType iconType;

    public NotificationItem(String id, String title, String message, String time,
                            boolean isRead, NotificationType iconType) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.isRead = isRead;
        this.iconType = iconType;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isRead() { return isRead; }
    public NotificationType getIconType() { return iconType; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTime(String time) { this.time = time; }
    public void setRead(boolean read) { isRead = read; }
    public void setIconType(NotificationType iconType) { this.iconType = iconType; }
}