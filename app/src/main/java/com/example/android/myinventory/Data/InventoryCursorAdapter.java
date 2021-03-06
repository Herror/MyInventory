package com.example.android.myinventory.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    public void bindView(View view, final Context context, Cursor cursor) {
        //Fields to populate the inflated template
        TextView nameView = (TextView) view.findViewById(R.id.product_name);
        TextView quantityView = (TextView) view.findViewById(R.id.product_quantity);
        TextView priceView = (TextView) view.findViewById(R.id.product_price);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);
        //Find the column of the product attributes that I'm interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        //read the product attributes from the cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        //Set quantity and price as Strings so it can be assigned to the TextView
        String productQuantity = String.valueOf(quantity);
        String productPrice = "$" + String.valueOf(price);
        //populate the fields with the extracted proprieties
        nameView.setText(productName);
        quantityView.setText(productQuantity);
        priceView.setText(productPrice);

        //Create the Uri for the product on the specific row
        int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final Uri mProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                //Check if the quantity is greater than 0
                //If so proceed by removing one at the time when pressing the sell button
                if(quantity > 0){
                    int qq = quantity;
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, --qq);
                    resolver.update(
                           mProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(mProductUri, null);
                }else {
                    //If the quantity is 0 - display a toast message
                    Toast.makeText(view.getContext(), R.string.you_are_out_of_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
