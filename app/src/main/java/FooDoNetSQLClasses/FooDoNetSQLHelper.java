package FooDoNetSQLClasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLHelper extends SQLiteOpenHelper {

    private static final String MY_TAG = "food_SQLHelper";

    public static final String FC_DATABASE_NAME = "FoodCollector.db";
    public static final int FC_DATABASE_VERSION = 6;

    public static final int FILTER_ID_LIST_ALL_BY_ID = 0;
    public static final int FILTER_ID_LIST_MY_BY_ID = 1;

    public FooDoNetSQLHelper(Context context) {
        super(context, FC_DATABASE_NAME, null, FC_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FCPublicationsTable.onCreate(db);
        RegisteredForPublicationTable.onCreate(db);
        PublicationReportsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FCPublicationsTable.onUpgrade(db);
        RegisteredForPublicationTable.onUpgrade(db);
        PublicationReportsTable.onUpgrade(db);
    }

    public static String GetRawSelectPublicationsForListByFilterID(int filterID, String... params){
        switch (filterID){
            case FILTER_ID_LIST_ALL_BY_ID:
                return RAW_FOR_LIST_SELECT + RAW_FOR_LIST_FROM
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "!=", params[0])
                        + RAW_FOR_LIST_GROUP + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_UNIQUE_ID_KEY, false);
            case FILTER_ID_LIST_MY_BY_ID:
                return RAW_FOR_LIST_SELECT + RAW_FOR_LIST_FROM
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "=", params[0])
                        + RAW_FOR_LIST_GROUP + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_UNIQUE_ID_KEY, false);
            default:
                Log.e(MY_TAG, "unexpected filter id = " + filterID);
                return "";
        }
    }

    private static final String RAW_FOR_LIST_SELECT
            = "SELECT "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL + ", "
            + "COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") "
            + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED;

    private static final String RAW_FOR_LIST_FROM
            = " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME + " PUBS "
            + "LEFT JOIN " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME + " REGS "
            + "ON PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " = REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID;

    private static final String RAW_FOR_LIST_GROUP
            = " GROUP BY "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL;

    private static final String getRawForListOrderBy(String tableName, String fieldName, boolean isDesc){
        return " ORDER BY " + tableName + "." + fieldName + (isDesc? " DESC": " ASC");
    }

    private static final String getRawForListWhere(String tableName, String fieldName, String operator, String value){
        return " WHERE " + tableName + "." + fieldName + " " + operator + " '" + value + "'";
    }

    public static final String RAW_SELECT_FOR_LIST_ALL_PUBS_ID_DESC
            = "SELECT "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL + ", "
            + "COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") "
            + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED
            + " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME + " PUBS "
            + "LEFT JOIN " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME + " REGS "
            + "ON PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " = REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID
            + " GROUP BY "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL
            + " ORDER BY PUBS." +FCPublication.PUBLICATION_UNIQUE_ID_KEY + " DESC";

    public static final String RAW_SELECT_NEW_NEGATIVE_ID
            = " SELECT " + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " - 1 AS " + FCPublication.PUBLICATION_NEW_NEGATIVE_ID
            + " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME
            + " ORDER BY " + FCPublication.PUBLICATION_UNIQUE_ID_KEY + " LIMIT 1";


}
