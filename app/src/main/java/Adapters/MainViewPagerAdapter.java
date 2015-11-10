package Adapters;

import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import upp.foodonet.AllPublicationsTabFragment;

/**
 * Created by Asher on 20.06.2015.
 */
public class MainViewPagerAdapter extends FragmentPagerAdapter {

    private static final String MY_TAG = "food_pagerAdapter";

    SupportMapFragment mapFragment;
    AllPublicationsTabFragment allPublicationsTabFragment;
    final int NUM_ITEMS = 2;

    public MainViewPagerAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
        allPublicationsTabFragment = new AllPublicationsTabFragment();
    }

    public void SetMapFragment(OnMapReadyCallback mapReadyCallback){
        mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(mapReadyCallback);
    }

/*
    public void SetListFragment(ArrayList<FooDoNetEvent> items, Context context){
        if(myPublicationsTabFragment != null){
            MyPublicationsTabFragment.SetListOfEvents(items);
            myPublicationsTabFragment.SetContext(context);
        }
    }
*/

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return mapFragment;
            case 1:
                return allPublicationsTabFragment;//return null;//eventsTabFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0)
            return "Map";
        if(position == 1)
            return "Events";
        return "";
    }

    public void NotifyListOnLocationChange(Location location){
        Log.i(MY_TAG, "updating location from map to list");
        allPublicationsTabFragment.OnGotMyLocationCallback(location);
    }
}
