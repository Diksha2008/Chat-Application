package com.example.diksha.chatapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

public class Main2Activity extends AppCompatActivity {

    private static final String TAG = "Main2Activity";
    private int userType;
    private BottomNavigationView navigation;
    private static final int BUSINESS = 1;
    private static final int CUSTOMER = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_recent_chats:
                    Fragment recentChatFragment = new RecentChatListFragment();
                    FragmentTransaction chatTransaction = getSupportFragmentManager().beginTransaction();
                    chatTransaction.replace(R.id.list, recentChatFragment).addToBackStack(null);
                    chatTransaction.commit();
                    return true;
                case R.id.navigation_contacts:
                    Bundle b = new Bundle();
                    b.putInt("user type", userType);

                    Fragment userFragment = new UserListFragment();
                    userFragment.setArguments(b);

                    FragmentTransaction contactTransaction = getSupportFragmentManager().beginTransaction();
                    contactTransaction.replace(R.id.list, userFragment).addToBackStack(null);
                    contactTransaction.commit();
                    return true;
                case R.id.navigation_products:
                    Fragment productFragment = new ProductFragment();
                    FragmentTransaction productTransaction = getSupportFragmentManager().beginTransaction();
                    productTransaction.replace(R.id.list, productFragment).addToBackStack(null);
                    productTransaction.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ChatApplication app = (ChatApplication) getApplication();
        Socket mSocket = app.getSocket();
        mSocket.on("get user type", OnGetCurrentUserType);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    private Emitter.Listener OnGetCurrentUserType = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Main2Activity.this.runOnUiThread(new Runnable() {
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

                    if (userType == CUSTOMER){
                        navigation.getMenu().removeItem(R.id.navigation_products);
                    }
                    navigation.setVisibility(View.VISIBLE);
                }
            });
        }
    };

}
