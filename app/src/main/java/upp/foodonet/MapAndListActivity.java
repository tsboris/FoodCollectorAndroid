package upp.foodonet;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import Adapters.MainViewPagerAdapter;
import Adapters.SideMenuCursorAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class MapAndListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationChangeListener,
        AdapterView.OnItemClickListener,
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, IFooDoNetSQLCallback {

    private static final String MY_TAG = "food_mapAndList";

    //region Variables

    GoogleMap googleMap;
    boolean isMapLoaded;
    double maxDistance;
    LatLng average, myLocation;
    HashMap<Marker, Integer> myMarkers;

    LinearLayout ll_sideMenu;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    ViewPager mainPager;
    MainViewPagerAdapter mainPagerAdapter;
    private final int PAGE_MAP = 0;
    private final int PAGE_LIST = 1;

    Button btn_navigate_share, btn_navigate_take;
    ImageButton btn_show_M, btn_show_L;

    int currentPageIndex;

    ListView lv_side_menu_reg;
    ListView lv_side_menu_my;
    SideMenuCursorAdapter adapter_my;
    SideMenuCursorAdapter adapter_reg;
    //endregion

    //region Overrides of activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        btn_navigate_share = (Button) findViewById(R.id.btn_navigate_share);
        btn_navigate_take = (Button) findViewById(R.id.btn_navigate_take);
        btn_navigate_share.setOnClickListener(this);
        btn_show_L = (ImageButton) findViewById(R.id.btn_show_list_allPubs);
        btn_show_L.setOnClickListener(this);
        btn_show_M = (ImageButton) findViewById(R.id.btn_show_map_allPubs);
        btn_show_M.setOnClickListener(this);

        Drawable navigate_share = getResources().getDrawable(R.drawable.donate_v62x_60x60);
        Drawable navigate_take = getResources().getDrawable(R.drawable.collect_v6_60x60);
        navigate_share.setBounds(0, 0, 60, 60);
        navigate_take.setBounds(0, 0, 60, 60);
        btn_navigate_share.setCompoundDrawables(null, navigate_share, null, null);
        btn_navigate_take.setCompoundDrawables(null, navigate_take, null, null);

        //region Sidemenu drawer methods
        ll_sideMenu = (LinearLayout) findViewById(R.id.ll_sideMenuPanel);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.foodonet_logo_200_200, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle("Title 1");
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle("Title 2");
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };

        lv_side_menu_my = (ListView)findViewById(R.id.lv_side_menu_my);
        lv_side_menu_reg = (ListView)findViewById(R.id.lv_side_menu_reg);
        adapter_my = new SideMenuCursorAdapter(this, null, 0);
        adapter_reg = new SideMenuCursorAdapter(this, null, 0);
        lv_side_menu_my.setAdapter(adapter_my);
        lv_side_menu_reg.setAdapter(adapter_reg);
        lv_side_menu_my.setOnItemClickListener(this);
        lv_side_menu_reg.setOnItemClickListener(this);
        StartLoadingSideMenuMy();
        StartLoadingSideMenuReg();

        //endregion

        /*drawerList = (ListView)findViewById(R.id.list_slidermenu);
        drawerList.setOnItemClickListener(this);*/
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        mainPager = (ViewPager) findViewById(R.id.main_Pager);
        mainPagerAdapter = new MainViewPagerAdapter(this, fm);
        mainPagerAdapter.SetMapFragment(this);
        currentPageIndex = mainPager.getCurrentItem();
        Log.i(MY_TAG, "onCreate sets map");
        //mainPagerAdapter.SetListFragment(eventArrayList, this);
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.addOnPageChangeListener(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  btn_navigate_share.setChecked(false);
        btn_navigate_share.setEnabled(true);
        //   btn_navigate_take.setChecked(true);
        btn_navigate_take.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_and_list, menu);
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

    //region Overrides of FooDoNetCustomActivityConnectedToService

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    //endregion

    //region Click / ItemClick listener

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OnPublicationSelected(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_navigate_share:
                Intent intent = new Intent(this, MyPublicationsActivity.class);
                //intent.putExtra("service", boundedService);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.btn_show_list_allPubs:
                if (currentPageIndex == PAGE_MAP) {
                    mainPager.setCurrentItem(1);
                }
                break;
            case R.id.btn_show_map_allPubs:
                if (currentPageIndex == PAGE_MAP) {
                    drawerLayout.openDrawer(ll_sideMenu);
                }
                if (currentPageIndex == PAGE_LIST) {
                    mainPager.setCurrentItem(0);
                }
                //case R.id.tgl_btn_take_maplst:
                //   break;
        }
    }

    //endregion

    //region Map and location methods

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (this.googleMap != null)
            Toast.makeText(getBaseContext(), "Map loaded!", Toast.LENGTH_SHORT);
        else {
            isMapLoaded = true;
            myMarkers = new HashMap<>();
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMyLocationChangeListener(this);

        drawerLayout.setDrawerListener(mDrawerToggle);

        StartLoadingForMarkers();
        //getSupportLoaderManager().initLoader(0, null, this);

        SetCamera();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        OnPublicationSelected(myMarkers.get(marker));
        return false;
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location == null)
            return;

/*
        if(myLocation == null) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_dot_circle);
            myLocation = AddMarker(((float)location.getLatitude()), (float)location.getLongitude(), getString(R.string.my_location), icon);
        }
*/
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (GetDistance(myLocation, new LatLng(location.getLatitude(), location.getLongitude())) <= maxDistance)
            return;
        if (myLocation == new LatLng(location.getLatitude(), location.getLongitude()))
            return;
        SetCamera();
    }

    @Override
    public void OnGotMyLocationCallback(Location location) {
        if (location != null)
            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        OnReadyToUpdateCamera();
    }

    private Marker AddMarker(float latitude, float longtitude, String title, BitmapDescriptor icon) {
        MarkerOptions newMarker = new MarkerOptions().position(new LatLng(latitude, longtitude)).title(title).draggable(false);
        if (icon != null)
            newMarker.icon(icon);
        return googleMap.addMarker(newMarker);
    }

    //endregion

    //region PageViewer methods

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPageIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //endregion

    //region My methods

    private void OnPublicationSelected(long publicationID){
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = publicationID;
        sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }

    private void SetCamera() {
        if (myLocation == null) {
            maxDistance = 0.009 * 40;
            StartGetMyLocation();
        } else {
            OnReadyToUpdateCamera();
        }
    }

    private void OnReadyToUpdateCamera() {
        Point size = GetScreenSize();
        int width = size.x;
        int height = size.y;
        if (myMarkers != null && myMarkers.size() != 0) {
            double latitude = 0;
            double longtitude = 0;
            int counter = 1;
            if (myLocation != null) {
                if (average != null && GetDistance(average, myLocation) < maxDistance)
                    return;

                latitude += myLocation.latitude;
                longtitude += myLocation.longitude;
                counter++;
            }

            for ( Marker m : myMarkers.keySet()) {
                latitude += m.getPosition().latitude;
                longtitude += m.getPosition().longitude;
                counter++;
            }

            average = new LatLng(latitude / counter, longtitude / counter);

            maxDistance = 0;

            if (myLocation != null)
                maxDistance = GetDistance(average, myLocation);

            for (Marker m : myMarkers.keySet()) {
                if (GetDistance(average, m.getPosition()) > maxDistance)
                    maxDistance = GetDistance(average, m.getPosition());
            }

        } else {
            average = myLocation;
        }
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(GetBoundsByCenterLatLng(average), width, height, 0);// new LatLngBounds(southWest, northEast)
        googleMap.animateCamera(cu);
    }

    private Point GetScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private double GetDistance(LatLng pos1, LatLng pos2) {
        return Math.sqrt(Math.pow(pos1.latitude - pos2.latitude, 2) + Math.pow(pos1.longitude - pos2.longitude, 2));
    }

    private LatLngBounds GetBoundsByCenterLatLng(LatLng center) {
        return new LatLngBounds
                (new LatLng(center.latitude - maxDistance * 1.5, center.longitude - maxDistance * 1.5),
                        new LatLng(center.latitude + maxDistance * 1.5, center.longitude + maxDistance * 1.5));


    }

    //endregion

    //region SQL Loader methods

    private void StartLoadingSideMenuMy(){
        getSupportLoaderManager().initLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE, null, this);
    }
    private void RestartLoadingSideMenuMy(){
        getSupportLoaderManager().restartLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE, null, this);
    }

    private void StartLoadingSideMenuReg(){
        getSupportLoaderManager().initLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED, null, this);
    }
    private void RestartLoadingSideMenuReg(){
        getSupportLoaderManager().restartLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED, null, this);
    }

    private void StartLoadingForMarkers(){
        getSupportLoaderManager().initLoader(0, null, this);
    }
    private void RestartLoadingForMarkers(){
        getSupportLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader cursorLoader = null;
        String[] projection;
        switch (id){
            case 0:
                projection = FCPublication.GetColumnNamesArray();
                cursorLoader = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.CONTENT_URI,
                                                                                projection, null, null, null);
                break;
            case FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED:
            case FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE:
                projection = FCPublication.GetColumnNamesForListArray();
                cursorLoader = new android.support.v4.content.CursorLoader(this,
                        Uri.parse(FooDoNetSQLProvider.URI_GET_PUBS_FOR_LIST_BY_FILTER_ID + "/" + id),
                        projection, null, null, null);
                break;
            default:
                Log.e(MY_TAG, "mapActivity - unexpected filter sent to loader");
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case 0:
                if (data.moveToFirst()) {
                    ArrayList<FCPublication> publications = FCPublication.GetArrayListOfPublicationsFromCursor(data, false);
                    if (publications == null) {
                        Log.e(MY_TAG, "error getting publications from sql");
                        return;
                    }

                    if (myMarkers == null)
                        myMarkers = new HashMap<>();
                    else {
                        for(Marker m : myMarkers.keySet())
                            m.remove();
                        myMarkers.clear();
                    }
                    for (FCPublication publication : publications) {
                        Bitmap markerIcon;

                        BitmapDescriptor icon = null;
                        switch (publication.getUniqueId() % 3){
                            case 0:
                                markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                        getResources(), R.drawable.map_marker_few, 13, 13);
                                icon = BitmapDescriptorFactory .fromBitmap(markerIcon);
                                break;
                            case 1:
                                markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                        getResources(), R.drawable.map_marker_half, 13, 13);
                                icon = BitmapDescriptorFactory .fromBitmap(markerIcon);
                                break;
                            case 2:
                                markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                        getResources(), R.drawable.map_marker_whole, 13, 13);
                                icon = BitmapDescriptorFactory .fromBitmap(markerIcon);
                                break;
                        }
                        myMarkers.put(AddMarker(publication.getLatitude().floatValue(),
                                publication.getLongitude().floatValue(),
                                publication.getTitle(), icon), publication.getUniqueId());
                    }

                    if (isMapLoaded)
                        SetCamera();
                }
                break;
            case FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE:
                adapter_my.swapCursor(data);
                break;
            case FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED:
                adapter_reg.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //endregion

    //region Callback

    @Override
    public void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        if(isMapLoaded){
            RestartLoadingForMarkers();
        }
        RestartLoadingSideMenuMy();
        RestartLoadingSideMenuReg();
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                lv_side_menu_reg.clearChoices();
                lv_side_menu_my.clearChoices();
                lv_side_menu_reg.requestLayout();
                lv_side_menu_my.requestLayout();
                FCPublication result = request.publicationForDetails;
                String myIMEI = CommonUtil.GetIMEI(this);
                result.isOwnPublication = result.getPublisherUID().compareTo(myIMEI) == 0;
                Intent intent = new Intent(this, PublicationDetailsActivity.class);
                intent.putExtra(PublicationDetailsActivity.PUBLICATION_PARAM, result);
                startActivityForResult(intent, 1);
                break;
            default:
                Log.e(MY_TAG, "can't get publication for details!");
                break;
        }

    }

    //endregion
}
