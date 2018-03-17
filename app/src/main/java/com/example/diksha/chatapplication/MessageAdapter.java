package com.example.diksha.chatapplication;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diksha on 13/3/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.msg);
            mImageView = (ImageView) v.findViewById(R.id.image);
        }
    }

    public MessageAdapter(List<Message> myMessage) {
        mMessages = myMessage;
    }


    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        Boolean isImage = message.getmImage() != null;
        if (isImage){
            holder.mTextView.setVisibility(View.GONE);
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.mImageView.setImageBitmap(message.getmImage());
        }
        else {
            holder.mTextView.setVisibility(View.VISIBLE);
            holder.mImageView.setVisibility(View.GONE);
            holder.mTextView.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
