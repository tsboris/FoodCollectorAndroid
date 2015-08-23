package FooDoNetSQLClasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLHelper extends SQLiteOpenHelper {

    public static final String FC_DATABASE_NAME = "FoodCollector.db";
    public static final int FC_DATABASE_VERSION = 4;

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
}
