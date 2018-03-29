package com.example.diksha.chatapplication;

import android.app.Application;
import android.support.annotation.NonNull;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.URISyntaxException;

/**
 * Created by diksha on 13/3/18.
 */

public class ChatApplication extends Application {

    private com.github.nkzawa.socketio.client.Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.1.7:3000");
        }catch (URISyntaxException e) {}
    }

    private FirebaseAuth mAuth;

    public FirebaseUser getCurrentUser(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            return currentUser;
        }
        return null;
    }


    public Socket getSocket(){
        return mSocket;
    }
}
