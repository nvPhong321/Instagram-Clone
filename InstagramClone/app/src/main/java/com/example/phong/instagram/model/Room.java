package com.example.phong.instagram.model;

/**
 * Created by phong on 11/5/2017.
 */

public class Room {
    private String key_room;
    private String user_id;

    public Room(){

    }

    public Room(String key_room, String user_id) {
        this.key_room = key_room;
        this.user_id = user_id;
    }

    public String getKey_room() {
        return key_room;
    }

    public void setKey_room(String key_room) {
        this.key_room = key_room;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
