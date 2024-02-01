package com.xsm;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.HashMap;

public class MyContentProvider extends ContentProvider {

    private HashMap<String, String> mDataHashMap;

    private static final String PROVIDER_NAME = "sensorData.provider";
    private static final String CONTENT_TRANSIT_URL = "content://" + PROVIDER_NAME;
    public static final Uri CONTENT_TRANSIT_URI = Uri.parse(CONTENT_TRANSIT_URL);

    @Override
    public boolean onCreate() {
        mDataHashMap = new HashMap<>();
        return true;
    }

    @Override

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (mDataHashMap.containsKey(selection)) {
            String[] columnNames = new String[]{selection};
            MatrixCursor matrixCursor = new MatrixCursor(columnNames);
            matrixCursor.addRow(new Object[]{mDataHashMap.get(selection)});
            return matrixCursor;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        for (String key : values.keySet()) {
            if (values.get(key) instanceof String) {
                mDataHashMap.put(key, (String) values.get(key));
                notifyChange();
            }
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mDataHashMap.containsKey(selection)) {
            mDataHashMap.remove(selection);
            return 0;
        }
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return -1;
    }

    private void notifyChange() {
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(CONTENT_TRANSIT_URI, null);
        }
    }
}