package com.example.android.inventoryapp_udacity_project;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.inventoryapp_udacity_project.data.InventoryContract;
import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;

/**
 * Created by mikem on 7/10/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter{

    public InventoryCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //getters

        //get a link to the name Textview
        TextView nameView = (TextView)view.findViewById(R.id.item_name_view);

        //get a link to the price Textview
        TextView priceView = (TextView)view.findViewById(R.id.item_price_view);

        //get a link to the quantity Textview
        TextView quantityView = (TextView)view.findViewById(R.id.item_quantity_view);

        //find the columns of the item name
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);

        //find the columns of the item name
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);

        //find the columns of the item name
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        //get current row name
        String currentName = cursor.getString(nameColumnIndex);

        //get current row price
        String currentPrice = cursor.getString(priceColumnIndex);

        //get current row quantity
        String currentQuantity = cursor.getString(quantityColumnIndex);

        //setters

        //set item name in view
        nameView.setText(currentName);

        //set item price in view
        priceView.setText(currentPrice);

        //set item quantity in view
        quantityView.setText(currentQuantity);
    }
}
