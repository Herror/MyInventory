package com.example.android.myinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.myinventory.Data.InventoryContract;
import com.example.android.myinventory.Data.InventoryDbHelper;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    //EditText field to enter the name of the product
    private EditText mProductNameEditText;
    //EditText filed to enter the quantity of the product
    private EditText mProductQuantityEditText;
    //EditText filed to enter the price of the product
    private EditText mProductPriceEditText;
    //Create a URI for one pet
    private Uri mCurrentProductUri;
    //Identifies a particular Loader being used in the component
    private static final int EXISTING_PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //examine the intent that was used to launch the activity
        //to figure out if I'm creating a new product or edit an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //Check to see if the currentProductUri contains any data
        //If the intent DOES NOT contain a content URI, then I know that I am creating a new product
        if(mCurrentProductUri == null){
            //if it doesn't contain, it will know that it will need to create a new product
            //and add the title "Add a product"
            setTitle(R.string.editor_activity_title_new_product);
        }else {
            //If it contains it will update the information and it will change the
            //title to "Edit product"
            setTitle(R.string.editor_activity_title_edit_product);
            //Initialize a loader to read the product data from the database
            //and display the current value in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all the relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.product_name);
        mProductQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mProductPriceEditText = (EditText) findViewById(R.id.product_price);
    }

    //Gets user input and saves the new product into the Inventory database
    public void saveProduct() {
        //get the inserted text
        String productName = mProductNameEditText.getText().toString().trim();
        String productQuantity = mProductQuantityEditText.getText().toString().trim();
        String productPrice = mProductPriceEditText.getText().toString().trim();

        //Check and see if all fields in the editor are blank
        //If so, I'll return it so it won't bother with the rest of the saveProduct method
        if(mCurrentProductUri == null &&
                TextUtils.isEmpty(productName) && TextUtils.isEmpty(productPrice) &&
                TextUtils.isEmpty(productQuantity)){
            return;
        }

        //If the quantity and the price are not provided by the user, do not try to parse
        //the strings into Integers. Use 0 by default
        int quantity = 0;
        if(!TextUtils.isEmpty(productQuantity)){
            //Convert the String into an integer
            quantity = Integer.parseInt(productQuantity);
        }

        int price = 0;
        if(!TextUtils.isEmpty(productPrice)){
            //Convert the String into an integer
            price = Integer.parseInt(productPrice);
        }

        //Create a ContentValues object where column names are the keys,
        //and inventory attributes from thge editor are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        //Check to see if the URI is null or not to know if you are updating an existing product
        //or create a new one
        //If the URI != null it means that Im updating an existing pet
        if (mCurrentProductUri == null){
            //Insert a new product into the provider, returning the content URI for the new pet
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            //Add a toast message to confirm that the product was added successfully
            if(newUri == null){
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_activity_insert_fail), Toast.LENGTH_SHORT).show();
            }else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_activity_insert_success), Toast.LENGTH_SHORT).show();
            }
        }else {
            //Otherwise this is an EXISTING product and I will need to update the data
            //Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            //Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0){
                //if no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.editor_activity_update_fail), Toast.LENGTH_SHORT).show();
            }else{
                //Otherwise, the update was successful and we can display the toast
                Toast.makeText(this, getString(R.string.editor_activity_update_success), Toast.LENGTH_SHORT).show();
            }
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
                saveProduct();
                //exit the activity
                finish();
                //respond to the Delete menu option
            case R.id.action_delete:
                //TODO
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define projector for a single row in the db
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY
        };
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1){
            return;
        }
        //Proceed with moving to the first row of the cursor and reading data from it
        if(cursor.moveToFirst()){
            //find the columns of pet attributes that I'm interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

            //Extract the values from the cursor for the given index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            //set the values to the Views
            mProductNameEditText.setText(name);
            mProductPriceEditText.setText(Integer.toString(price));
            mProductQuantityEditText.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
    }
}
