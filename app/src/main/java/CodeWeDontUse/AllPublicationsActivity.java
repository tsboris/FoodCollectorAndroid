/*
package upp.foodonet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

        final Context context = this;

        // this is for user to be able to click on list elements
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
                Object item = listView.getItemAtPosition(position);
                try {
                    FCPublication pub = (FCPublication) item;
                    int pubId = pub.getUniqueId();
                    Log.i(MY_TAG, "item at Position :" + position + "  cklicked " + "Id = " + Integer.toString(pubId));
                    Bundle bundle = new Bundle();
                    bundle.putInt(FCPublication.PUBLICATION_UNIQUE_ID_KEY, pubId);
                    bundle.putCharSequence(FCPublication.PUBLICATION_TITLE_KEY, pub.getTitle());
                    bundle.putCharSequence(FCPublication.PUBLICATION_SUBTITLE_KEY, pub.getSubtitle());

                    Intent allPublicationsIntent = new Intent(context, MyPublicationDetailsActivity.class);
                    startActivity(allPublicationsIntent);

                } catch (Exception ex) {
                    Log.e(MY_TAG, "item at Position :" + position + "  cklicked. " + " Item is not FCPublication!");
                    return;
                }
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
*/
