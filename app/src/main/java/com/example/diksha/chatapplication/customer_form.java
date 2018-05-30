package com.example.diksha.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

public class customer_form extends AppCompatActivity {

    private com.github.nkzawa.socketio.client.Socket mSocket;
    private ChatApplication app;
    EditText ed1;
    Button b1;

    private static final String TAG = "customer_form";
    private static  final int CUSTOMER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_form);

        app = (ChatApplication) customer_form.this.getApplication();
        mSocket = app.getSocket();

        ed1 = (EditText) findViewById(R.id.name_customer);
        b1 = (Button) findViewById(R.id.add_details_button_customer);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
//                String id = ref.push().getKey();
                String name = ed1.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    ed1.setError(getString(R.string.invalid_name));
                    return;
                }

                JSONObject userData = new JSONObject();
                try {
                    userData.put("userType", CUSTOMER);
                    userData.put("phone", app.getCurrentUser().getPhoneNumber());
                    userData.put("name", name);
                } catch (JSONException e) {
                    Log.e(TAG, "onCreate: " + e);
                }


                mSocket.emit("update user details", userData);

                Toast.makeText(getApplicationContext(), "added", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(customer_form.this, MainActivity.class));
                finish();

            }
        });

    }

}
