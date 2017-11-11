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
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.myinventory.Data.InventoryCursorAdapter;
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

        //find the ListView witch will be populated with the pet data
        ListView inventoryListView = (ListView) findViewById(R.id.list_view_product);
        //find and set empty view on the ListView, so that it will show only when the list
        //has 0 items
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

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

        //Define projection for the cursor
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE
        };

        //Perform a query on the provider using the ContentResolver.
        //use the InventoryEntry.CONTENT_URI to access the inventory data
        Cursor cursor = getContentResolver().query(
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        //Create a reference to the text_view_product from the activity_catalog.xml
        // to populate it
        ListView displayView = (ListView) findViewById(R.id.list_view_product);
        //Setup the cursor adapter
        InventoryCursorAdapter inventoryAdapter = new InventoryCursorAdapter(this, cursor);
        //Attach the cursor to the ListView
        displayView.setAdapter(inventoryAdapter);
    }

    private void insertProduct(){
        //Create a new map of values where the column names are the keys
        ContentValues values = new ContentValues();
        //Add the desired values for the dummy data
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Bolts");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, "100");
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, "200");

        //Insert the data into the table using the ContentProvider
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
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
