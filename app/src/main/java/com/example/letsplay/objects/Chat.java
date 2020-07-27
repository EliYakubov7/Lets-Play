package com.example.letsplay.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    //The User Im online with
    private String mySideUserKey;
    //The user Im chatting with
    private String otherSideUserKey;
    //The messages' list inside the chat window of both sides
    private ArrayList<Message> messages = new ArrayList<>();
    //Control the seen/not seen of the messages
    private boolean isLastMessageSeen = false;

    public Chat() {}

    public Chat(String mySideUserKey, String otherSideUserKey) {
        this.mySideUserKey = mySideUserKey;
        this.otherSideUserKey = otherSideUserKey;
    }

    public String getMySideUserKey() {
        return mySideUserKey;
    }

    public void setMySideUserKey(String mySideUserKey) {
        this.mySideUserKey = mySideUserKey;
    }

    public String getOtherSideUserKey() {
        return otherSideUserKey;
    }

    public void setOtherSideUserKey(String otherSideUserKey) {
        this.otherSideUserKey = otherSideUserKey;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public boolean isLastMessageSeen() {
        return isLastMessageSeen;
    }

    public void setLastMessageSeen(boolean lastMessageSeen) {
        isLastMessageSeen = lastMessageSeen;
    }
}
