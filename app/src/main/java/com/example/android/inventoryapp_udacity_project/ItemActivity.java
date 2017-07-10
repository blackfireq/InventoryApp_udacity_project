package com.example.android.inventoryapp_udacity_project;

import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;

import static android.R.attr.data;
import static android.R.attr.name;

public class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** EditText field to enter the item name */
    private EditText mNameEditText;

    /** EditText field to enter the item price */
    private EditText mPriceEditText;

    /** EditText field to enter the item quantity */
    private EditText mQuantityEditText;


    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri mCurrentItemUri;

    private static final int EXISTING_ITEM_LOADER = 1;

    //flag is changed if the user clicks on a field
    private boolean mItemHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY
        };

        if (mCurrentItemUri != null){
            CursorLoader cursor = new CursorLoader(
                    this,                               // the Context
                    mCurrentItemUri,                    // The Content URI of the item table
                    projection,                         // The columns to return for each row
                    null,                               // Selection Critera
                    null,                               // Selection Args Critera
                    null                                // Sort order for the returned rows
            );
            return cursor;
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

        // Extract out the value from the Cursor for the given column index
        String currentName = cursor.getString(nameColumnIndex);
        int currentPrice = cursor.getInt(priceColumnIndex);
        int currentQuantity = cursor.getInt(quantityColumnIndex);

        // Update the views on the screen with the values from the database
        mNameEditText.setText(currentName);
        mPriceEditText.setText(currentPrice);
        mQuantityEditText.setText(currentQuantity);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
