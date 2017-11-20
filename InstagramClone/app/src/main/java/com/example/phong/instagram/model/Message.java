package com.example.phong.instagram.model;

/**
 * Created by phong on 11/5/2017.
 */

public class Message {
    private String id_mess;
    private String user_sent;
    private String message;
    private String date;

    public Message() {
    }

    public Message(String key_room, String user_sent, String message, String date) {
        this.id_mess = key_room;
        this.user_sent = user_sent;
        this.message = message;
        this.date = date;
    }

    public String getKey_room() {
        return id_mess;
    }

    public void setKey_room(String key_room) {
        this.id_mess = key_room;
    }

    public String getUser_sent() {
        return user_sent;
    }

    public void setUser_sent(String user_sent) {
        this.user_sent = user_sent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
