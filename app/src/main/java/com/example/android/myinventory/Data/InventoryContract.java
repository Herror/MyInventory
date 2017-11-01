package com.example.android.myinventory.Data;

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
    public static final String PATH_INVENTORU = "myinventory";

    //To prevent someone from accidentally instantiating the contract class,
    //make the constructor private
    private InventoryContract(){}

    //Inner class that defines the table contents

    public static abstract class InvetoryEntry implements BaseColumns{
        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String PRODUCT_NAME = "name";
        public static final String PRODUCT_QUANTITY = "quantity";
        public static final String PRODUCT_PRICE = "price";

    }
}
