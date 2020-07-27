package com.example.letsplay.objects;

import com.example.letsplay.date.DateTimeHelper;

import java.io.Serializable;

public class Message implements Serializable {
    // User Id
    private String sender;
    //The date and the time the message was created
    private String date,time;
    //The message body
    private String message;
    //The message seen or not
    private boolean seen;


    public Message() {}

    public Message(String sender, String message) {
        this.sender = sender;
        this.message = message;
        setTime(DateTimeHelper.getTime());
        setDate(DateTimeHelper.getDate());
    }

    public Message(String sender, String message, boolean seen) {
        this.sender = sender;
        this.date = date;
        this.time = time;
        this.message = message;
        this.seen = seen;
        setTime(DateTimeHelper.getTime());
        setDate(DateTimeHelper.getDate());
    }


    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
