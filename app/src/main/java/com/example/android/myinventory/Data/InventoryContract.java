package com.example.android.myinventory.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by enach on 11/1/2017.
 */

public final class InventoryContract {

    //create an entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.myinventory";

    //add the content:// scheme to the CONTENT_AUTHORITY to create the BASE_CONTENT_URI
    //This will be used to access the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //create a constant for the path of the pets table
    public static final String PATH_INVENTORY = "myinventory";

    //To prevent someone from accidentally instantiating the contract class,
    //make the constructor private
    private InventoryContract(){}

    //Inner class that defines the table contents

    public static abstract class InventoryEntry implements BaseColumns{

        //Create the content URI to access the inventory data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_PRICE = "price";
    }

    //The MIME type of the CONTENT_URI for a list of products of the inventory
    public static final String CONTENT_LIST_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
    //The MIME type of the CONTENT_URI for a single product pf the inventory
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
}
