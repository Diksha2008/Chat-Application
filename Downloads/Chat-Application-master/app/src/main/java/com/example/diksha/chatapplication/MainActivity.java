package com.example.diksha.chatapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseUser;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {
    String answer;
    private com.github.nkzawa.socketio.client.Socket mSocket;
    private FirebaseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication app = (ChatApplication) MainActivity.this.getApplication();
        mCurrentUser=app.getCurrentUser();
        mSocket = app.getSocket();
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            setContentView(R.layout.activity_main);
            android.support.v4.app.Fragment userFragment = new UserListFragment();
            android.support.v4.app.FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.chat, userFragment);
            transaction.commit();
//          mSocket.connect();
//            mSocket.on(Socket.EVENT_CONNECT,OnConnect);
           // mSocket.on("update online status",onStatusUpdate);
        }
        else
        {
           buildDialog(MainActivity.this).show();}

    }
    public AlertDialog.Builder buildDialog(Context c)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
        alertDialog=alertDialog.setTitle("No Internet Connection");
        alertDialog.setMessage("Mobile Data is Turned Off.Go to Settings to Turn It on");
        alertDialog.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent();
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.show();
        return alertDialog;
    }
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
    public void setActionBarSubTitle(String toDisplay ) {
        getSupportActionBar().setSubtitle(toDisplay);

    }
}