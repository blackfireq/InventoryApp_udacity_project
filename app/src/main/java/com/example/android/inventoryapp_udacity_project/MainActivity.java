package com.example.android.inventoryapp_udacity_project;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp_udacity_project.data.InventoryContract;
import com.example.android.inventoryapp_udacity_project.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //set up default cursor
    InventoryCursorAdapter mCursorAdapter;

    //cursorLoader ID
    private static final int ITEM_LOADER_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                startActivity(intent);
            }
        });

        // find the ListView to populate
        ListView itemListView = (ListView)findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        // create empty Adapter for the loader to populate
        mCursorAdapter = new InventoryCursorAdapter(this,null);
        itemListView.setAdapter(mCursorAdapter);

        //Setup item click listner
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent editItemIntent = new Intent(MainActivity.this,ItemActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                //Set the URI on the data field of the intent
                editItemIntent.setData(currentItemUri);

                startActivity(editItemIntent);
            }
        });

        //start cursor loader
        getLoaderManager().initLoader(ITEM_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY
        };

        CursorLoader cursor = new CursorLoader(
                this,                               // the Context
                InventoryEntry.CONTENT_URI,         // The Content URI of the Item table
                projection,                         // The columns to return for each row
                null,                               // Selection Critera
                null,                               // Selection Args Critera
                null                                // Sort order for the returned rows
        );

        return cursor;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
