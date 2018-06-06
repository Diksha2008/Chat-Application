package com.example.diksha.chatapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class EditBussinessProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    EditText nameView;
    EditText emailView;
    EditText addressView;
    EditText typeView;
    EditText timeView;

    private String name;
    private String email;
    private String address;
    private String type;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        final ChatApplication app = (ChatApplication) getApplication();
        final Socket mSocket = app.getSocket();

         nameView = (EditText) findViewById(R.id.name_business);
         emailView = (EditText) findViewById(R.id.email_business);
         addressView = (EditText) findViewById(R.id.address);
         typeView = (EditText) findViewById(R.id.business_type);
         timeView = (EditText) findViewById(R.id.business_hours);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            name = bundle.getString("name");
            email = bundle.getString("email");
            address = bundle.getString("address");
            type = bundle.getString("business type");
            time = bundle.getString("delivery time");
        }

        nameView.setText(name);
        emailView.setText(email);
        if (!address.equals("null") && !address.isEmpty()){
            addressView.setText(address);
        }
        if (!type.equals("null") && !type.isEmpty()){
            typeView.setText(type);
        }
        if (!time.equals("null") && !time.isEmpty()){
            timeView.setText(time);
        }

        Button button = (Button) findViewById(R.id.update);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 name = nameView.getText().toString().trim();
                 email = emailView.getText().toString().trim();
                address = addressView.getText().toString().trim();
                type = typeView.getText().toString().trim();
                time = timeView.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    nameView.setError(getString(R.string.invalid_name));
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    emailView.setError(getString(R.string.invalid_email));
                    return;
                }
                if (TextUtils.isEmpty(address)) {
                    addressView.setError(getString(R.string.invalid_address));
                    return;
                }

                if (TextUtils.isEmpty(type)) {
                    typeView.setError(getString(R.string.invalid_type));
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    timeView.setError(getString(R.string.invalid_time));
                    return;
                }

                JSONObject data = new JSONObject();
                try {
                    data.put("phone", app.getCurrentUser().getPhoneNumber());
                    data.put("name", name);
                    data.put("email", email);
                    data.put("address", address);
                    Log.i(TAG, "onClick: " + address);
                    data.put("businessType", type);
                    data.put("deliveryTime", time);
                    mSocket.emit("update business details", data);
                }catch (JSONException e){
                    Log.e(TAG, "onClick: Couldn't create object",e);
                }
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
