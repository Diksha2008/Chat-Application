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

    private RelativeLayout.LayoutParams messageParams;
    private RelativeLayout.LayoutParams timeViewParams;

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

        messageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        timeViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final Message message = mMessages.get(position);

        //set time view
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat output = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        try {
            Date date = input.parse(message.createdAt());
            holder.mTimeView.setText(output.format(date));
        }catch (ParseException e){
            Log.d(TAG, "displayTime: Could not parse" );
        }


        //set message view
        Boolean isImage = message.getmImage() != null;
        if (isImage){
            holder.mTextView.setVisibility(View.GONE);
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.mImageView.setImageURI(message.getmImage());

            //aligning the message
            if(!message.isReceived()){
                alignTimeView(holder.mTimeView,RelativeLayout.LEFT_OF, R.id.image);
                alignMessageView(holder.mImageView, false, R.drawable.rounded_purple_rectangle, RelativeLayout.ALIGN_PARENT_RIGHT, Color.WHITE);
            }
            else {
                alignTimeView(holder.mTimeView, RelativeLayout.RIGHT_OF, R.id.image);
                alignMessageView(holder.mImageView, false, R.drawable.rounded_grey_rectangle, RelativeLayout.ALIGN_PARENT_LEFT, Color.BLACK);
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
                alignTimeView(holder.mTimeView,RelativeLayout.LEFT_OF, R.id.msg);
                alignMessageView(holder.mTextView, true, R.drawable.rounded_purple_rectangle, RelativeLayout.ALIGN_PARENT_RIGHT, Color.WHITE);
            }
            else {
                alignTimeView(holder.mTimeView, RelativeLayout.RIGHT_OF, R.id.msg);
                alignMessageView(holder.mTextView, true, R.drawable.rounded_grey_rectangle, RelativeLayout.ALIGN_PARENT_LEFT, Color.BLACK);
            }
        }
    }

    public void alignTimeView(TextView timeView, int alignment, int id){
        timeViewParams.addRule(alignment, id);
        timeViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, id);
        timeView.setLayoutParams(timeViewParams);
    }


    public void alignMessageView(View messageView, Boolean isText, int backgroundResource, int alignment, int textColor){
        messageParams.addRule(alignment);
        messageView.setLayoutParams(messageParams);
        messageView.setBackgroundResource(backgroundResource);
        if(isText) {
            ((TextView)messageView).setTextColor(textColor);
        }
    }



    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
