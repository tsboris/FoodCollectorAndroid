package upp.foodonet;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import Adapters.MainViewPagerAdapter;
import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class MapAndListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationChangeListener,
        AdapterView.OnItemClickListener,
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String MY_TAG = "food_mapAndList";

    GoogleMap googleMap;
    boolean isMapLoaded;
    double maxDistance;
    LatLng average, myLocation;
    ArrayList<Marker> myMarkers;


    DrawerLayout drawerLayout;
    ViewPager mainPager;
    MainViewPagerAdapter mainPagerAdapter;
    ActionBarDrawerToggle mDrawerToggle;
    //MyPublicationsTabFragment myPublicationsTabFragment;

    ToggleButton tgl_btn_navigate_share;
    ToggleButton tgl_btn_navigate_take;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        tgl_btn_navigate_share = (ToggleButton) findViewById(R.id.tgl_btn_share_maplst);
        tgl_btn_navigate_take = (ToggleButton) findViewById(R.id.tgl_btn_take_maplst);
        tgl_btn_navigate_share.setOnClickListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        /*drawerList = (ListView)findViewById(R.id.list_slidermenu);
        drawerList.setOnItemClickListener(this);*/
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        mainPager = (ViewPager) findViewById(R.id.main_Pager);
        mainPagerAdapter = new MainViewPagerAdapter(this, fm);
        mainPagerAdapter.SetMapFragment(this);
        Log.i(MY_TAG, "onCreate sets map");
        //mainPagerAdapter.SetListFragment(eventArrayList, this);
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.addOnPageChangeListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        tgl_btn_navigate_share.setChecked(false);
        tgl_btn_navigate_share.setEnabled(true);
        tgl_btn_navigate_take.setChecked(true);
        tgl_btn_navigate_take.setEnabled(false);
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

    @Override
    public void OnNotifiedToFetchData() {
        Toast.makeText(this, MY_TAG + " OnNotifiedToFetchData()", Toast.LENGTH_LONG);
        Log.i(MY_TAG, "OnNotifiedToFetchData()");
        getSupportLoaderManager().restartLoader(0, null, this);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (this.googleMap != null)
            Toast.makeText(this, "Map loaded!", Toast.LENGTH_SHORT);
        else{
            isMapLoaded = true;
            myMarkers = new ArrayList<>();
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMyLocationChangeListener(this);
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
        drawerLayout.setDrawerListener(mDrawerToggle);

        getSupportLoaderManager().initLoader(0, null, this);

        SetCamera();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tgl_btn_share_maplst:
                Intent intent = new Intent(this, MyPublicationsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            //case R.id.tgl_btn_take_maplst:
            //   break;
        }
    }

    private void SetCamera() {
        if (myLocation == null) {
            maxDistance = 0.009 * 40;
            StartGetMyLocation();
        } else {
            OnReadyToUpdateCamera();
        }
    }

    private void OnReadyToUpdateCamera(){
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

            for (Marker m : myMarkers) {
                latitude += m.getPosition().latitude;
                longtitude += m.getPosition().longitude;
                counter++;
            }

            average = new LatLng(latitude / counter, longtitude / counter);

            maxDistance = 0;

            if (myLocation != null)
                maxDistance = GetDistance(average, myLocation);

            for (Marker m : myMarkers) {
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
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        OnReadyToUpdateCamera();
    }

    private Marker AddMarker(float latitude, float longtitude, String title, BitmapDescriptor icon) {
        MarkerOptions newMarker = new MarkerOptions().position(new LatLng(latitude, longtitude)).title(title).draggable(false);
        if (icon != null)
            newMarker.icon(icon);
        return googleMap.addMarker(newMarker);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = FCPublication.GetColumnNamesArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            ArrayList<FCPublication> publications = FCPublication.GetArrayListOfPublicationsFromCursor(data, false);
            if(publications == null){
                Log.e(MY_TAG, "error getting publications from sql");
                return;
            }

            if(myMarkers == null)
                myMarkers = new ArrayList<>();
            for (FCPublication publication : publications){
                myMarkers.add(AddMarker(publication.getLatitude().floatValue(),
                                        publication.getLongitude().floatValue(),
                                        publication.getTitle(), null));
            }

            if(isMapLoaded)
                SetCamera();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
