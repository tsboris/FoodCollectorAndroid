package upp.foodonet;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class MyPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener {

    Button btn_add_new_publication;
    ToggleButton tgl_btn_navigate_share;
    ToggleButton tgl_btn_navigate_take;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publications);

        btn_add_new_publication = (Button)findViewById(R.id.btn_add_new_myPubsLst);
        btn_add_new_publication.setOnClickListener(this);
        tgl_btn_navigate_share = (ToggleButton)findViewById(R.id.tgl_btn_share_mypubs);
        tgl_btn_navigate_take = (ToggleButton)findViewById(R.id.tgl_btn_take_mypubs);
        //tgl_btn_navigate_share.setOnClickListener(this);
        tgl_btn_navigate_take.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tgl_btn_navigate_take.setChecked(false);
        tgl_btn_navigate_take.setEnabled(true);
        tgl_btn_navigate_share.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_publications, menu);
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

    @Override
    public void OnNotifiedToFetchData() {

    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {

    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_new_myPubsLst:
                Intent addNewPubIntent = new Intent(this, AddNewFCPublicationActivity.class);
                startActivityForResult(addNewPubIntent, 1);
                break;
            //case R.id.tgl_btn_share_mypubs:
            //    break;
            case R.id.tgl_btn_take_mypubs:
                Intent intent = new Intent(this, MapAndListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }
}
