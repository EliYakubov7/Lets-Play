package com.example.letsplay.objects;

import com.example.letsplay.date.DateTimeHelper;

import java.io.Serializable;

public class Comment implements Serializable {
    //The user which commented
    private String author;
    //The comment's body
    private String body;
    //The comment's time and date
    private String date,time;

    public Comment() {}

    public Comment(String author, String body) {
        this.author = author;
        this.body = body;
        setTime(DateTimeHelper.getTime());
        setDate(DateTimeHelper.getDate());
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
}
