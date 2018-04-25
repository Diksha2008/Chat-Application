package com.example.diksha.chatapplication;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

public class CategoryActivity extends AppCompatActivity {

    private ChatApplication app;
    private com.github.nkzawa.socketio.client.Socket mSocket;

    private JSONObject userData;

    private static final String TAG = "CategoryActivity";

    private static  final int BUSINESS = 1;
    private static  final int CUSTOMER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Button business = findViewById(R.id.business);
        Button customer = findViewById(R.id.customer);

        app = (ChatApplication) CategoryActivity.this.getApplication();
        mSocket = app.getSocket();

        userData = new JSONObject();
        try {
            userData.put("uid", app.getCurrentUser().getUid());
            userData.put("phone", app.getCurrentUser().getPhoneNumber());
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e);
        }

        business.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    userData.put("userType", BUSINESS);
                } catch (JSONException e) {
                    Log.e(TAG, "onClick: " + e);
                }
                mSocket.emit("insert user to db", userData);
                Intent intent = new Intent (getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    userData.put("userType", CUSTOMER);
                } catch (JSONException e) {
                    Log.e(TAG, "onClick: " + e);
                }
                mSocket.emit("insert user to db", userData);
                Intent intent = new Intent (getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
