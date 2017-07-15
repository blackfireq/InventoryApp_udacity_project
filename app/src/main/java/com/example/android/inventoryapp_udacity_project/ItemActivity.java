package com.example.android.inventoryapp_udacity_project;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.android.inventoryapp_udacity_project.data.InventoryProvider.LOG_TAG;

public class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /* identifier for the camera capture */
    private static final int REQUEST_TAKE_PHOTO = 1;
    /* Indentifier for cursorloader */
    private static final int EXISTING_ITEM_LOADER = 2;
    String mImageTest;
    /* EditText field to enter the item name */
    private EditText mNameEditText;
    /* EditText field to enter the item price */
    private EditText mPriceEditText;
    /* EditText field to enter the item quantity */
    private EditText mQuantityEditText;
    /* ImageView field to display Item image*/
    private ImageView mItemImage;
    /* Button to send email intent to order from supplier */
    private Button mOrderFromSupplier;
    /* Used for image path*/
    private String mCurrentPhotoPath;
    /* Content URI for the existing item (null if it's a new item) */
    private Uri mCurrentItemUri;
    /* flag is changed if the user clicks on a field */
    private boolean mItemHasChanged = false;
    /* used for the image uri */
    private Uri mPhotoUri;

    // touch listner to let us know when they have clicked on a field
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        /* TextView field to decrease item quantity */
        TextView mQuantityEditTextMinus;
        /* TextView field to increase item quantity */
        TextView mQuantityEditTextPlus;

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
        mQuantityEditTextMinus = (TextView)findViewById(R.id.edit_quantity_minus);
        mQuantityEditTextPlus = (TextView)findViewById(R.id.edit_quantity_plus);
        mOrderFromSupplier = (Button)findViewById(R.id.buy_more);
        mItemImage = (ImageView)findViewById(R.id.item_image_view);

        //set the tochlisteners to the fields
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mQuantityEditTextMinus.setOnTouchListener(mTouchListener);
        mQuantityEditTextPlus.setOnTouchListener(mTouchListener);
        mItemImage.setOnTouchListener(mTouchListener);

        //set onclick action to decrease quantity
        mQuantityEditTextMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get current quantity
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                //update quantity
                if(quantity > 0) {
                    quantity--;
                }
                //set quantity view
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });
        //set onclick action to increase quantity
        mQuantityEditTextPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity;
                //get current quantity

                //check if the quantity is empty
                if(TextUtils.isEmpty(mQuantityEditText.getText().toString())){
                    quantity =0;
                } else{
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                //update quantity
                quantity++;
                //set quantity view
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });
        //send email to vendor to order
        mOrderFromSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "jdoe@example.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New order");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "body text please update");

                //check for an app that can process the intent
                PackageManager packageManager = getPackageManager();
                List activities = packageManager.queryIntentActivities(emailIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                if(isIntentSafe) {
                    startActivity(emailIntent);
                } else {
                    Toast.makeText(ItemActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //update photo from camera
        mItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });




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

    //prompt dialog to confirm deletion
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

    // add item, display toast if sucessfull or not
    private void addItem(){
        String mItemName;
        float mItemPrice;
        int mItemQuantity;

        //check if any fields are empty and end the activity
        if(TextUtils.isEmpty( mNameEditText.getText().toString()) &&
                TextUtils.isEmpty(mPriceEditText.getText().toString()) &&
                TextUtils.isEmpty(mQuantityEditText.getText().toString())){
            return;
        } else {
            //get values from fields
            mItemName = mNameEditText.getText().toString().trim();
            mItemPrice = Float.parseFloat(mPriceEditText.getText().toString().trim());
            mItemQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            mCurrentPhotoPath = mPhotoUri.toString();
        }

        //create object to collect data
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME,mItemName);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE,mItemPrice);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY,mItemQuantity);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE,mCurrentPhotoPath);

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

    // delete item, display toast if sucessfull or not
    private void deleteItem(){
        if(mCurrentItemUri != null){
            //run delete and get result back
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

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

    //create image file used for storing image captured by camera
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        URI image_uri = image.toURI();
        mImageTest = image_uri.toString();
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // take a picture for the item and
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(LOG_TAG, "Problem with creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mPhotoUri = FileProvider.getUriForFile(this,
                        "com.example.android.inventoryapp_udacity_project.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private Bitmap getBitmapFromUri (Uri uri){
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap mBitmap = getBitmapFromUri(mPhotoUri);
            mItemImage.setImageBitmap(mBitmap);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_IMAGE
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
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String currentName = cursor.getString(nameColumnIndex);
            float currentPrice = cursor.getFloat(priceColumnIndex);
            int currentQuantity = cursor.getInt(quantityColumnIndex);
            Uri currentImage = Uri.parse(cursor.getString(imageColumnIndex));


            // Update the views on the screen with the values from the database
            mNameEditText.setText(currentName);
            mPriceEditText.setText(Float.toString(currentPrice));
            mQuantityEditText.setText(Integer.toString(currentQuantity));
            Bitmap bm = getBitmapFromUri(currentImage);
            mItemImage.setImageBitmap(bm);

            //show button for existing items
            mOrderFromSupplier.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
