package upp.foodonet;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
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
{
    private final String MY_TAG = "AllPublicationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(MY_TAG, "entering");

        setContentView(R.layout.activity_all_publications);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.lv_all_publications_list);

        // arranging data supply: initialaze adapter and connect to listview
        String[] columns = new String[] {FCPublication.PUBLICATION_TITLE_KEY, FCPublication.PUBLICATION_SUBTITLE_KEY};
        int[] to = new int[] {R.id.tv_title_myPub_item, R.id.tv_subtitle_myPub_item};
        adapter = new SimpleCursorAdapter(this, R.layout.my_fcpublication_item, null, columns, to, 0);
        listView.setAdapter(adapter);

        // starting cursor loader
        loaderCallback = new CursorLoaderCallback(this, adapter);
        getLoaderManager().initLoader(0, null, loaderCallback);

        // this is for user to be able to click on list elements
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = listView.getItemAtPosition(position).toString();

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

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
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallback;
}
