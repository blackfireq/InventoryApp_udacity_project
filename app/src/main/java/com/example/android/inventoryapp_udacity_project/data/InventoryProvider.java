package com.example.android.inventoryapp_udacity_project.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventoryapp_udacity_project.data.InventoryDbHelper;
import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;
import java.net.URI;

import static android.R.attr.id;
import static android.R.attr.selectable;

/**
 * Created by mikem on 7/10/2017.
 */

public class InventoryProvider  extends ContentProvider{

    /// Tag for the log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // URI matcher code for the content URI for the item table
    private static final int ITEM = 100;

    // URI matcher code for the content URI for a single item in the item table
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY,ITEM);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    //Database helper object
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return false;
    }


    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                //query whole table
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case ITEM_ID:
                //query row by Id
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public String getType( Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Override
    public Uri insert( Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){

            case ITEM:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }



    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch(match) {

            case ITEM:
                return updateItem(uri, contentValues, selection, selectionArgs);

            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // HELPER METHODS

    private Uri insertItem (Uri uri, ContentValues values){
        // Create connection to the database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Item requires a name");
        }

        // Check that the price is not null
        int price = Integer.parseInt(values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE));
        if (TextUtils.isEmpty(Integer.toString(price))) {
            throw new IllegalArgumentException("Item requires a Price");
        }

        // Check that the quanity is not null
        int quantity = Integer.parseInt(values.getAsString(InventoryEntry.COLUMN_ITEM_NAME));
        if (TextUtils.isEmpty(Integer.toString(quantity))) {
            quantity = 0;
        }

        // insert item and get the row id
        long rowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, rowId);
    }


    private int updateItem (Uri uri, ContentValues values, String selection, String[] selectionArgs ){

        // Create connection to the database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Item requires a name");
        }

        // Check that the price is not null
        int price = Integer.parseInt(values.getAsString(InventoryEntry.COLUMN_ITEM_PRICE));
        if (TextUtils.isEmpty(Integer.toString(price))) {
            throw new IllegalArgumentException("Item requires a Price");
        }

        // Check that the quanity is not null
        int quantity = Integer.parseInt(values.getAsString(InventoryEntry.COLUMN_ITEM_NAME));
        if (TextUtils.isEmpty(Integer.toString(quantity))) {
            throw new IllegalArgumentException("Item requires a quantity");
        }

        //run update and get number of rows
        int numOfRows = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (numOfRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numOfRows;
    }

}
