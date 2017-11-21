package com.example.android.myinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myinventory.Data.InventoryCursorAdapter;
import com.example.android.myinventory.Data.InventoryDbHelper;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //Create an ID that will be used by the CursorLoader
    private static final int PRODUCT_LOADER = 0;
    //Create a global variable for the Adapter
    InventoryCursorAdapter mInventoryCursorAdapter;

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

        //Setup the adapter to create a list item for each row of the pet data in the Cursor
        //There is no pet data yet (until the loader finishes) so pass in null for Cursor
        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        //attach the cursor adapter to the ListView
        inventoryListView.setAdapter(mInventoryCursorAdapter);

        //setup itemClickListener for opening the product at the selected position
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create a new intent to open the editor activity when an item in the list is selected
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                /**
                 * Form the content URI that represents the specific product that was clicked on,
                 * by appending the "id" (passed as an input to this method) onto the
                 * InventoryEntry.CONTENT_URI
                 */
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                //set the URI on the data filled of the intent
                intent.setData(currentInventoryUri);
                //launch the activity
                startActivity(intent);
            }
        });

        //Initialize the cursorLoader itself
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

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
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define projection
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE
        };
        //Return the CursorLoader
        return new CursorLoader(
                this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //update the new cursor containing the the updated data
        mInventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback when the data needs to be deleted
        mInventoryCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_catalog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllProducts(){
        int deleteProducts = getContentResolver().delete(
                InventoryEntry.CONTENT_URI,
                null,
                null
        );
        if(deleteProducts == 0){
            Toast.makeText(this, R.string.catalog_delete_products_failed, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, R.string.catalog_delete_products_successful, Toast.LENGTH_SHORT).show();
        }
    }
}
