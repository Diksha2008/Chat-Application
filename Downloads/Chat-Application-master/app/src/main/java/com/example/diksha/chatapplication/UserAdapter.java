package com.example.diksha.chatapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> mUsers;
    private Activity mActivity;
    private Socket mSocket;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDisplayUsername;
        public ImageView mLabel;
        public TextView  mUnread;
        public GradientDrawable mShape;
        public ViewHolder(View v){
            super(v);
            mUnread = (TextView)v.findViewById(R.id.unread) ;
            mLabel = (ImageView) v.findViewById(R.id.label);
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
    };

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final User user = mUsers.get(position);
        if(Integer.parseInt(user.getUnreadMsg())!=0)
        holder.mUnread.setText(user.getUnreadMsg());
        else
            holder.mUnread.setVisibility(View.GONE);
        String black = "#000000 ";
        if(user.getColor().equals(black)){
            holder.mLabel.setVisibility(View.GONE);
        }
        else{
            holder.mLabel.setColorFilter(Color.parseColor(user.getColor()));
        }

        holder.mDisplayUsername.setText(user.getUserId());
        holder.mDisplayUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.Fragment chatFragment = new ChatFragment();
                android.support.v4.app.FragmentTransaction transaction = ((FragmentActivity) mActivity).getSupportFragmentManager().beginTransaction();
                                                           transaction.replace(R.id.chat, chatFragment).addToBackStack(null).commit();
                //sending the user to which chat is initiated to the fragment
                Bundle toUser = new Bundle();
                toUser.putString("toUser", user.getUserId());
                chatFragment.setArguments(toUser);
                String id = user.getUserId();
                joinRoom(id);
            }
        });

        holder.mDisplayUsername.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                final ChatApplication app = (ChatApplication) mActivity.getApplication();
                mSocket = app.getSocket();
                final JSONObject colorData= new JSONObject();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
                alertDialog.setTitle("Add Label");
                alertDialog.setPositiveButton("RED", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        holder.mLabel.setColorFilter(Color.RED);
                        holder.mLabel.setVisibility(View.VISIBLE);
                        mSocket.emit("here");
                    }
                });
                alertDialog.setNegativeButton("BLUE", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        holder.mLabel.setColorFilter(Color.BLUE);
                        holder.mLabel.setVisibility(View.VISIBLE);
                    }
                });
                alertDialog.setNeutralButton("REMOVE", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        holder.mLabel.setVisibility(View.GONE);
                    }
                });
                alertDialog.show();
                return true;
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
