package upp.foodonet;

import android.location.Location;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
        ViewPager.OnPageChangeListener {

    GoogleMap googleMap;
    DrawerLayout drawerLayout;
    ViewPager mainPager;
    MainViewPagerAdapter mainPagerAdapter;
    ActionBarDrawerToggle mDrawerToggle;
    MyPublicationsTabFragment myPublicationsTabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        /*drawerList = (ListView)findViewById(R.id.list_slidermenu);
        drawerList.setOnItemClickListener(this);*/
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        mainPager = (ViewPager)findViewById(R.id.main_Pager);
        mainPagerAdapter = new MainViewPagerAdapter(this, fm);
        mainPagerAdapter.SetMapFragment(this);
        //mainPagerAdapter.SetListFragment(eventArrayList, this);
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.addOnPageChangeListener(this);

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
}
