package com.example.diksha.chatapplication;

/**
 * Created by diksha on 15/3/18.
 */

public class User {
    private String mUserId;
    private String mColor;
    private String mUnreadMsg;
    public User(String userId , String color , String unread){
        mUserId = userId;
        mUnreadMsg = unread;
        mColor = color;

    }
//    public void setUnreadMsg(int x)
//    {
//        mUnreadMsg=x;
//    }
    public String getUnreadMsg(){
        return mUnreadMsg;
    }
    public String getColor()
    {
        return mColor;
    }
    public String getUserId(){
        return mUserId;
    }

}
