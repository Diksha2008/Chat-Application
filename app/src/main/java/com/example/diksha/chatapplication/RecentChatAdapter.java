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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.content.ContentValues.TAG;


public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ViewHolder> {
    private static final int BUSINESS = 1;
    private List<User> mUsers;
    private Activity mActivity;
    boolean setOrderPending = true;
    boolean setOrderComplete = false;
    boolean setOrderConfirmed = false;
    boolean setInTransit = false;
    private Socket mSocket;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDisplayUsername;
        public ImageView mLabel;
        public TextView mUnread;
        public RelativeLayout mLayout;
        public GradientDrawable mShape;

        public ViewHolder(View v) {
            super(v);
            mUnread = (TextView) v.findViewById(R.id.unread);
            mLabel = (ImageView) v.findViewById(R.id.label);
            mDisplayUsername = (TextView) v.findViewById(R.id.username);
            mLayout = (RelativeLayout) v.findViewById(R.id.user);
        }
    }

    public RecentChatAdapter(List<User> Users, Activity context) {
        mUsers = Users;
        mActivity = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_users, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    ;

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final User user = mUsers.get(position);
        final String color = user.getColor().trim();
        Log.i(TAG, "onBindViewHolder: " + color);
        if (user.getUnreadMsg() == 0)
            holder.mUnread.setVisibility(View.GONE);
        else {
            String value = Integer.toString(user.getUnreadMsg());
            holder.mUnread.setText(value);
        }
        holder.mLabel.setColorFilter(Color.parseColor(color));

        if (user.getUsername() != null)
            holder.mDisplayUsername.setText(user.getUsername());
        else
            holder.mDisplayUsername.setText(user.getPhone());
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.Fragment chatFragment = new ChatFragment();
                android.support.v4.app.FragmentTransaction transaction = ((FragmentActivity) mActivity).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.list, chatFragment).addToBackStack(null).commit();
                //sending the user to which chat is initiated to the fragment
                Bundle b = new Bundle();
                b.putString("toUser", user.getPhone());
                b.putString("username", user.getUsername());
                b.putInt("user type", user.getUserType());
                chatFragment.setArguments(b);
                String id = user.getPhone();
                joinRoom(id);
            }
        });

        if (MainActivity.getUserType() == BUSINESS) {

            holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int checked;
                    Log.i(TAG, "onLongClick: " + color);
                    if (color.equals("#000080")){
                        checked = 0;
                    }
                    else if(color.equals("#d11141")){
                        checked = 1;
                    }
                    else if(color.equals("#00b159")){
                        checked = 2;
                    }
                    else if(color.equals("#ffc425")){
                        checked = 3;
                    }
                    else {
                        checked = 0;
                    }
                    final ChatApplication app = (ChatApplication) mActivity.getApplication();
                    mSocket = app.getSocket();
                    final JSONObject colorData = new JSONObject();
                    try {
                        colorData.put("person1", user.getPhone());
                        colorData.put("person2", app.getCurrentUser().getPhoneNumber());
                    } catch (JSONException e) {
                    }
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                    dialog.setTitle("Add Label");
                    final CharSequence[] choices = {
                            "Order Pending", "Order Complete", "Order Confirmed", "In Transit"
                    };
                    dialog.setSingleChoiceItems(choices, checked, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((AlertDialog) dialog).getListView().setItemChecked(which, true);
                            if (which == 0) {
                                setOrderPending = true;
                                setOrderComplete = false;
                                setOrderConfirmed = false;
                                setInTransit = false;
                                try {
                                    colorData.put("label_type", "Order Pending");
                                    colorData.put("color", "#000080");
                                } catch (JSONException e) {
                                }
                            } else if (which == 1) {
                                setOrderPending = false;
                                setOrderComplete = true;
                                setOrderConfirmed = false;
                                setInTransit = false;
                                try {
                                    colorData.put("label_type", "Order Complete");
                                    colorData.put("color", "'#d11141");
                                } catch (JSONException e) {
                                }

                            } else if (which == 2) {
                                setOrderPending = false;
                                setOrderComplete = false;
                                setOrderConfirmed = true;
                                setInTransit = false;
                                try {
                                    colorData.put("label_type", "Order Confirmed");
                                    colorData.put("color", "#00b159");
                                } catch (JSONException e) {
                                }
                            } else if (which == 3) {
                                setOrderPending = false;
                                setOrderComplete = false;
                                setOrderConfirmed = false;
                                setInTransit = true;
                                try {
                                    colorData.put("label_type", "Order In Transit");
                                    colorData.put("color", "#ffc425");
                                } catch (JSONException e) {
                                }
                            }
                        }
                    });
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (setOrderPending == true) {
                                holder.mLabel.setColorFilter(Color.BLUE);
                                holder.mLabel.setVisibility(View.VISIBLE);
                                mSocket.emit("add label", colorData);
                            }
                            if (setOrderComplete == true) {
                                holder.mLabel.setColorFilter(Color.RED);
                                holder.mLabel.setVisibility(View.VISIBLE);
                                mSocket.emit("add label", colorData);
                            }
                            if (setOrderConfirmed == true) {
                                holder.mLabel.setColorFilter(Color.GREEN);
                                holder.mLabel.setVisibility(View.VISIBLE);
                                mSocket.emit("add label", colorData);
                            }
                            if (setInTransit == true) {
                                holder.mLabel.setColorFilter(Color.YELLOW);
                                holder.mLabel.setVisibility(View.VISIBLE);
                                mSocket.emit("add label", colorData);
                            }

                        }
                    });
                    dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.setNeutralButton("REMOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSocket.emit("remove label", colorData);
                            holder.mLabel.setVisibility(View.GONE);
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void joinRoom(String userId) {
        ChatApplication app = (ChatApplication) mActivity.getApplication();
        mSocket = app.getSocket();
        JSONObject roomData = new JSONObject();
        try {
            roomData.put("person1", userId);
            roomData.put("person2", app.getCurrentUser().getPhoneNumber());
        } catch (JSONException e) {
        }
        mSocket.emit("join room", roomData);
    }
}
