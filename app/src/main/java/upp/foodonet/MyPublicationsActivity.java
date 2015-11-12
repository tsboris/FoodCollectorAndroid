package upp.foodonet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import android.widget.SimpleCursorAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.google.android.gms.maps.model.LatLng;

import Adapters.PublicationsListCursorAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.GetMyLocationAsync;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;


public class MyPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, IFooDoNetSQLCallback {
    //, IFooDoNetSQLCallback, IFooDoNetServerCallback {

    private static final String MY_TAG = "food_myPubs";

    private int currentFilterID = 0;
    ListView lv_my_publications_list;
    Cursor cursor_my_publications;
    //SimpleCursorAdapter adapter;
    PublicationsListCursorAdapter adapter;

    //SearchView src_all_pub_listView;
    Button btn_add_new_publication, btn_navigate_share, btn_navigate_take, btn_active_pub, btn_not_active_pub, btn_ending_pub;
    //Animation animZoomIn;

    //ProgressDialog progressDialog;

/*    ToggleButton tgl_btn_navigate_share;
      ToggleButton tgl_btn_navigate_take;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publications);

        //src_all_pub_listView = (SearchView) findViewById(R.id.searchView1);
        btn_ending_pub = (Button) findViewById(R.id.btn_publication_ending);
        btn_ending_pub.setOnClickListener(this);
        btn_not_active_pub = (Button) findViewById(R.id.btn_publication_notActive);
        btn_not_active_pub.setOnClickListener(this);
        btn_active_pub = (Button) findViewById(R.id.btn_publication_active);
        btn_active_pub.setOnClickListener(this);
        btn_add_new_publication = (Button) findViewById(R.id.btn_add_new_myPubsLst);
        btn_add_new_publication.setOnClickListener(this);
        btn_navigate_share = (Button) findViewById(R.id.btn_share_mypubs);
        btn_navigate_take = (Button) findViewById(R.id.btn_take_mypubs);
        //tgl_btn_navigate_share.setOnClickListener(this);
        btn_navigate_take.setOnClickListener(this);
        //animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);

        int dimenID = 0;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                dimenID = getResources().getDimensionPixelSize(R.dimen.bottom_nav_btn_img_size_ldpi);
                break;
            case DisplayMetrics.DENSITY_HIGH:
                dimenID = getResources().getDimensionPixelSize(R.dimen.bottom_nav_btn_img_size_hdpi);
                break;
            default:
                dimenID = getResources().getDimensionPixelSize(R.dimen.bottom_nav_btn_img_size);
                break;
        }
        Drawable navigate_share = getResources().getDrawable(R.drawable.donate_v62x_60x60);
        Drawable navigate_take = getResources().getDrawable(R.drawable.collect_v6_60x60);
        navigate_share.setBounds(0, 0, dimenID, dimenID);
        navigate_take.setBounds(0, 0, dimenID, dimenID);
        btn_navigate_share.setCompoundDrawables(null, navigate_share, null, null);
        //  btn_navigate_share.setCompoundDrawablePadding(10);
        btn_navigate_take.setCompoundDrawables(null, navigate_take, null, null);

        lv_my_publications_list = (ListView) findViewById(R.id.lv_my_publications_list);
        //String[] from = new String[]{FCPublication.PUBLICATION_TITLE_KEY, FCPublication.PUBLICATION_UNIQUE_ID_KEY};
        //int[] to = new int[]{R.id.tv_title_myPub_item, R.id.tv_subtitle_myPub_item};
/*
        GetMyLocationAsync locationAsync = new GetMyLocationAsync((LocationManager) getSystemService(LOCATION_SERVICE), this);
        locationAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
*/
        //StartGetMyLocation();
        adapter = new PublicationsListCursorAdapter(this, null, 0, null, true);
        lv_my_publications_list.setAdapter(adapter);
        lv_my_publications_list.setOnItemClickListener(this);
        onClick(btn_active_pub);

    }


    @Override
    protected void onStart() {
        super.onStart();
        // btn_navigate_take.setChecked(false);
        btn_navigate_take.setEnabled(true);
        btn_navigate_share.setEnabled(false);
        StartLoadingCursorForList(currentFilterID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
            StartLoadingCursorForList(currentFilterID);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_publications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        progressDialog = CommonUtil.ShowProgressDialog(this, "");
        Intent intent = new Intent(this, MapAndListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
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
                if (!CheckInternetForAction(getString(R.string.action_add_new_publication)))
                    return;
                Intent addNewPubIntent = new Intent(this, AddEditPublicationActivity.class);
                startActivityForResult(addNewPubIntent, 1);
                break;
            case R.id.btn_take_mypubs:
                Intent intent = new Intent(this, MapAndListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.btn_publication_ending:
                btn_ending_pub.setEnabled(false);
                btn_ending_pub.setTextColor(getResources().getColor(R.color.inactive_blue));
                btn_not_active_pub.setEnabled(true);
                btn_not_active_pub.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_active_pub.setEnabled(true);
                btn_active_pub.setTextColor(getResources().getColor(R.color.basic_blue));
                if (currentFilterID == FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON)
                    RestartLoadingCursorForList();
                else {
                    adapter.swapCursor(null);
                    currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON;
                    StartLoadingCursorForList(currentFilterID);
                }
                break;
            case R.id.btn_publication_active:
                btn_ending_pub.setEnabled(true);
                btn_ending_pub.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_not_active_pub.setEnabled(true);
                btn_not_active_pub.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_active_pub.setEnabled(false);
                btn_active_pub.setTextColor(getResources().getColor(R.color.inactive_blue));
                if (currentFilterID == FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC)
                    RestartLoadingCursorForList();
                else {
                    adapter.swapCursor(null);
                    currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC;
                    StartLoadingCursorForList(currentFilterID);
                }
                break;
            case R.id.btn_publication_notActive:
                btn_ending_pub.setEnabled(true);
                btn_ending_pub.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_not_active_pub.setEnabled(false);
                btn_not_active_pub.setTextColor(getResources().getColor(R.color.inactive_blue));
                btn_active_pub.setEnabled(true);
                btn_active_pub.setTextColor(getResources().getColor(R.color.basic_blue));
                if (currentFilterID == FooDoNetSQLHelper.FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC)
                    RestartLoadingCursorForList();
                else {
                    adapter.swapCursor(null);
                    currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC;
                    StartLoadingCursorForList(currentFilterID);
                }
                break;
        }
    }

    private void StartLoadingCursorForList(int filterTypeID) {
        getSupportLoaderManager().initLoader(filterTypeID, null, this);
    }

    private void RestartLoadingCursorForList() {//int filterTypeID) {
        getSupportLoaderManager().restartLoader(currentFilterID, null, this);
//        onLoaderReset(null);
//        StartLoadingCursorForList(filterTypeID);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = FCPublication.GetColumnNamesForListArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this,
                Uri.parse(FooDoNetSQLProvider.URI_GET_PUBS_FOR_LIST_BY_FILTER_ID + "/" + id),
                projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && adapter != null) {
            adapter.swapCursor(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null)
            adapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        switch (resultCode) {
            case AddEditPublicationActivity.RESULT_OK:
                int action = data.getIntExtra(PublicationDetailsActivity.DETAILS_ACTIVITY_RESULT_KEY, -1);
                switch (action) {
                    case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_saving));
                        FCPublication publication
                                = (FCPublication) data.getExtras().get(AddEditPublicationActivity.PUBLICATION_KEY);
                        if (publication == null) {
                            Log.i(MY_TAG, "got no pub from AddNew");
                            return;
                        }
                        //=============>
                        AddEditPublicationService.StartSaveNewPublication(getApplicationContext(), publication);
                        break;
                    case InternalRequest.ACTION_DELETE_PUBLICATION:
                    case InternalRequest.ACTION_NO_ACTION:
                    default:
                        if(adapter != null){
                            //adapter.swapCursor(null);
                            RestartLoadingCursorForList();
                        }
                        break;

                }


                break;
        }
    }

    @Override
    public void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_SUCCESS:
                Location location = (Location) intent.getParcelableExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_LOCATION_KEY);
                if (location == null) {
                    Log.e(MY_TAG, "got null location extra from broadcast");
                    return;
                }
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_FAIL:
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_COMPLETE:
            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_SQL_SUCCESS:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            default:
                if (adapter != null){
                    //adapter.swapCursor(null);
                    RestartLoadingCursorForList();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(id<0){
            Toast.makeText(this, getString(R.string.new_pub_waiting_for_save_on_server), Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = id;
        sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                FCPublication result = request.publicationForDetails;
                if (result == null)
                    Log.e(MY_TAG, "OnSQLTaskComplete got null request.publicationForDetails");
                String myIMEI = CommonUtil.GetIMEI(this);
                if (result.getPublisherUID() != null)
                    result.isOwnPublication = result.getPublisherUID().compareTo(myIMEI) == 0;
                Intent intent = new Intent(getApplicationContext(), PublicationDetailsActivity.class);
                intent.putExtra(PublicationDetailsActivity.PUBLICATION_PARAM, result);
                startActivityForResult(intent, 1);
                if(progressDialog != null){
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                break;
        }
    }


}
