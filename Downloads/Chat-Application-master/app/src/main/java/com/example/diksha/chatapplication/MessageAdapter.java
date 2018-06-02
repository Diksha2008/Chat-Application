package com.example.diksha.chatapplication;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private Context mContext;
    private String mCurrentUser;

    private RelativeLayout.LayoutParams messageParams;
    private RelativeLayout.LayoutParams timeViewParams;
    private RelativeLayout.LayoutParams deliverViewParams;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView mTimeView;
        public ImageView mTickView;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.msg);
            mTimeView = v.findViewById(R.id.time);
            mTickView = v.findViewById(R.id.deltick);
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

        deliverViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final Message message = mMessages.get(position);

        //set time view
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat output = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        try {
            Date date = input.parse(message.createdAt());
            holder.mTimeView.setText(output.format(date));
        } catch (ParseException e) {
        }

        holder.mTextView.setText(message.getMessage());
        if (!message.isReceived())
        {
           if(message.getIsDelivered()) {
                holder.mTickView.setImageResource(R.drawable.deldtick);
            }
            alignDeliverView(holder.mTickView,RelativeLayout.LEFT_OF,R.id.deltick);
            alignTimeView(holder.mTimeView, RelativeLayout.LEFT_OF, R.id.msg);
            alignMessageView(holder.mTextView, true, R.drawable.rounded_purple_rectangle, RelativeLayout.ALIGN_PARENT_RIGHT, Color.WHITE);
        }
        else
        {
            holder.mTickView.setVisibility(View.GONE);
            //no need to show status in this one
            alignTimeView(holder.mTimeView, RelativeLayout.RIGHT_OF, R.id.msg);
            alignMessageView(holder.mTextView, true, R.drawable.rounded_grey_rectangle, RelativeLayout.ALIGN_PARENT_LEFT, Color.BLACK);
        }
    }

    public void alignTimeView(TextView timeView, int alignment, int id){
        timeViewParams.addRule(alignment, id);
        timeViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, id);
        timeView.setLayoutParams(timeViewParams);
    }
    public void alignDeliverView(ImageView imageView,int alignment,int id){
        deliverViewParams.addRule(alignment,id);
        deliverViewParams.addRule(RelativeLayout.ALIGN_BOTTOM,id);
        imageView.setLayoutParams(deliverViewParams);
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
