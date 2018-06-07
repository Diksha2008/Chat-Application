package com.example.diksha.chatapplication;

/**
 * Created by diksha on 15/3/18.
 */

public class User {
    private String mPhone;
    private String mUsername;
    private int mUserType;
    private int mUnreadMsg;
    private String mColor;

    public User(String phone, String username,String color, int unread, int userType) {
        mPhone = phone;
        mUsername=username;
        mUnreadMsg = unread;
        mColor = color;
        mUserType = userType;
    }

    public User(String phone, String username){
        mPhone = phone;
        mUsername = username;
    }

    public User(String phone, String username, int userType){
        mPhone = phone;
        mUsername = username;
        mUserType = userType;
    }
    public String getPhone(){
        return mPhone;
    }

    public String getUsername() {
        return mUsername;
    }

    public int getUserType() {
        return mUserType;
    }

    public int getUnreadMsg() {
        return mUnreadMsg;
    }
    public String getColor() {
        return mColor;
    }
}
