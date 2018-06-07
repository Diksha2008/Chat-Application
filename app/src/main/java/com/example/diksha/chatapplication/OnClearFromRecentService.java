package com.example.diksha.chatapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.nkzawa.socketio.client.Socket;

/**
 * Created by diksha on 7/6/18.
 */

public class OnClearFromRecentService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        ChatApplication app = (ChatApplication) getApplication();
        Socket mSocket = app.getSocket();
        String currentUser = app.getCurrentUser().getPhoneNumber();
        mSocket.emit("offline", currentUser);
        stopSelf();
    }
}
