package com.example.diksha.chatapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> mProducts;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemName;
        public TextView mPrice;
        public ImageButton fabButton;
        public ImageView mDelete;
        public ViewHolder(View v){
            super(v);
            mItemName = (TextView)v.findViewById(R.id.itemName) ;
            fabButton = (ImageButton)v.findViewById(R.id.fab_image_button);
            mPrice = (TextView) v.findViewById(R.id.price);
            mDelete=(ImageView)v.findViewById(R.id.delete);
        }
    }
    public ProductAdapter(List<Product> Products, Activity context){
        mProducts = Products;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    };

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        final Product product = mProducts.get(position);
        holder.mItemName.setText(product.getProductName());
        holder.mPrice.setText(product.getProductPrice());
        holder.mDelete.setVisibility(View.VISIBLE);
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProducts.remove(product);
                notifyItemRemoved(position);
            }
        });

    }
   @Override
    public int getItemCount() {
        return mProducts.size();
    }


}
