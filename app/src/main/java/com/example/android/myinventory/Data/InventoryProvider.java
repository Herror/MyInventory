package com.example.android.myinventory.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;
import static android.R.attr.id;

/**
 * Created by enach on 11/3/2017.
 */

public class InventoryProvider extends ContentProvider {

    //Tag for the log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    //Global variable for the DB helper class
    private InventoryDbHelper mDbHelper;

    //Integer variables used for UriMatcher
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    //UriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /**
         * calls the addURI method and links it to the desired table with the desired
         * UriMatcher variable
         *(Authority, Table, Variable)
         * /# means a specific row in the table
         */
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        //Initialize the InventoryDbHelper
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //This cursor will hold the results of the query
        //For now I will set it to null
        Cursor cursor = null;

        //Figure out if the URI matcher can match the specific code of the URI
        //and create a switch statement to help with that
        int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PRODUCT_ID:
                //I extract out the ID for the URI
                //I replace selection with "=?" and the selection args with the desired number

                selection = InventoryContract.InventoryEntry._ID + "=?";
                //String.valueOf(ContentUris.parseId(uri)) - will get the actual number at
                //the end of the URI
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                // This will perform a query on the Inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification URI on the Cursor,
        //so we know what content URI the Cursor was created for.
        //If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Inseertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values){
        //Check to see if the name of the product is not null
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if(name == null){
            throw new IllegalArgumentException("Product requires a name");
        }
        //check and see if the price is valid
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if(price == null){
            throw new IllegalArgumentException("Product requires price");
        }else if(price < 0){
            throw new IllegalArgumentException("The price cannot be a negative value");
        }
        //check and see if the quantity is valid
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if(quantity == null){
            throw new IllegalArgumentException("Need to enter a quantity");
        }else if(quantity < 0){
            throw new IllegalArgumentException("You can not have a negative quantity");
        }

        //Create an object of the SQLiteDatabase class so I can write on it
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //Insert the new row with the entered information
        long newRow = database.insert(InventoryEntry.TABLE_NAME, null, values);

        //return null if it fails to insert a new row
        if(newRow == -1){
            Log.e(LOG_TAG, "Failes to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the inventory content uri
        getContext().getContentResolver().notifyChange(uri, null);

        //Once we know the new ID of the new row in the table, return the new URI
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
