package com.example.diksha.chatapplication;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * A message entity
 */

public class Message {
    private String mMessage;
    private Boolean mIsReceived;
    private Uri mImage;
    private String mCreatedAt;

    public Message(String message, Uri image, Boolean isReceived, String createdAt){
        mMessage = message;
        mImage = image;
        mIsReceived = isReceived;
        mCreatedAt = createdAt;
    }

    public String getMessage(){
        return mMessage;
    }

    public Uri getmImage() { return mImage; }

    public Boolean isReceived() { return mIsReceived;}

    public String createdAt() { return  mCreatedAt; }


}
