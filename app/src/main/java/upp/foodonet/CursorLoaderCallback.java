package upp.foodonet;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.CursorLoader;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import DataModel.FCPublication;

/**
 * Created by ah on 23/08/15.
 */
public class CursorLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

    public CursorLoaderCallback(Context context, SimpleCursorAdapter adapter)
    {
        this.context = context;
        this.adapter = adapter;
    }

    private final String MY_TAG = "CursorLoaderCallback";
    //region LoaderManager.LoaderCallbacks implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(MY_TAG, " onCreateLoader");
        String[] projection = FCPublication.GetColumnNamesArray();
        CursorLoader cursorLoader
                = new CursorLoader(context, FooDoNetSQLProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(MY_TAG, " swapping cursor");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(MY_TAG, " onLoaderReset");
        adapter.swapCursor(null);
    }
    //endregion

    private SimpleCursorAdapter adapter;
    private Context context;
}
