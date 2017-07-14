package com.example.android.inventoryapp_udacity_project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;

/**
 * Created by mikem on 7/10/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper{

    /** Name of the database file*/
    public static final String DATABASE_NAME = "inventory.db";

    /**  Databse version. If you change the database schema, you must increment the database version */
    public static final int DATABASE_VERSION = 1;
    //create item table command
    public static final String SQL_CREATE_ITEM_TABLE =
            "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                    InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                    InventoryEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                    InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0" +
                    InventoryEntry.COLUMN_ITEM_IMAGE + "TEXT )";
    //Drop item table
    private static final String SQL_DELETE_ITEM_TABLE =
            "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ITEM_TABLE);
        onCreate(db);
    }
}
