package com.example.android.inventoryapp_udacity_project;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp_udacity_project.data.InventoryContract;
import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;

import static android.R.attr.id;

/**
 * Created by mikem on 7/10/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter{

    //get current row id
    int currentId;;

    //get current row name
    String currentName;

    //get current row price
    String currentPrice;

    //get current row quantity
    String currentQuantity;

    Context mContext;

    public InventoryCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        mContext = context;

        //getters

        // product id to identify items in ListView
        long id = cursor.getLong(cursor.getColumnIndex(InventoryEntry._ID));

        final Uri mCurrentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        //get a link to the name Textview
        TextView nameView = (TextView)view.findViewById(R.id.item_name_view);

        //get a link to the price Textview
        TextView priceView = (TextView)view.findViewById(R.id.item_price_view);

        //get a link to the quantity Textview
        final TextView quantityView = (TextView)view.findViewById(R.id.item_quantity_view);

        //get a link to the sale Textview
        TextView saleView = (TextView)view.findViewById(R.id.item_sale_view);

        //find the columns of the item name
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);

        //find the columns of the item name
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);

        //find the columns of the item name
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);

        //find the columns of the item name
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

        //get current row id
        currentId = cursor.getInt(idColumnIndex);

        //get current row name
        currentName = cursor.getString(nameColumnIndex);

        //get current row price
        currentPrice = cursor.getString(priceColumnIndex);

        //get current row quantity
        currentQuantity = cursor.getString(quantityColumnIndex);

        //setters

        //set item name in view
        nameView.setText(currentName);

        //set item price in view
        priceView.setText(currentPrice);

        //set item quantity in view
        quantityView.setText(currentQuantity);

        //set onclick for the sale button
        saleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get current Quantity
                String[] projection = {InventoryEntry.COLUMN_ITEM_QUANTITY};
                Cursor cursor = mContext.getContentResolver().query(mCurrentUri, projection, null, null, null);
                if(cursor.moveToFirst()){
                    currentQuantity = Integer.toString(cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY)));
                }
                //check if the the quantity is above zero
                int quantityInt = Integer.parseInt(currentQuantity);
                if (quantityInt > 0) {
                    currentQuantity = Integer.toString(Integer.parseInt(currentQuantity) - 1);
                }
                Log.v("Qman",currentQuantity);
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, currentQuantity);

                String selection = "_id = ?";
                String[] selectionArgs = {Integer.toString(currentId)};
                Log.v("QId",mCurrentUri.toString());
                int rowsUpdated = mContext.getContentResolver().update(mCurrentUri, values, selection, selectionArgs);
                Log.v("Qrow",rowsUpdated + " updated");
            }
        });
    }
}
