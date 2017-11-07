package com.example.android.myinventory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.myinventory.Data.InventoryContract;
import com.example.android.myinventory.Data.InventoryDbHelper;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity {

    //EditText field to enter the name of the product
    private EditText mProductNameEditText;
    //EditText filed to enter the quantity of the product
    private EditText mProductQuantityEditText;
    //EditText filed to enter the price of the product
    private EditText mProductPriceEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Find all the relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.product_name);
        mProductQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mProductPriceEditText = (EditText) findViewById(R.id.product_price);
    }

    //Gets user input and saves the new product into the Inventory database
    public void insertProduct() {
        //get the inserted text
        String productName = mProductNameEditText.getText().toString().trim();
        String productQuantity = mProductQuantityEditText.getText().toString().trim();
        String productPrice = mProductPriceEditText.getText().toString().trim();
        //set the text to integers
        int quantity = Integer.parseInt(productQuantity);
        int price = Integer.parseInt(productPrice);

        //Create a ContentValues object where column names are the keys,
        //and inventory attributes from thge editor are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if(newUri == null){
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "You've screwed up", Toast.LENGTH_SHORT).show();
        }else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, "New product added", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu option from the res/menu/editor_menu.xml file.
        //This adds menu items to the top of the bar
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //user click on the menu option in the app bar overflow menu
        switch (item.getItemId()) {
            //respond to the Save menu option
            case R.id.action_save:
                insertProduct();
                //exit the activity
                finish();
                //respond to the Delete menu option
            case R.id.action_delete:
                //TODO
        }
        return super.onOptionsItemSelected(item);
    }
}
