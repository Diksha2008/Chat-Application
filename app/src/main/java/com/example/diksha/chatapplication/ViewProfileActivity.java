package com.example.diksha.chatapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewProfileActivity extends AppCompatActivity {

    private Socket mSocket;

    private static final String TAG = "ViewProfileActivity";
    TextView phoneView;
    TextView nameView;
    TextView emailView;
    TextView addressView;
    TextView typeView;
    TextView timeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        phoneView = (TextView) findViewById(R.id.phone_business);
        nameView = (TextView) findViewById(R.id.name_business);
        emailView = (TextView) findViewById(R.id.email_business);
        addressView = (TextView) findViewById(R.id.address);
        typeView = (TextView) findViewById(R.id.business_type);
        timeView = (TextView) findViewById(R.id.business_hours);

        final ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();

        mSocket.on("send business details", BusinessDetails);
        mSocket.emit("get business details", ChatFragment.getToUser());

    }

    @Override
    protected void onDestroy() {
        mSocket.off("send business details", BusinessDetails);
        super.onDestroy();
    }

    Emitter.Listener BusinessDetails = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject result = (JSONObject) args[0];
                    try {
                        String phone = result.getString("phone");
                        String name = result.getString("name");
                        String email = result.getString("email");
                        String address = result.getString("address");
                        String type = result.getString("business_type");
                        String time = result.getString("delivery_time");
                        phoneView.setText(phone);
                        nameView.setText(name);
                        emailView.setText(email);
                        if(address.equals("null")) {
                            addressView.setText("not provided");
                            typeView.setText("not provided");
                            timeView.setText("not provided");
                        }
                        else {
                            addressView.setText(address);
                            typeView.setText(type);
                            timeView.setText(time);
                        }
                    }catch (JSONException e){
                        Log.e(TAG, "run: Could not parse",e );
                    }
                }
            });
        }
    };
}
