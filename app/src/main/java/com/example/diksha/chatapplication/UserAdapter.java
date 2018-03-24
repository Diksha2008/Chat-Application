package com.example.diksha.chatapplication;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by diksha on 15/3/18.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> mUsers;
    private Activity mActivity;
    private Socket mSocket;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mDisplayUsername;
        public ViewHolder(View v){
            super(v);
            mDisplayUsername = (TextView) v.findViewById(R.id.username);
        }
    }
    public UserAdapter(List<User> Users, Activity context){
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.mDisplayUsername.setText(user.getUserId());
        holder.mDisplayUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.Fragment chatFragment = new ChatFragment();
                android.support.v4.app.FragmentTransaction transaction = ((FragmentActivity)mActivity).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.chat, chatFragment).addToBackStack(null).commit();
                //sending the user to which chat is initiated to the fragment
                Bundle toUser = new Bundle();
                toUser.putString("toUser", user.getUserId());
                chatFragment.setArguments(toUser);
                joinRoom(user.getUserId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void joinRoom(String userId){
        ChatApplication app = (ChatApplication) mActivity.getApplication();
        mSocket = app.getSocket();
        JSONObject roomData = new JSONObject();
        try{
            roomData.put("person1", userId);
            roomData.put("person2", app.getCurrentUser().getPhoneNumber());
        }catch (JSONException e){  }
        mSocket.emit("join room", roomData);
    }
}
