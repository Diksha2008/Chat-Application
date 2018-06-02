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
    private Boolean misDelivered;
    private String mCreatedAt;

    public Message(String message, Uri image, Boolean isReceived,String createdAt,Boolean isDelivered){
        mMessage = message;
        mImage = image;
        mIsReceived = isReceived;
        mCreatedAt = createdAt;
        misDelivered =isDelivered;
    }

    public String getMessage(){
        return mMessage;
    }
    public boolean getIsDelivered()
    {
        return misDelivered;
    }
    public Uri getmImage() { return mImage; }

    public Boolean isReceived() { return mIsReceived;}

    public String createdAt() { return  mCreatedAt; }


}