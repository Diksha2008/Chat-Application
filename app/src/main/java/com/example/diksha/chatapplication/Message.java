package com.example.diksha.chatapplication;

import android.graphics.Bitmap;

/**
 * A message entity
 */

public class Message {
    private String mMessage;
    private String mUsername;
    private Bitmap mImage;

    public Message(String message, Bitmap image){
        mMessage = message;
        mImage = image;
    }

    public String getMessage(){
        return mMessage;
    }

    public String getUsername(){
        return  mUsername;
    }

    public Bitmap getmImage() { return mImage; }
}
