package com.example.diksha.chatapplication;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by diksha on 15/3/18.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private static final String TAG = "UserAdapter";
    private List<User> mUsers;
    private Activity mActivity;
    private static final int BUSINESS = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mUserView;
        public TextView mDisplayUsername;
        public TextView mUserTypeView;

        public ViewHolder(View v) {
            super(v);
            mUserView = (LinearLayout) v.findViewById(R.id.user);
            mDisplayUsername = (TextView) v.findViewById(R.id.username);
            mUserTypeView = (TextView) v.findViewById(R.id.userType);

        }
    }

    public UserAdapter(List<User> Users, Activity context) {
        mUsers = Users;
        mActivity = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = mUsers.get(position);
//        Log.i(TAG, "onClick: " + user.getUserType());
        if (user.getUserType() == BUSINESS) {
            holder.mUserTypeView.setText("business");
        }
        holder.mDisplayUsername.setText(user.getUsername());
        holder.mUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.support.v4.app.Fragment chatFragment = new ChatFragment();
                android.support.v4.app.FragmentTransaction transaction = ((FragmentActivity) mActivity).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.list, chatFragment).addToBackStack(null).commit();

                //sending the user to which chat is initiated to the fragment
                Bundle b = new Bundle();
                b.putString("toUser", user.getPhone());
                b.putInt("user type", user.getUserType());
                b.putString("username", user.getUsername());
                chatFragment.setArguments(b);
                joinRoom(user.getPhone());

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    private void joinRoom(String userId) {
        ChatApplication app = (ChatApplication) mActivity.getApplication();
        Socket mSocket = app.getSocket();
        JSONObject roomData = new JSONObject();
        try {
            roomData.put("person1", userId);
            roomData.put("person2", app.getCurrentUser().getPhoneNumber());
        } catch (JSONException e) {
            Log.e(TAG, "joinRoom: Could not parse", e);
        }
        mSocket.emit("join room", roomData);
    }
}
