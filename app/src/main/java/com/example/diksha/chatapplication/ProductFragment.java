package com.example.diksha.chatapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Priyanka on 6/3/2018.
 */

public class ProductFragment extends android.support.v4.app.Fragment{
    private List<Product> mProductList = new ArrayList<Product>();
    private RecyclerView.Adapter mProductAdapter;
    private String user_input;
    private String item_price;
    public FloatingActionButton fabButton;
    private RecyclerView mProductListView;
    private  int counter=1;
    public ProductFragment(){
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProductAdapter = new ProductAdapter(mProductList, getActivity());
        mProductListView = (RecyclerView) view.findViewById(R.id.productList);
        mProductListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProductListView.setAdapter(mProductAdapter);
        fabButton = (FloatingActionButton) view.findViewById(R.id.fab_image_button);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    private void addItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ADD ITEM");
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText input = new EditText(getActivity());
        input.setHint("Item Name");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(input);

        final EditText price = new EditText((getActivity()));
        price.setHint("Enter Price");
        price.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(price);

        builder.setView(layout);

        builder.setPositiveButton("ENTER ITEM", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {
                user_input = input.getText().toString().trim();
                item_price = price.getText().toString().trim();
                if(user_input.length()!=0 && item_price.length() != 0 ){
                    Product product = new Product(user_input,item_price);
                    mProductList.add(product);
                    mProductAdapter.notifyItemInserted(mProductList.size()-1);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
