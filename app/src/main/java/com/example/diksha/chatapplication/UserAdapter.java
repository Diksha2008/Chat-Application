package com.example.diksha.chatapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by diksha on 15/3/18.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> mUsers;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mDisplayUsername;
        public ViewHolder(View v){
            super(v);
            mDisplayUsername = (TextView) v.findViewById(R.id.username);
        }
    }
    public UserAdapter(List<User> Users){
        mUsers = Users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.mDisplayUsername.setText(user.getUsername());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
