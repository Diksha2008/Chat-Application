package com.example.diksha.chatapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by diksha on 13/3/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private Context mContext;
    private String mCurrentUser;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;
        public TextView mTimeView;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.msg);
            mImageView = v.findViewById(R.id.image);
            mTimeView = v.findViewById(R.id.time);
        }
    }

    public MessageAdapter(List<Message> myMessage, Context context) {
        mMessages = myMessage;
        mContext = context;
    }


    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RelativeLayout.LayoutParams messageTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        messageTextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams timeViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final Message message = mMessages.get(position);

        //set time view
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        SimpleDateFormat output = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        try {
            Date date = input.parse(message.createdAt());
            holder.mTimeView.setText(output.format(date));
        }catch (ParseException e){ }


        //set message view
        Boolean isImage = message.getmImage() != null;
        if (isImage){
            holder.mTextView.setVisibility(View.GONE);
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.mImageView.setImageURI(message.getmImage());

            //aligning the message
            if(!message.isReceived()){
                alignTimeView(timeViewParams, holder.mTimeView,R.id.image);
                holder.mImageView.setLayoutParams(messageTextParams);
                holder.mImageView.setBackgroundResource(R.drawable.rounded_purple_rectangle);
            }
            else {
                timeViewParams.addRule(RelativeLayout.RIGHT_OF,R.id.image);
                timeViewParams.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.image);
                holder.mTimeView.setLayoutParams(timeViewParams);
            }

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(message.getmImage(), "image/*");
                    mContext.startActivity(intent);
                }
            });
        }
        else {
            holder.mTextView.setVisibility(View.VISIBLE);
            holder.mImageView.setVisibility(View.GONE);
            holder.mTextView.setText(message.getMessage());

            // aligning the message
            if(!message.isReceived()){
                alignTimeView(timeViewParams, holder.mTimeView,R.id.msg);
                holder.mTextView.setLayoutParams(messageTextParams);
                holder.mTextView.setBackgroundResource(R.drawable.rounded_purple_rectangle);
                holder.mTextView.setTextColor(Color.WHITE);
            }
        }
    }
    public void alignTimeView(RelativeLayout.LayoutParams timeViewParams, TextView timeView, int id){
        timeViewParams.addRule(RelativeLayout.LEFT_OF,id);
        timeViewParams.addRule(RelativeLayout.ALIGN_BOTTOM,id);
        timeView.setLayoutParams(timeViewParams);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
