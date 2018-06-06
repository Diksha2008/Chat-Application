package com.example.diksha.chatapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Priyanka on 6/3/2018.
 */

public class ProductFragment extends android.support.v4.app.Fragment{
    private Socket mSocket;
    boolean isListLoaded=false;
    private FirebaseUser mCurrentUser;
    private List<Product> mProductList = new ArrayList<Product>();
    private RecyclerView.Adapter mProductAdapter;
    private String user_input;
    private String item_price;
    boolean visible=false;
    public int counter;
    public FloatingActionButton fabButton;
    private RecyclerView mProductListView;
    public ProductFragment(){
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mCurrentUser=app.getCurrentUser();
        mSocket.on("product list",ProductList);
        mSocket.emit("create product list",mCurrentUser.getPhoneNumber());
    }
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        Log.i(TAG, "setUserVisibleHint: " + (isVisibleToUser && !isListLoaded));
//        if (isVisibleToUser && !isListLoaded) {
//            isListLoaded = true;
//            mSocket.emit("create product list",mCurrentUser.getPhoneNumber());
//        }
//    }

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
                user_input = input.getText().toString();
                item_price = price.getText().toString();
                if(user_input.length()!=0 && item_price.length() != 0 ){
                    Product product = new Product(user_input,item_price);
                    mProductList.add(product);
                    mProductAdapter.notifyItemInserted(mProductList.size()-1);
                    JSONObject data = new JSONObject();
                    try
                    {
                        data.put("productName",user_input);
                        data.put("itemPrice", item_price);
                        data.put("id",mCurrentUser.getPhoneNumber());
                    }
                    catch (JSONException e) {}
                    mSocket.emit("add product",data);

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
    private Emitter.Listener ProductList = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Log.e(TAG, "run: " + args[0], new Exception());
                    JSONArray result = (JSONArray) args[0];
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject jsonObject = (JSONObject) result.get(i);
                            String productName = jsonObject.optString("productName");
                            String productPrice = jsonObject.optString("productPrice");
                            Product product = new Product(productName,productPrice);
                            mProductList.add(product);
                            mProductAdapter.notifyItemInserted(mProductList.size() - 1);
                        } catch (JSONException e) {}
                    }
                    mSocket.off("product list",ProductList);
                }
            });
        }
    };
}
