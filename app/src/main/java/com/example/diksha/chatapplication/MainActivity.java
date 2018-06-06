package com.example.diksha.chatapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int userType;
    private BottomNavigationView mNavigation;
    private FragmentTransaction transaction;
    private ProgressBar mLoadingView;
    private AlertDialog alertDialog;
    private Menu mMenu;
    private Socket mSocket;
    private ChatApplication app;
    private String name;
    private String email;
    private String address;
    private String type;
    private String time;
    private static final int CUSTOMER = 0;
    private static final int BUSINESS = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_recent_chats:
                    Fragment recentChatFragment = new RecentChatListFragment();
                    transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.list, recentChatFragment).addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_contacts:
                    Bundle b = new Bundle();
                    b.putInt("user type", userType);

                    Fragment userFragment = new UserListFragment();
                    userFragment.setArguments(b);

                    transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.list, userFragment).addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_products:
                    Fragment productFragment = new ProductFragment();
                    transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.list, productFragment).addToBackStack(null);
                    transaction.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on("get user type", OnGetCurrentUserType);
        mSocket.on("send business details", OnBusinessDetail);


        mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mLoadingView = (ProgressBar) findViewById(R.id.loadApp);


        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (null != activeNetwork) {
            mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        } else {
            buildDialog(MainActivity.this).show();
        }
    }

    @Override
    protected void onResume(){
        mNavigation.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mNavigation.setSelectedItemId(R.id.navigation_recent_chats);
        super.onResume();
    }

    public AlertDialog buildDialog(Context c) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("No Internet Connection");
        alertDialog.setMessage("Mobile Data is Turned Off.Go to Settings to Turn It on");
        alertDialog.setButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivityForResult(intent, 0);
            }
        });


        alertDialog.setButton2("EXIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog.show();
        return alertDialog;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {
            case R.id.editProfile:
                intent = new Intent(this, EditBussinessProfileActivity.class);
                Bundle b = new Bundle();
                b.putString("name", name);
                b.putString("email", email);
                b.putString("address", address);
                b.putString("business type", type);
                b.putString("delivery time", time);
                intent.putExtras(b);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Emitter.Listener OnGetCurrentUserType = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userType = (int) args[0];
                    Bundle b = new Bundle();
                    b.putInt("user type", userType);

                    android.support.v4.app.Fragment recentChatFragment = new RecentChatListFragment();
                    recentChatFragment.setArguments(b);

                    android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.list, recentChatFragment).addToBackStack(null);
                    transaction.commit();

                    if (userType == CUSTOMER) {
                        mNavigation.getMenu().removeItem(R.id.navigation_products);
                        MenuItem item = mMenu.findItem(R.id.editProfile);
                        item.setVisible(false);
                    }
                    if (userType == BUSINESS){
                        mSocket.emit("get business details", app.getCurrentUser().getPhoneNumber());
                    }
                    mNavigation.setVisibility(View.VISIBLE);
                    mLoadingView.setVisibility(View.GONE);

                }
            });
        }
    };

    Emitter.Listener OnBusinessDetail = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject result = (JSONObject) args[0];
                    try {
                         name = result.getString("name");
                         email = result.getString("email");
                         address = result.getString("address");
                         type = result.getString("business_type");
                         time = result.getString("delivery_time");

                    }catch (JSONException e){
                        Log.e(TAG, "run: Could not parse",e );
                    }
                    if (address.equals("null")){
                        final AlertDialog box = new AlertDialog.Builder(MainActivity.this).create();
                        box.setTitle("UPDATE DETAILS");
                        box.setIcon(R.drawable.ic_error_black_24dp);
                        box.setMessage("Update your details so that people can know about you business");
                        box.setButton("UPDATE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getBaseContext(), EditBussinessProfileActivity.class);
                                Bundle b = new Bundle();
                                b.putString("name", name);
                                b.putString("email", email);
                                b.putString("address", address);
                                b.putString("business type", type);
                                b.putString("delivery time", time);
                                intent.putExtras(b);
                                startActivityForResult(intent, 1);
                            }
                        });
                        box.setButton2("REMIND ME LATER", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                box.dismiss();
                            }
                        });
                        box.show();
                    }
                }
            });
        }
    };

}
