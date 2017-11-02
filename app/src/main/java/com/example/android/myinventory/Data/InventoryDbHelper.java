package com.example.android.myinventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.myinventory.Data.InventoryContract.InventoryEntry;

/**
 * Created by enach on 11/1/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    //DB version. If the DB schema is changed, you must increment the DB version
    public static final int DATABASE_VERSION = 1;

    //name of the database file
    public static final String DATABASE_NAME = "inventory.db";

    //create the constructor
    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create a String that contains the SQL statement to create the table
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                        InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                        InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                        InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL)";

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
