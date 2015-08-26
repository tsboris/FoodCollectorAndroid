package upp.foodonet;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

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
        View.OnClickListener {

    private static final String MY_TAG = "food_mapAndList";

    GoogleMap googleMap;
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

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMyLocationChange(Location location) {

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
}
