package upp.foodonet;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
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
{
    public static final String PUBLICATION_PARAM = "publication";

    private final String MY_TAG = "MyPublicationDetailsActivity";

    //region Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publication_details);

        try {
            Intent i = getIntent();
            this.publication = (FCPublication) i.getSerializableExtra("publication");
        } catch (Exception ex) {
            Log.e(MY_TAG, "error deserializing passed FCPublication: " + ex.getMessage());
            return;
        }

        if (publication.getTitle() != null) {
            this.setTitle(publication.getTitle());
            subtitleTextView = (TextView) findViewById(R.id.tv_subtitle);
            subtitleTextView.setText(publication.getTitle());
        }

        interestedPersonsCountTextView = (TextView) findViewById(R.id.interested_persons_count);
        postAddressTextView =            (TextView) findViewById(R.id.post_address);
        publicationDescriptionTextView = (TextView) findViewById(R.id.publication_description);

        interestedPersonsCountTextView.setText(getString(R.string.going_to_collect) + "  "
                + Integer.toString(publication.getRegisteredForThisPublication().size()));
        postAddressTextView.setText(publication.getAddress());
        publicationDescriptionTextView.setText(publication.getSubtitle());

        cancelPublicationButton        = (Button) findViewById(R.id.btn_cancel_publication);
        cancelPublicationButton.setText(getString(R.string.cancel_publication));

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

    private FCPublication publication;

    private TextView subtitleTextView;
    private TextView interestedPersonsCountTextView;
    private TextView postAddressTextView;
    private TextView publicationDescriptionTextView;
    private Button cancelPublicationButton;

}
