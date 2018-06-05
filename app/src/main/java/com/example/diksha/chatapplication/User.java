package com.example.diksha.chatapplication;

/**
 * Created by diksha on 15/3/18.
 */

public class User {
    private String mPhone;
    private String mUsername;

    public User(String phone, String username){
        mPhone = phone;
        mUsername = username;
    }
    public String getPhone(){
        return mPhone;
    }

    public String getUsername() {
        return mUsername;
    }
}
