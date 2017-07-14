package com.example.android.inventoryapp_udacity_project.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mikem on 7/10/2017.
 */

public class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp_udacity_project";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "item";

    public static class InventoryEntry implements BaseColumns
    {
        /* The MIME type of the {@link #CONTENT_URI} for a list of items.  */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /* The MIME type of the {@link #CONTENT_URI} for a single item. */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //create constant for table name
        public static final String TABLE_NAME = "item";

        //create constants for the columns
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_IMAGE = "image";
    }
}
