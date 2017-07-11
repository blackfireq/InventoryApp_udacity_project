package com.example.android.inventoryapp_udacity_project;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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


    /** Content URI for the existing item (null if it's a new item) */
    private Uri mCurrentItemUri;

    private static final int EXISTING_ITEM_LOADER = 1;

    //flag is changed if the user clicks on a field
    private boolean mItemHasChanged = false;

    // touch listner to let us know when they have clicked on a field
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private String mItemName;
    private int mItemPrice;
    private int mItemQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        //get intent info if any
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null){
            // This is a new Item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText)findViewById(R.id.edit_name_view);
        mPriceEditText = (EditText)findViewById(R.id.edit_price_view);
        mQuantityEditText = (EditText)findViewById(R.id.edit_quantity_view);

        //set the tochlisteners to the fields
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //add item to db
                addItem();
                //exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // trigger the show delete conf dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(ItemActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ItemActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addItem(){

        //check if any fields are empty and end the activity
        if(TextUtils.isEmpty( mNameEditText.getText().toString()) &&
                TextUtils.isEmpty(mPriceEditText.getText().toString()) &&
                TextUtils.isEmpty(mQuantityEditText.getText().toString())){
            return;
        } else {
            //get values from fields
            mItemName = mNameEditText.getText().toString().trim();
            mItemPrice = Integer.parseInt(mPriceEditText.getText().toString().trim());
            mItemQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        }

        //create object to collect data
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME,mItemName);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE,mItemPrice);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY,mItemQuantity);

        if(mCurrentItemUri == null){
            mCurrentItemUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        } else {
            String selection = InventoryEntry._ID;
            long currrentItemID = ContentUris.parseId(mCurrentItemUri);
            String[] selectionArgs = {Long.toString(currrentItemID)};
            long rowsUpdated = getContentResolver().update(mCurrentItemUri, values, selection, selectionArgs);
        }

        //create context for the toast to know what activity to display on
        Context context = getApplicationContext();

        // Show a toast message depending on whether or not the insertion was successful
        if (mCurrentItemUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteItem(){
        if(mCurrentItemUri != null){
            //run delete and get result back
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            //create context for the toast to know what activity to display on
            Context context = getApplicationContext();


            if (rowsDeleted > 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, rowsDeleted + " Rows Deleted",
                        Toast.LENGTH_SHORT).show();

            } else {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Failed to delete item",
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
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
        if (cursor.moveToFirst()) {
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
            mPriceEditText.setText(Integer.toString(currentPrice));
            mQuantityEditText.setText(Integer.toString(currentQuantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
