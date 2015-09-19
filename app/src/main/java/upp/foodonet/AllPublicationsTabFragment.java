package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.maps.model.LatLng;

import Adapters.PublicationsListCursorAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.GetMyLocationAsync;
import CommonUtilPackage.IGotMyLocationCallback;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import CommonUtilPackage.InternalRequest;

/**
 * Created by Asher on 26.08.2015.
 */
public class AllPublicationsTabFragment
        extends android.support.v4.app.Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, IFooDoNetSQLCallback, IGotMyLocationCallback {

    private static final String MY_TAG = "food_allPubs";

    private static final int ADD_TODO_ITEM_REQUEST = 0;
    //FCPublicationListAdapter mAdapter;
    private Context context;
    //SimpleCursorAdapter adapter;
    PublicationsListCursorAdapter adapter;

    ListView lv_my_publications;
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
        //btn_new_publication = (Button)view.findViewById(R.id.btn_add_new_publication);
        //btn_new_publication.setOnClickListener(this);

        //String[] from = new String[]{FCPublication.PUBLICATION_TITLE_KEY,
                //FCPublication.PUBLICATION_ADDRESS_KEY
                /*,Distance(Double.parseDouble(FCPublication.PUBLICATION_LONGITUDE_KEY),Double.parseDouble(FCPublication.PUBLICATION_LATITUDE_KEY))*/
               /* ,FCPublication.PUBLICATION_PHOTO_URL*///};
        //int[] to = new int[]{R.id.tv_title_myPub_item,R.id.tv_subtitle_myPub_item/*,R.id.tv_distance_myPub_item,*//*,R.id.img_main__myPub_item*/};
        GetMyLocationAsync locationAsync
                = new GetMyLocationAsync((LocationManager)context.getSystemService(Context.LOCATION_SERVICE), context);
        locationAsync.setGotLocationCallback(this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (context == null) return null;
        String[] projection = FCPublication.GetColumnNamesForListArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(context, FooDoNetSQLProvider.URI_GET_ALL_PUBS_FOR_LIST_ID_DESC, projection, null, null, null);
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

    private void GetPublicationFromListDetails(){

    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
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
        adapter = new PublicationsListCursorAdapter(context, null, 0,
                new LatLng(location.getLatitude(), location.getLongitude()));
        lv_my_publications.setAdapter(adapter);
        lv_my_publications.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);

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
