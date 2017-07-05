package com.farhanapps.HitChat.Models;

/**
 * Created by farhan on 17-04-2016.
 */
public class MessageModel {
    String id,sender,receiver,time,message;
    int type;

    public MessageModel(String id, String sender, String receiver, String time, String message, int type) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.message = message;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(int type) {
        this.type = type;
    }
}
