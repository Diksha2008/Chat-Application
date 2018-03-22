package com.example.diksha.chatapplication;

import android.app.Application;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by diksha on 13/3/18.
 */

public class ChatApplication extends Application {

    private com.github.nkzawa.socketio.client.Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://172.16.60.32:3000");
        }catch (URISyntaxException e) {}
    }

    public Socket getSocket(){
        return mSocket;
    }
}
