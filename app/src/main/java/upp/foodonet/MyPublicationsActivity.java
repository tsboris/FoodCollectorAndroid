package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class MyPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, IFooDoNetSQLCallback, AdapterView.OnItemClickListener {

    private static final String MY_TAG = "food_myPubsList";

    private int currentFilterID;

    SearchView src_all_pub_listView;
    Button btn_add_new_publication, btn_navigate_share, btn_navigate_take, btn_active_pub, btn_not_avtive_pub, btn_ending_pub;
    ListView lv_my_publications_list;
    Cursor cursor_my_publications;
    SimpleCursorAdapter adapter;
/*    ToggleButton tgl_btn_navigate_share;
      ToggleButton tgl_btn_navigate_take;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publications);

        src_all_pub_listView = (SearchView) findViewById(R.id.searchView1);
        btn_ending_pub = (Button) findViewById(R.id.btn_publication_ending);
        btn_not_avtive_pub = (Button) findViewById(R.id.btn_publication_notActive);
        btn_active_pub = (Button) findViewById(R.id.btn_publication_active);
        btn_add_new_publication = (Button) findViewById(R.id.btn_add_new_myPubsLst);
        btn_add_new_publication.setOnClickListener(this);
        btn_navigate_share = (Button) findViewById(R.id.btn_share_mypubs);
        btn_navigate_take = (Button) findViewById(R.id.btn_take_mypubs);
        //tgl_btn_navigate_share.setOnClickListener(this);
        btn_navigate_take.setOnClickListener(this);

        Drawable navigate_share = getResources().getDrawable(R.drawable.donate_v6_30x30);
        Drawable navigate_take = getResources().getDrawable(R.drawable.collect_v6_30x30);
        navigate_share.setBounds(0, 0, 30, 30);
        navigate_take.setBounds(0, 0, 30, 30);
        btn_navigate_share.setCompoundDrawables(null, navigate_share, null, null);
        btn_navigate_share.setCompoundDrawablePadding(10);
        btn_navigate_take.setCompoundDrawables(null, navigate_take, null, null);

        lv_my_publications_list = (ListView) findViewById(R.id.lv_my_publications_list);
        String[] from = new String[]{FCPublication.PUBLICATION_TITLE_KEY, FCPublication.PUBLICATION_NUMBER_OF_REGISTERED};
        int[] to = new int[]{R.id.tv_title_myPub_item, R.id.tv_subtitle_myPub_item};
        adapter = new SimpleCursorAdapter(this, R.layout.my_fcpublication_item, null, from, to);
        lv_my_publications_list.setAdapter(adapter);
        lv_my_publications_list.setOnItemClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // btn_navigate_take.setChecked(false);
        btn_navigate_take.setEnabled(true);
        btn_navigate_share.setEnabled(false);

        StartLoadingCursorForList(0);

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
        switch (v.getId()) {
            case R.id.btn_add_new_myPubsLst:
                Intent addNewPubIntent = new Intent(this, AddNewFCPublicationActivity.class);
                startActivityForResult(addNewPubIntent, 1);
                //AddNumberOfTestPublicationsToSql(5);
                //StartLoadingCursorForList(0);
                break;
            //case R.id.tgl_btn_share_mypubs:
            //    break;
            case R.id.btn_take_mypubs:
                Intent intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra("service", boundedService);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    // Vitaly 31/08/2015
    // this is temp code for checking list
    private static FCPublication GetNewIndexedPublicationForTest(Context context, int index) {
        String imei = CommonUtil.GetIMEI(context);
        Date sDate = new Date();
        Date eDate;
        Calendar c = Calendar.getInstance();
        c.setTime(sDate);
        c.add(Calendar.DATE, 5);
        eDate = c.getTime();
        FCPublication newPublication
                = new FCPublication(index, imei, "test pub " + index, "", "some address " + index,
                FCTypeOfCollecting.ContactPublisher, 0, 0,
                sDate, eDate, "", "", true);
        return newPublication;
    }

    private void AddNumberOfTestPublicationsToSql(int numberOfPublications) {
        for (int i = 0; i < numberOfPublications; i++)
            getContentResolver().insert(FooDoNetSQLProvider.CONTENT_URI,
                    GetNewIndexedPublicationForTest(this, i).GetContentValuesRow());
    }

    private void StartLoadingCursorForList(int filterTypeID) {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void RestartLoadingCursorForList(int filterTypeID) {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = FCPublication.GetColumnNamesForListArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this,
                FooDoNetSQLProvider.URI_GET_MY_PUBS_FOR_LIST_ID_DESC,
                projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case AddNewFCPublicationActivity.RESULT_OK:
                FCPublication publication
                        = (FCPublication) data.getExtras().get(AddNewFCPublicationActivity.PUBLICATION_KEY);
                if (publication == null) {
                    Log.i(MY_TAG, "got no pub from AddNew");
                    return;
                }
                if (publication.getUniqueId() == 0) {
                    int newNegativeID = 0;
                    Cursor negIdCursor = getContentResolver()
                            .query(FooDoNetSQLProvider.URI_GET_NEW_NEGATIVE_ID,
                                    new String[]{FCPublication.PUBLICATION_NEW_NEGATIVE_ID}, null, null, null);
                    if (negIdCursor.moveToFirst()) {
                        publication.setUniqueId(
                                negIdCursor.getInt(
                                        negIdCursor.getColumnIndex(FCPublication.PUBLICATION_NEW_NEGATIVE_ID)));
                        if(publication.getUniqueId() > 0) publication.setUniqueId(-1);
                    }
                }
/*
                getContentResolver().insert(FooDoNetSQLProvider.CONTENT_URI,
                        publication.GetContentValuesRow());
*/
                FooDoNetSQLExecuterAsync saveExecuter
                        = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                saveExecuter.execute(
                        new InternalRequest(
                                InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION, publication));
                break;
        }
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                RestartLoadingCursorForList(currentFilterID);
                break;
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                FCPublication result = request.publicationForDetails;
                Intent intent = new Intent(this, PublicationDetailsActivity.class);
                intent.putExtra(PublicationDetailsActivity.PUBLICATION_PARAM, result);
                startActivityForResult(intent, 1);
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = id;
        sqlGetPubAsync.execute(ir);
    }
}
