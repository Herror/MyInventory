package com.example.android.myinventory.Data;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.myinventory.R;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    //Creating constructor
    public InventoryCursorAdapter(Context context, Cursor c){
        super(context, c, 0 );
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Fields to populate the inflated template
        TextView nameView = (TextView) view.findViewById(R.id.product_name);
        TextView quantityView = (TextView) view.findViewById(R.id.product_quantity);
        TextView priceView = (TextView) view.findViewById(R.id.product_price);
        //Find the column of the product attributes that I'm interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        //read the product attributes from the cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);
        String productPrice = "$" + cursor.getString(priceColumnIndex);
        //populate the fields with the extracted proprieties
        nameView.setText(productName);
        quantityView.setText(productQuantity);
        priceView.setText(productPrice);
    }
}
