package com.example.letsplay.objects;

import androidx.annotation.Nullable;

import com.example.letsplay.date.DateTimeHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Feed implements Serializable {
    //Feed's author (first and last name)
    private String author;
    //Date and time of the feed's creation
    private String date,time;
    //Feed's title and body
    private String body;
    //Number of likes and comments
    private int likes=0,noOfComments=0;


    //List of comments
    ArrayList<Comment> comments = new ArrayList<>();
    //Hash Map of likes control
    HashMap<String,Boolean> likesControl = new HashMap<>();

    public Feed() {}

    public Feed(String author, String body) {
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getNoOfComments() {
        return noOfComments;
    }

    public void setNoOfComments(int noOfComments) {
        this.noOfComments = noOfComments;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HashMap<String, Boolean> getLikesControl() {
        return likesControl;
    }

    public void setLikesControl(HashMap<String, Boolean> likesControl) {
        this.likesControl = likesControl;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Feed feed = (Feed)obj;
        return (getAuthor().equals(feed.getAuthor())&&getBody().equals(feed.getBody())&&
                getTime().equals(feed.getTime())&&getDate().equals(feed.getDate())&&
                getLikes()==feed.getLikes()&&getNoOfComments()==feed.getNoOfComments());
    }
}
