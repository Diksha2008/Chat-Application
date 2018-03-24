package com.example.diksha.chatapplication;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * A message entity
 */

public class Message {
    private String mMessage;
    private String mTo;
    private Uri mImage;

    public Message(String message, Uri image){
        mMessage = message;
        mImage = image;
    }

    public String getMessage(){
        return mMessage;
    }

    public String getTo(){
        return  mTo;
    }

    public Uri getmImage() { return mImage; }
}
