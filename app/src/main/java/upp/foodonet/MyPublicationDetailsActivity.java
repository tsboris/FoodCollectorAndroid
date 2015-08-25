package upp.foodonet;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class MyPublicationDetailsActivity extends FooDoNetCustomActivityConnectedToService
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final String MY_TAG = "MyPublicationDetailsActivity";

    //region Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publication_details);

        publicationID = savedInstanceState.getInt(FCPublication.PUBLICATION_UNIQUE_ID_KEY);
        String publicationTitle = savedInstanceState.getString(FCPublication.PUBLICATION_TITLE_KEY);
        String publicationSubtitle = savedInstanceState.getString(FCPublication.PUBLICATION_SUBTITLE_KEY);

        if (publicationTitle!=null)  setTitle(publicationTitle);

        subtitleTextView = (TextView) findViewById(R.id.tv_subtitle);
        subtitleTextView.setText(publicationSubtitle);

        interestedPersonsCountTextView = (TextView) findViewById(R.id.interested_persons_count);
        postAddressTextView =            (TextView) findViewById(R.id.post_address);
        publicationDescriptionTextView = (TextView) findViewById(R.id.publication_description);

        cancelPublicationButton        = (Button) findViewById(R.id.btn_cancel_publication);
        cancelPublicationButton.setText(R.string.cancel_publication);

        // Show Alert
        cancelPublicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " cancelPublicationButton  clicked!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_publication_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region FooDoNetCustomActivityConnectedToService implementation
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
        //TODO
    }

    @Override
    public void OnInternetNotConnected() {
        //TODO
    }
    //endregion

    private int publicationID;

    private TextView subtitleTextView;
    private TextView interestedPersonsCountTextView;
    private TextView postAddressTextView;
    private TextView publicationDescriptionTextView;
    private Button cancelPublicationButton;


    //region LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(MY_TAG, " onCreateLoader");

        String[] projection = FCPublication.GetColumnNamesArray();
        CursorLoader cursorLoader = new CursorLoader(this,
                FooDoNetSQLProvider.CONTENT_URI,
                projection,
               FCPublication.PUBLICATION_UNIQUE_ID_KEY + " = ?",
                new String[] {Integer.toString(publicationID)},
                null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(MY_TAG, " onLoadFinished");
        ArrayList<FCPublication> pubArr = FCPublication.GetArrayListOfPublicationsFromCursor(data);
        if (pubArr == null || pubArr.size() > 1 || pubArr.size() == 0 ) {
            Log.e(MY_TAG, "error getting data for publication " + Integer.toString(publicationID));
            return;
        }

        FCPublication pub = pubArr.get(0);

        setTitle(pub.getTitle());
        subtitleTextView.setText(pub.getSubtitle());
        interestedPersonsCountTextView.setText(R.string.going_to_collect + "  "
                + Integer.toString(pub.getRegisteredForThisPublication().size()));
        postAddressTextView.setText(pub.getAddress());
        publicationDescriptionTextView.setText(pub.getSubtitle());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    //endregion
}
