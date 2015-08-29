package FooDoNetSQLClasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLHelper extends SQLiteOpenHelper {

    public static final String FC_DATABASE_NAME = "FoodCollector.db";
    public static final int FC_DATABASE_VERSION = 5;

    public FooDoNetSQLHelper(Context context) {
        super(context, FC_DATABASE_NAME, null, FC_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FCPublicationsTable.onCreate(db);
        RegisteredForPublicationTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FCPublicationsTable.onUpgrade(db);
        RegisteredForPublicationTable.onUpgrade(db);
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
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL
            + " ORDER BY PUBS." +FCPublication.PUBLICATION_UNIQUE_ID_KEY + " DESC";


}
