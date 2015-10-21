package upp.foodonet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.maps.model.LatLng;

import Adapters.PublicationsListCursorAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.GetMyLocationAsync;
import CommonUtilPackage.IGotMyLocationCallback;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

/**
 * Created by Asher on 26.08.2015.
 */
public class AllPublicationsTabFragment
        extends android.support.v4.app.Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener,
        IFooDoNetSQLCallback,
        IGotMyLocationCallback,
        View.OnClickListener, TextWatcher {

    private static final String MY_TAG = "food_allPubs";

    private static final int ADD_TODO_ITEM_REQUEST = 0;

    private int currentFilterID;

    //FCPublicationListAdapter mAdapter;
    private Context context;
    //SimpleCursorAdapter adapter;
    PublicationsListCursorAdapter adapter;
    //Animation animZoomIn;

    ListView lv_my_publications;
    //SearchView sv_search_in_all_pubs;
    EditText et_search_in_all_pubs;
    boolean preventOverflow = false;
    boolean isFirstLoad = false;

    Button btn_close_search;
    Button btn_filter_closest;
    Button btn_filter_newest;
    Button btn_filter_less_regs;

    ProgressDialog progressDialog;
    boolean waitingForMyLocationForList = false;
    //Button btn_new_publication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
/*
        Intent allPubIntent = new Intent(context, AllPublicationsActivity.class);
        startActivity(allPubIntent);
*/
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_all_publications_activity, container, false);
        lv_my_publications = (ListView) view.findViewById(R.id.lv_all_active_publications);
        et_search_in_all_pubs = (EditText) view.findViewById(R.id.et_search_in_all_pubs);
        et_search_in_all_pubs.addTextChangedListener(this);
//        et_search_in_all_pubs.setOnFocusChangeListener(this);
//        sv_search_in_all_pubs = (SearchView) view.findViewById(R.id.sv_search_in_all_pubs);
//        sv_search_in_all_pubs.setOnQueryTextListener(this);
//        sv_search_in_all_pubs.setIconified(true);
//        btn_close_search = (Button)sv_search_in_all_pubs.findViewById(R.id.search_close_btn);
//        btn_close_search.setOnClickListener(this);
        btn_filter_closest = (Button) view.findViewById(R.id.btn_filter_closest_all_pubs);
        btn_filter_closest.setOnClickListener(this);
        btn_filter_newest = (Button) view.findViewById(R.id.btn_filter_newest_all_pubs);
        btn_filter_newest.setOnClickListener(this);
        btn_filter_less_regs = (Button) view.findViewById(R.id.btn_filter_less_regs_all_pubs);
        btn_filter_less_regs.setOnClickListener(this);

        //animZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
        //btn_new_publication = (Button)view.findViewById(R.id.btn_add_new_publication);
        //btn_new_publication.setOnClickListener(this);

        //String[] from = new String[]{FCPublication.PUBLICATION_TITLE_KEY,
        //FCPublication.PUBLICATION_ADDRESS_KEY
                /*,Distance(Double.parseDouble(FCPublication.PUBLICATION_LONGITUDE_KEY),Double.parseDouble(FCPublication.PUBLICATION_LATITUDE_KEY))*/
               /* ,FCPublication.PUBLICATION_PHOTO_URL*///};
        //int[] to = new int[]{R.id.tv_title_myPub_item,R.id.tv_subtitle_myPub_item/*,R.id.tv_distance_myPub_item,*//*,R.id.img_main__myPub_item*/};

        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST;
        GetMyLocationAsync locationAsync
                = new GetMyLocationAsync((LocationManager) context.getSystemService(Context.LOCATION_SERVICE), context);
        locationAsync.setGotLocationCallback(this);
        isFirstLoad = true;
        locationAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return view;
    }

    public void SetContext(Context context) {
        this.context = context;
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void StartLoadingCursorForList(int filterTypeID) {
        getLoaderManager().initLoader(filterTypeID, null, this);
    }

    private void RestartLoadingCursorForList() {//int filterTypeID) {
        getLoaderManager().restartLoader(currentFilterID, null, this);
//        onLoaderReset(null);
//        StartLoadingCursorForList(filterTypeID);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = FCPublication.GetColumnNamesForListArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(context,
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

    /*
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, AddNewFCPublicationActivity.class);
        startActivityForResult(intent, 0);
    }

*/
    // NOT SURE IF THIS WILL BE USED - maybe there will be result after openin existing pub
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*
        Intent intent = new Intent(context, PublicationDetailsActivity.class);
        intent.putExtra(PublicationDetailsActivity.PUBLICATION_PARAM, )
*/
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, context.getContentResolver());
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = id;
        sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }

    private void GetPublicationFromListDetails() {

    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                FCPublication result = request.publicationForDetails;
                String myIMEI = CommonUtil.GetIMEI(context);
                result.isOwnPublication = result.getPublisherUID().compareTo(myIMEI) == 0;
                Intent intent = new Intent(context, PublicationDetailsActivity.class);
                intent.putExtra(PublicationDetailsActivity.PUBLICATION_PARAM, result);
                startActivityForResult(intent, 1);
                break;
            default:
                Log.e(MY_TAG, "can't get publication for details!");
                break;
        }
    }

    @Override
    public void OnGotMyLocationCallback(Location location) {
        LatLng locationData = location == null ? null : new LatLng(location.getLatitude(), location.getLongitude());
        if (locationData != null)
            CommonUtil.UpdateFilterMyLocationPreferences(context, locationData);
        Log.i(MY_TAG, "list got location from map");
        if (isFirstLoad) {
            Log.i(MY_TAG, "location got first time");
            isFirstLoad = false;
            adapter = new PublicationsListCursorAdapter(context, null, 0, locationData, false);
            lv_my_publications.setAdapter(adapter);
            lv_my_publications.setOnItemClickListener(this);
            StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST);
            return;
        } else {
            Log.i(MY_TAG, "updating adapter's my location field");
            if (adapter != null)
                adapter.SetMyLocation(locationData);
            adapter.notifyDataSetChanged();
        }
        if (waitingForMyLocationForList) {
            waitingForMyLocationForList = false;
            if (progressDialog != null)
                progressDialog.dismiss();
            progressDialog = null;
//            preventOverflow = true;
//            sv_search_in_all_pubs.setFocusable(false);
//            sv_search_in_all_pubs.setQuery("", false);
//            sv_search_in_all_pubs.setFocusable(true);
//            preventOverflow = false;
            StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST);
        }

    }

/*
    @Override
    public boolean onQueryTextSubmit(String query) {// on button search pressed
        Log.i(MY_TAG, "text query: " + query);
        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER;
        sv_search_in_all_pubs.clearFocus();
        FooDoNetCustomActivityConnectedToService.UpdateFilterTextPreferences(context, query);
//        btn_filter_closest.setAnimation(null);
//        btn_filter_newest.setAnimation(null);
//        btn_filter_less_regs.setAnimation(null);
        StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {// on letter entered
        Log.i(MY_TAG, "text newText: " + newText);
        if(TextUtils.isEmpty(newText) && !preventOverflow){
            Log.i(MY_TAG, "clean search button pressed");
            sv_search_in_all_pubs.clearFocus();
            onClick(btn_filter_newest);
            sv_search_in_all_pubs.onActionViewCollapsed();
        }
        return false;
    }
*/

    @Override
    public void onClick(View v) {
        //sv_search_in_all_pubs.onActionViewCollapsed();
        et_search_in_all_pubs.removeTextChangedListener(this);
        et_search_in_all_pubs.setText("");
        et_search_in_all_pubs.setCompoundDrawablesWithIntrinsicBounds(R.drawable.collect_v6_30x30, 0, 0, 0);
        InputMethodManager inputManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(et_search_in_all_pubs.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        et_search_in_all_pubs.addTextChangedListener(this);
        switch (v.getId()) {
            case R.id.btn_filter_closest_all_pubs:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
                btn_filter_closest.setEnabled(false);
                btn_filter_closest.setTextColor(getResources().getColor(R.color.inactive_blue));
                btn_filter_newest.setEnabled(true);
                btn_filter_newest.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_filter_less_regs.setEnabled(true);
                btn_filter_less_regs.setTextColor(getResources().getColor(R.color.basic_blue));
                CheckIfMyLocationSavedInPreferencesAndLoad();
                break;
            case R.id.btn_filter_newest_all_pubs:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST;
                btn_filter_closest.setEnabled(true);
                btn_filter_closest.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_filter_newest.setEnabled(false);
                btn_filter_newest.setTextColor(getResources().getColor(R.color.inactive_blue));
                btn_filter_less_regs.setEnabled(true);
                btn_filter_less_regs.setTextColor(getResources().getColor(R.color.basic_blue));
/*
                preventOverflow = true;
                sv_search_in_all_pubs.setFocusable(false);
                sv_search_in_all_pubs.setQuery("", false);
                sv_search_in_all_pubs.setFocusable(true);
                preventOverflow = false;
*/
                StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST);
                break;
            case R.id.btn_filter_less_regs_all_pubs:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_LESS_REGS;
                btn_filter_closest.setEnabled(true);
                btn_filter_closest.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_filter_newest.setEnabled(true);
                btn_filter_newest.setTextColor(getResources().getColor(R.color.basic_blue));
                btn_filter_less_regs.setEnabled(false);
                btn_filter_less_regs.setTextColor(getResources().getColor(R.color.inactive_blue));
/*
                preventOverflow = true;
                sv_search_in_all_pubs.setFocusable(false);
                sv_search_in_all_pubs.setQuery("", false);
                sv_search_in_all_pubs.setFocusable(true);
                preventOverflow = false;
*/
                StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_LESS_REGS);
                break;
//            case R.id.search_close_btn:
//                break;
        }
    }

    private void StartLoaderByFilterID(int filterID) {
        if (currentFilterID == filterID)
            RestartLoadingCursorForList();
        else {
            adapter.swapCursor(null);
            currentFilterID = filterID;
            StartLoadingCursorForList(currentFilterID);
        }
    }

    private void CheckIfMyLocationSavedInPreferencesAndLoad() {
        LatLng myLocationFromPreferences = CommonUtil.GetFilterLocationFromPreferences(context);
        if (myLocationFromPreferences.latitude == -1000
                || myLocationFromPreferences.longitude == -1000) {
            String progressMessage = getString(R.string.progress_waiting_for_location);
            progressDialog = CommonUtil.ShowProgressDialog(context, progressMessage);
            GetMyLocationAsync locationTask
                    = new GetMyLocationAsync((LocationManager) context.getSystemService(Context.LOCATION_SERVICE), context);
            locationTask.setGotLocationCallback(this);
            waitingForMyLocationForList = true;
            locationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(et_search_in_all_pubs.getText().toString()))
            onClick(btn_filter_newest);
        FooDoNetCustomActivityConnectedToService.UpdateFilterTextPreferences(context, et_search_in_all_pubs.getText().toString());
        StartLoaderByFilterID(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().length() > 0) {
            et_search_in_all_pubs.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            //Assign your image again to the view, otherwise it will always be gone even if the text is 0 again.
            et_search_in_all_pubs.setCompoundDrawablesWithIntrinsicBounds(R.drawable.collect_v6_30x30, 0, 0, 0);
        }
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        mAdapter = new FCPublicationListAdapter(getApplicationContext());

        getListView().setFooterDividersEnabled(true);

        Button headerView = (Button)getLayoutInflater().inflate(R.layout.header_view, null);

        // TODO - Add footerView to ListView
        getListView().addHeaderView(headerView);


        // TODO - Attach Listener to FooterView
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("FOODONET", "Entered submitButton.OnClickListener.onClick()");

                Intent intent = new Intent(MainActivity.this, AddNewFCPublicationActivity.class);
                startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);
            }
        });

        getListView().setAdapter(mAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("FOODONET", "Entered onActivityResult()");


        if (requestCode == ADD_TODO_ITEM_REQUEST) {
            if(resultCode == RESULT_OK){

                FCPublication item = new FCPublication(data);
                mAdapter.add(item);
            }
            if (resultCode == RESULT_CANCELED) {

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
*/
}
