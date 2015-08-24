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
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;
import FooDoNetSQLClasses.FCPublicationsTable;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.RegisteredForPublicationTable;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLProvider extends ContentProvider {

    private FooDoNetSQLHelper database;

    private static final String MY_TAG = "food_contentProvider";

    private static final int PUBLICATIONS = 10;
    private static final int PUBLICATION_ID = 20;
    private static final int PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC = 30;

    private static final String AUTHORITY = "foodonet.foodcollector.sqlprovider";

    private static final String BASE_PATH = "foodonet";

    private static final String ALL_PUBS_FOR_LIST_ID_DESC_PATH = "/Pubs_ALL_for_list_id_desc";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final Uri URI_GET_ALL_PUBS_FOR_LIST_ID_DESC
            = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH+ ALL_PUBS_FOR_LIST_ID_DESC_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/publications";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/publication";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PUBLICATIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PUBLICATION_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + ALL_PUBS_FOR_LIST_ID_DESC_PATH, PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC);
    }

    @Override
    public boolean onCreate() {
        database = new FooDoNetSQLHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int uriType = sURIMatcher.match(uri);
        checkColumns(projection, uriType);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case PUBLICATIONS:
                queryBuilder.setTables(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME);// + "," + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME
                break;
            case PUBLICATION_ID:
                queryBuilder.setTables(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME);// + "," + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME
                queryBuilder.appendWhere(FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + uri.getLastPathSegment());
                break;
            case PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC:
                return database.getReadableDatabase().rawQuery(FooDoNetSQLHelper.RAW_SELECT_FOR_LIST_ALL_PUBS_ID_DESC, null);
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

    private void checkColumns(String[] projection, int action) {
        String[] available;
        switch (action){
            case PUBLICATIONS:
            case PUBLICATION_ID:
                available = FCPublication.GetColumnNamesArray();
                break;
            case PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC:
                available = FCPublication.GetColumnNamesForListArray();
                break;
            default:
                Log.e(MY_TAG, "checkColumns got bad parameter action");
                available = new String[]{};
                break;
        }
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
