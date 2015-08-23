package upp.foodonet;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import DataModel.FCPublication;

import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class AllPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final String MY_TAG = "AllPublicationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(MY_TAG, "entering");

        getLoaderManager().initLoader(0, null, this);

        setContentView(R.layout.activity_all_publications);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.lv_all_publications_list);

        String[] columns = new String[] {FCPublication.PUBLICATION_TITLE_KEY, FCPublication.PUBLICATION_SUBTITLE_KEY};
        int[] to = new int[] {R.id.tv_title_myPub_item, R.id.tv_subtitle_myPub_item};

        adapter = new SimpleCursorAdapter(this, R.layout.my_fcpublication_item, null, columns, to, 0);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

    //region LoaderManager.LoaderCallbacks implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(MY_TAG, " onCreateLoader");
        String[] projection = FCPublication.GetColumnNamesArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.CONTENT_URI, projection, null, null, null);
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


    // region FooDoNetCustomActivityConnectedToService implementation
    @Override
    public void OnNotifiedToFetchData() {
//TODO
    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {
//TODO
    }

    @Override
    public void OnGooglePlayServicesCheckError() {
        // TODO
    }

    @Override
    public void OnInternetNotConnected() {
        // TODO
    }
    //endregion

    private ListView listView;
    private SimpleCursorAdapter adapter;
}
