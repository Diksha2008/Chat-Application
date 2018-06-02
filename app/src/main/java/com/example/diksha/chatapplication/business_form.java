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

public class business_form extends AppCompatActivity {

    private com.github.nkzawa.socketio.client.Socket mSocket;
    private ChatApplication app;
    EditText ed1, ed2;
    Button b1;

    private static final String TAG = "business_form";
    private static  final int BUSINESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_form);

        app = (ChatApplication) business_form.this.getApplication();
        mSocket = app.getSocket();

        ed1 = (EditText) findViewById(R.id.name_business);
        ed2 = (EditText) findViewById(R.id.email_business);
        b1 = (Button) findViewById(R.id.add_details_button_business);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                String name = ed1.getText().toString().trim();
                String email = ed2.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    ed1.setError(getString(R.string.invalid_name));
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    ed2.setError(getString(R.string.invalid_email));
                    return;
                }

                JSONObject userData = new JSONObject();
                try {
                    userData.put("userType", BUSINESS);
                    userData.put("phone", app.getCurrentUser().getPhoneNumber());
                    userData.put("name", name);
                    userData.put("email", email);
                } catch (JSONException e) {
                    Log.e(TAG, "onCreate: " + e);
                }

                mSocket.emit("update user details", userData);

                Toast.makeText(getApplicationContext(), "added", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(business_form.this, MainActivity.class));
                finish();

            }
        });

    }

}
