package com.example.android.myinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.myinventory.Data.InventoryDbHelper;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity {

    private InventoryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //Setup the FAB to open the EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new InventoryDbHelper(this);
        displayDatabaseInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new InventoryDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        //Define projection for the cursor
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE
        };
        //Query the information from the table
        Cursor cursor = db.query(
                InventoryEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        TextView displayView = (TextView) findViewById(R.id.test);

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            displayView.setText("The inventory table contains " + cursor.getCount() + " products.\n\n");
            displayView.append(InventoryEntry._ID + " - " +
                    InventoryEntry.COLUMN_PRODUCT_NAME + " - " +
                    InventoryEntry.COLUMN_PRODUCT_QUANTITY + " - " + 
                    InventoryEntry.COLUMN_PRODUCT_PRICE + "\n");

            //figure out the index for each column
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int quantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int priceIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);

            //iterate though all the returned rows in the cursor
            while (cursor.moveToNext()){
                //use that index to extract the String or int value of the word
                //at the current row the cursor is on
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentQuantity = cursor.getInt(quantityIndex);
                int currentPrice = cursor.getInt(priceIndex);
                //display the values from each column of the current row
                displayView.append("\n" + currentID + " - " + currentName + " - " + currentQuantity
                 + " - " + currentPrice + "$");
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    private void insertProduct(){
        //Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Create a new map of values where the column names are the keys
        ContentValues values = new ContentValues();
        //Add the desired values for the dummy data
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Bolts");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, "100");
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, "200");
        //Insert the data into the table
        db.insert(InventoryEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu option from the res/menu/editor_menu.xml file.
        //This adds menu items to the top of the bar
        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User click on a menu option in the bar overflow menu
        switch (item.getItemId()){
            //Respond to a click on the "Insert dummy data" menu option
            case R.id.dummy_data:
                insertProduct();
                return true;
            //Respond to a click on the "Delete all" menu option
            case R.id.delete_all:
                //TODO
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
