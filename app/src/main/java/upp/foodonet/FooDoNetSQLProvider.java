package upp.foodonet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import DataModel.FCPublication;
import FooDoNetSQLClasses.FCPublicationsTable;
import FooDoNetSQLClasses.FooDoNetSQLHelper;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLProvider extends ContentProvider {

    private FooDoNetSQLHelper database;

    private static final int PUBLICATIONS = 10;
    private static final int PUBLICATION_ID = 20;

    private static final String AUTHORITY = "foodonet.foodcollector.sqlprovider";

    private static final String BASE_PATH = "foodonet";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/publications";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/publication";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PUBLICATIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PUBLICATION_ID);
    }

    @Override
    public boolean onCreate() {
        database = new FooDoNetSQLHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        checkColumns(projection);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME);
        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case PUBLICATIONS:
                break;
            case PUBLICATION_ID:
                queryBuilder.appendWhere(FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case PUBLICATIONS:
                id = db.insert(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType){
            case PUBLICATIONS:
                rowsDeleted = db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME, selection, selectionArgs);
                break;
            case PUBLICATION_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsDeleted = db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType){
            case PUBLICATIONS:
                rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case PUBLICATION_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            values, FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id, null);
                } else {
                    rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            values, FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id
                                    + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = FCPublication.GetColumnNamesArray();
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
