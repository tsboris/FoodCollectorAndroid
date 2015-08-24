package upp.foodonet;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class MyPublicationDetailsActivity extends FooDoNetCustomActivityConnectedToService {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publication_details);

        interestedPersonsCountTextView = (TextView) findViewById(R.id.interested_persons_count);
        postAddressTextView =            (TextView) findViewById(R.id.post_address);
        publicationDescriptionTextView = (TextView) findViewById(R.id.publication_description);
        cancelPublicationButton        = (Button) findViewById(R.id.btn_cancel_publication);
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

    private TextView interestedPersonsCountTextView;
    private TextView postAddressTextView;
    private TextView publicationDescriptionTextView;
    private Button cancelPublicationButton;
}
