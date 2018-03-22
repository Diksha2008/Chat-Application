package com.example.diksha.chatapplication;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * A message entity
 */

public class Message {
    private String mMessage;
    private String mUsername;
    private Uri mImage;

    public Message(String message, Uri image){
        mMessage = message;
        mImage = image;
    }

    public String getMessage(){
        return mMessage;
    }

    public String getUsername(){
        return  mUsername;
    }

    public Uri getmImage() { return mImage; }
}
