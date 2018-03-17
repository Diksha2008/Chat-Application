package com.example.diksha.chatapplication;

import android.app.Application;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by diksha on 13/3/18.
 */

public class ChatApplication extends Application {
//    private Socket mSocket;
//    {
//        try {
//            mSocket = IO.socket("http://192.168.0.101:3000");
//        }catch (URISyntaxException e){
//            throw new RuntimeException(e);
//        }
//    }

    private com.github.nkzawa.socketio.client.Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.1.4:3000");
        }catch (URISyntaxException e) {}
    }

    public Socket getSocket(){
        return mSocket;
    }
}
