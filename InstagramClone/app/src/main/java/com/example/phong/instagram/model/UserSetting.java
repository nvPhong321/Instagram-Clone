package com.example.phong.instagram.model;

/**
 * Created by phong on 8/26/2017.
 */

public class UserSetting {
    private User user;
    private UserAccountSetting userAccountSetting;

    public UserSetting(){

    }

    public UserSetting(User user, UserAccountSetting userAccountSetting) {
        this.user = user;
        this.userAccountSetting = userAccountSetting;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAccountSetting getUserAccountSetting() {
        return userAccountSetting;
    }

    public void setUserAccountSetting(UserAccountSetting userAccountSetting) {
        this.userAccountSetting = userAccountSetting;
    }

    @Override
    public String toString() {
        return "UserSetting{" +
                "user=" + user +
                ", userAccountSetting=" + userAccountSetting +
                '}';
    }
}
