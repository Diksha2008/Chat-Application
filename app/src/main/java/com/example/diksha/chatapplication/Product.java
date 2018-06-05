package com.example.diksha.chatapplication;

/**
 * Created by Priyanka on 6/2/2018.
 */

public class Product
{
    private String mProductName;
    private String mPrice;
    public Product(String productName,String price)
    {
        mProductName=productName;
        mPrice=price;
    }
    public String getProductName() {
        return mProductName;
    }
    public String getProductPrice(){
        return mPrice;
    }
}
