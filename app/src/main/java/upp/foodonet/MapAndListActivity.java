package upp.foodonet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Adapters.MainViewPagerAdapter;
import Adapters.MapMarkerInfoWindowAdapter;
import Adapters.SideMenuCursorAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class MapAndListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationChangeListener,
        AdapterView.OnItemClickListener,
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        IFooDoNetSQLCallback,
        IFooDoNetServerCallback,
        GoogleMap.OnInfoWindowClickListener {

    private static final String MY_TAG = "food_mapAndList";
    public static final String PUBLICATION_NUMBER = "pubnumber";

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
    ImageButton btn_focus_on_my_location;
    HorizontalScrollView hsv_gallery;
    LinearLayout gallery_pubs;
//    Button btn_side_menu_coll_exp_all;
//    Button btn_side_menu_coll_exp_my;
    boolean is_smenu_lv_my_expanded = true;
    boolean is_smenu_lv_all_expanded = true;

    int currentPageIndex;

    RelativeLayout btn_feedback_dialog;
    EditText et_feedbackText;
    Dialog feedbackDialog;
    ListView lv_side_menu_reg;
    ListView lv_side_menu_my;
    SideMenuCursorAdapter adapter_my;
    SideMenuCursorAdapter adapter_reg;

    boolean isSideMenuOpened = false;

    float kilometer_for_map;
    Date lastLocationUpdateDate;
    int myLocationRefreshRate;
    int width, height;
    //endregion

    //region Overrides of activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));

        int id = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = (int) extras.get(PUBLICATION_NUMBER);
        }
        if(id != 0)
        {
            //progressDialog = CommonUtil.ShowProgressDialog(getApplicationContext(), getString(R.string.progress_loading));
            FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
            InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
            ir.PublicationID = id;
            sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
        }

        btn_navigate_share = (Button) findViewById(R.id.btn_navigate_share);
        btn_navigate_take = (Button) findViewById(R.id.btn_navigate_take);
        btn_navigate_share.setOnClickListener(this);
        btn_show_L = (ImageButton) findViewById(R.id.btn_show_list_allPubs);
        btn_show_L.setOnClickListener(this);
        btn_show_M = (ImageButton) findViewById(R.id.btn_show_map_allPubs);
        btn_show_M.setOnClickListener(this);

        int dimenID = 0;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(metrics.densityDpi < DisplayMetrics.DENSITY_HIGH)
            dimenID = getResources().getDimensionPixelSize(R.dimen.bottom_nav_btn_img_size);
        else if(metrics.densityDpi < DisplayMetrics.DENSITY_XHIGH)
            dimenID = getResources().getDimensionPixelSize(R.dimen.bottom_nav_btn_img_size);
        else
            dimenID = getResources().getDimensionPixelSize(R.dimen.bottom_nav_btn_img_size);//42dp
/*
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
*/
        Drawable navigate_share = getResources().getDrawable(R.drawable.donate_v62x_60x60);//new BitmapDrawable(CommonUtil.decodeScaledBitmapFromDrawableResource(getResources(), R.drawable.donate_v62x_60x60, dimenID, dimenID));
        Drawable navigate_take = getResources().getDrawable(R.drawable.collect_v6_60x60);//new BitmapDrawable(CommonUtil.decodeScaledBitmapFromDrawableResource(getResources(), R.drawable.collect_v6_60x60, dimenID, dimenID));
        navigate_share.setBounds(0, 0, dimenID, dimenID);
        navigate_take.setBounds(0, 0, dimenID, dimenID);
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
                isSideMenuOpened = false;
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle("Title 2");
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
                isSideMenuOpened = true;
                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "Map and list", "Side menu", "Open menu");
            }
        };

//        btn_side_menu_coll_exp_my = (Button) findViewById(R.id.btn_collapse_expand_ll_my);
//        btn_side_menu_coll_exp_all = (Button) findViewById(R.id.btn_collapse_expand_ll_all);
//        btn_side_menu_coll_exp_my.setOnClickListener(this);
//        btn_side_menu_coll_exp_all.setOnClickListener(this);

        btn_focus_on_my_location = (ImageButton) findViewById(R.id.btn_center_on_my_location_map);
        btn_focus_on_my_location.setOnClickListener(this);
        hsv_gallery = (HorizontalScrollView)findViewById(R.id.hsv_image_gallery);

        btn_feedback_dialog = (RelativeLayout)findViewById(R.id.rl_btn_side_menu_feedback);
        btn_feedback_dialog.setOnClickListener(this);
        lv_side_menu_my = (ListView) findViewById(R.id.lv_side_menu_my);
        lv_side_menu_reg = (ListView) findViewById(R.id.lv_side_menu_reg);
        adapter_my = new SideMenuCursorAdapter(this, null, 0);
        adapter_reg = new SideMenuCursorAdapter(this, null, 0);
        lv_side_menu_my.setAdapter(adapter_my);
        lv_side_menu_reg.setAdapter(adapter_reg);
        lv_side_menu_my.setOnItemClickListener(this);
        lv_side_menu_reg.setOnItemClickListener(this);
        StartLoadingSideMenuMy();
        StartLoadingSideMenuReg();

        //endregion

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.map_one_kilometer_for_calculation, typedValue, true);
        kilometer_for_map = typedValue.getFloat();
        myLocationRefreshRate = getResources().getInteger(R.integer.map_refresh_my_location_frequency_milliseconds);
        Point size = GetScreenSize();
        width = size.x;
        height = size.y;

        /*drawerList = (ListView)findViewById(R.id.list_slidermenu);
        drawerList.setOnItemClickListener(this);*/
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        mainPager = (ViewPager) findViewById(R.id.main_Pager);
        mainPagerAdapter = new MainViewPagerAdapter(fm);
        mainPagerAdapter.SetMapFragment(this);
        currentPageIndex = mainPager.getCurrentItem();
        Log.i(MY_TAG, "onCreate sets map");
        //mainPagerAdapter.SetListFragment(eventArrayList, this);
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.addOnPageChangeListener(this);

        final View activityRootView = findViewById(R.id.drawer_layout);
/*
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    btn_navigate_share.setVisibility(View.GONE);
                    btn_navigate_take.setVisibility(View.GONE);
                } else {
                    btn_navigate_take.setVisibility(View.VISIBLE);
                    btn_navigate_share.setVisibility(View.VISIBLE);
                }
            }
        });
*/

        gallery_pubs = (LinearLayout)findViewById(R.id.ll_image_btns_gallery);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        // Checks whether a hardware keyboard is available
//        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
//            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
//            btn_navigate_share.setVisibility(View.VISIBLE);
//            btn_navigate_take.setVisibility(View.VISIBLE);
//        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
//            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
//            btn_navigate_share.setVisibility(View.GONE);
//            btn_navigate_take.setVisibility(View.GONE);
//        }
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
    protected void onResume() {
        super.onResume();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
    public void onBackPressed() {
        if (isSideMenuOpened) {
            drawerLayout.closeDrawer(ll_sideMenu);
            return;
        }
        if (currentPageIndex == PAGE_LIST){
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            mainPager.setCurrentItem(0);
            return;
        }
/*
        progressDialog = CommonUtil.ShowProgressDialog(this, "");
        Intent intent = new Intent(this, MyPublicationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
*/
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ForceReturn();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        return;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirmExit))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    private void ForceReturn(){
        finish();
    }

    @Override
    protected void onPause() {
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onPause();
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

                    btn_show_M.setImageDrawable(getResources().getDrawable(R.drawable.location_header_btn));
                    btn_show_L.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_show_map_allPubs:
                if (currentPageIndex == PAGE_MAP) {
                    drawerLayout.openDrawer(ll_sideMenu);

                    btn_show_M.setImageDrawable(getResources().getDrawable(R.drawable.menu_header_button));
                }
                if (currentPageIndex == PAGE_LIST) {
                    View view = this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    mainPager.setCurrentItem(0);

                    btn_show_M.setImageDrawable(getResources().getDrawable(R.drawable.menu_header_button));
                    btn_show_L.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_center_on_my_location_map:
                if (myLocation == null)
                    return;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(GetBoundsByCenterLatLng(myLocation), width, height, 0);
                googleMap.animateCamera(cu);
                break;
            case R.id.rl_btn_side_menu_feedback:
                feedbackDialog = new Dialog(this);
                feedbackDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                feedbackDialog.setContentView(R.layout.dialog_send_feedback);
                final Button btn_dialog_ok = (Button)feedbackDialog.findViewById(R.id.btn_feedback_ok);
                final Button btn_dialog_cancel = (Button)feedbackDialog.findViewById(R.id.btn_feedback_cancel);
                btn_dialog_ok.setOnClickListener(this);
                btn_dialog_cancel.setOnClickListener(this);
                et_feedbackText = (EditText)feedbackDialog.findViewById(R.id.et_feedback_text);
                et_feedbackText.setText("");
                feedbackDialog.show();
                break;
            case R.id.btn_feedback_ok:
                if(et_feedbackText.getText().toString().length() == 0){
                    CommonUtil.SetEditTextIsValid(this, et_feedbackText, false);
                    Toast.makeText(this, getString(R.string.feedback_validation_text_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.feedback_progress_sending));
                HttpServerConnectorAsync serverConnector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_FEEDBACK);
                ir.publicationReport = new PublicationReport();
                ir.publicationReport.setReportContactInfo(et_feedbackText.getText().toString());
                ir.publicationReport.setReportUserName(
                        getSharedPreferences(getString(R.string.shared_preferences_contact_info), MODE_PRIVATE)
                                .getString(getString(R.string.shared_preferences_contact_info_name), ""));
                ir.publicationReport.setDevice_uuid(CommonUtil.GetIMEI(this));
                ir.ServerSubPath = getString(R.string.server_post_report_feedback);
                serverConnector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);


            case R.id.btn_feedback_cancel:
                if(feedbackDialog != null)
                    feedbackDialog.dismiss();
                break;

            /*
            case R.id.btn_collapse_expand_ll_my:
                if (is_smenu_lv_my_expanded) {
                    adapter_my.swapCursor(null);
                    collapse(lv_side_menu_my);
                    is_smenu_lv_my_expanded = false;
                } else {
                    //RestartLoadingSideMenuMy();
                    expand(lv_side_menu_my);
                    is_smenu_lv_my_expanded = true;
                }
                break;
            case R.id.btn_collapse_expand_ll_all:
                if (is_smenu_lv_all_expanded) {
                    //adapter_reg.swapCursor(null);
                    collapse(lv_side_menu_reg);
                    is_smenu_lv_all_expanded = false;
                } else {
                    //RestartLoadingSideMenuReg();
                    expand(lv_side_menu_reg);
                    is_smenu_lv_all_expanded = true;
                }
                break;
            */
        }
    }

    //endregion

    //region Map and location methods

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (this.googleMap != null) {
            Toast.makeText(this, "Map loaded!", Toast.LENGTH_SHORT);
            isMapLoaded = true;
        } else {
            myMarkers = new HashMap<>();
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMyLocationChangeListener(this);
        googleMap.setInfoWindowAdapter(new MapMarkerInfoWindowAdapter(getLayoutInflater()));
/*
        Location myLocationLoc = googleMap.getMyLocation();
        if(myLocationLoc != null)
            myLocation = new LatLng(myLocationLoc.getLatitude(), myLocationLoc.getLongitude());
*/

        drawerLayout.setDrawerListener(mDrawerToggle);

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

/*
        googleMap.setOnMyLocationButtonClickListener();
        googleMap.setMyLocationEnabled();

        // Get the button view
        View locationButton = ((View) googleMap.get(1).getParent()).findViewById(2);

        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
*/

        StartLoadingForMarkers();
        //getSupportLoaderManager().initLoader(0, null, this);

        if (btn_focus_on_my_location != null && googleMap != null)
            btn_focus_on_my_location.setVisibility(View.VISIBLE);
        if(hsv_gallery != null)
            hsv_gallery.setVisibility(View.VISIBLE);
        if(btn_navigate_share != null)
            btn_navigate_share.setVisibility(View.VISIBLE);
        if(btn_navigate_take != null)
            btn_navigate_take.setVisibility(View.VISIBLE);

        if(progressDialog != null)
            progressDialog.dismiss();

        SetCamera();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        OnPublicationSelected(myMarkers.get(marker));
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        OnPublicationSelected(myMarkers.get(marker));
    }

    @Override
    public void onMyLocationChange(Location location) {
        Log.i(MY_TAG, "got location update from map");
        if (location == null)
            return;
        if (lastLocationUpdateDate == null)
            lastLocationUpdateDate = new Date();
        else {
            long millisPassed = new Date().getTime() - lastLocationUpdateDate.getTime();
            if (millisPassed < myLocationRefreshRate) {
                Log.i(MY_TAG, millisPassed + " after last update, not updating");
                return;
            } else {
                Log.i(MY_TAG, "updating location! lat: " + location.getLatitude()
                        + "; long: " + location.getLongitude());
                lastLocationUpdateDate = new Date();
            }
        }
/*
        if(myLocation == null) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_dot_circle);
            myLocation = AddMarker(((float)location.getLatitude()), (float)location.getLongitude(), getString(R.string.my_location), icon);
        }
*/
        if (myLocation != null && myLocation.latitude == location.getLatitude() && myLocation.longitude == location.getLongitude())
            return;
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (mainPagerAdapter != null){
            mainPagerAdapter.NotifyListOnLocationChange(location);
        }
        SetCamera();
/*
        if (GetDistance(myLocation, new LatLng(location.getLatitude(), location.getLongitude())) <= maxDistance)
            return;
*/
            //UpdateMyLocationPreferences(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void OnGotMyLocationCallback(Location location) {
        Log.i(MY_TAG, "got location callback from task");
        if (location != null)
            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (isMapLoaded)
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
        if (btn_focus_on_my_location != null)
            btn_focus_on_my_location.setVisibility(position == PAGE_MAP && googleMap != null ? View.VISIBLE : View.GONE);
        if(hsv_gallery != null)
            hsv_gallery.setVisibility(position == PAGE_MAP ? View.VISIBLE : View.GONE);
//        if(btn_navigate_share != null)
//            btn_navigate_share.setVisibility(position == PAGE_MAP ? View.VISIBLE : View.GONE);
//        if(btn_navigate_take != null)
//            btn_navigate_take.setVisibility(position == PAGE_MAP ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //endregion

    //region My methods

    private void OnPublicationSelected(long publicationID) {
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = publicationID;
        sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }

    private void SetCamera() {
        if (myLocation == null) {
            Log.i(MY_TAG, "SetCamera starts getting location");
            StartGetMyLocation();
        } else {
            OnReadyToUpdateCamera();
        }
    }

    private void OnReadyToUpdateCamera() {
        if (myMarkers != null && myMarkers.size() != 0) {
            double latitude = 0;
            double longtitude = 0;
            int counter = 0;
            if (myLocation != null) {
                if (average != null && GetDistance(average, myLocation) < maxDistance)
                    return;

                latitude += myLocation.latitude;
                longtitude += myLocation.longitude;
                counter++;
                maxDistance = getResources().getInteger(R.integer.map_max_distance_if_location_available) * kilometer_for_map;
            } else {
                for (Marker m : myMarkers.keySet()) {
                    latitude += m.getPosition().latitude;
                    longtitude += m.getPosition().longitude;
                    counter++;
                }
                maxDistance = getResources().getInteger(R.integer.map_max_distance_if_location_not_available) * kilometer_for_map;
            }

            average = new LatLng(latitude / counter, longtitude / counter);
            Log.i(MY_TAG, "center coordinades: " + average.latitude + ":" + average.longitude);

/*
            if (myLocation != null && GetDistance(average, myLocation) < maxDistance)
                maxDistance = GetDistance(average, myLocation);
*/

/*
            for (Marker m : myMarkers.keySet()) {
                if (GetDistance(average, m.getPosition()) > maxDistance)
                    maxDistance = GetDistance(average, m.getPosition());
            }
*/

        } else {
            average = myLocation;
            Log.i(MY_TAG, "center coordinades (by my location): " + average.latitude + ":" + average.longitude);
        }
        AnimateCameraFocusOnLatLng(average);
    }

    private void AnimateCameraFocusOnLatLng(LatLng latLng){
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(GetBoundsByCenterLatLng(latLng), width, height, 0);
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
        double distance = maxDistance * 1.5;
        return new LatLngBounds
                (new LatLng(center.latitude - distance, center.longitude - distance),
                        new LatLng(center.latitude + distance, center.longitude + distance));
    }

    //endregion

    //region SQL Loader methods

    private void StartLoadingSideMenuMy() {
        getSupportLoaderManager().initLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE, null, this);
    }

    private void RestartLoadingSideMenuMy() {
        getSupportLoaderManager().restartLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE, null, this);
    }

    private void StartLoadingSideMenuReg() {
        getSupportLoaderManager().initLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED, null, this);
    }

    private void RestartLoadingSideMenuReg() {
        getSupportLoaderManager().restartLoader(FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED, null, this);
    }

    private void StartLoadingForMarkers() {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void RestartLoadingForMarkers() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader cursorLoader = null;
        String[] projection;
        switch (id) {
            case 0:
                projection = FCPublication.GetColumnNamesArray();
                cursorLoader = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.URI_GET_PUBS_FOR_MAP_MARKERS,
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
        switch (loader.getId()) {
            case 0:
                if (data != null && data.moveToFirst()) {
                    //Log.i(MY_TAG, "num of rows in adapter: " + data.getCount());
                    ArrayList<FCPublication> publications = FCPublication.GetArrayListOfPublicationsForMapFromCursor(data);
                    if (publications == null) {
                        Log.e(MY_TAG, "error getting publications from sql");
                        return;
                    }

                    if (myMarkers == null)
                        myMarkers = new HashMap<>();
                    else {
                        for (Marker m : myMarkers.keySet())
                            m.remove();
                        myMarkers.clear();
                    }
                    gallery_pubs.setVisibility(View.GONE);
                    gallery_pubs.removeAllViews();

                    for (FCPublication publication : publications) {
                        Bitmap markerIcon;
                        BitmapDescriptor icon = null;

                        if(publication.getNumberOfRegistered() == 0){
                            markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                    getResources(), R.drawable.map_marker_whole, 13, 13);
                            icon = BitmapDescriptorFactory.fromBitmap(markerIcon);
                        }else {
                            if(publication.getNumberOfRegistered() >0
                                    && publication.getNumberOfRegistered() <3)
                                markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                        getResources(), R.drawable.map_marker_half, 13, 13);
                            else if(publication.getNumberOfRegistered() >= 3)
                                markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                        getResources(), R.drawable.map_marker_few, 13, 13);
                            else
                                markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                                        getResources(), R.drawable.map_marker_whole, 13, 13);

                            icon = BitmapDescriptorFactory.fromBitmap(markerIcon);
                        }

                        myMarkers.put(AddMarker(publication.getLatitude().floatValue(),
                                publication.getLongitude().floatValue(),
                                publication.getTitle(), icon), publication.getUniqueId());
                        AddImageToGallery(publication);
                    }

                    gallery_pubs.setVisibility(View.VISIBLE);

                    if (isMapLoaded)
                        SetCamera();
                }
                break;
            case FooDoNetSQLHelper.FILTER_ID_SIDEMENU_MY_ACTIVE:
                adapter_my.swapCursor(data);
                adapter_my.notifyDataSetChanged();
                break;
            case FooDoNetSQLHelper.FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED:
                adapter_reg.swapCursor(data);
                adapter_reg.notifyDataSetChanged();
                break;
        }
    }

    public void AddImageToGallery(final FCPublication publication){

        int screenLayout = this.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        int size = getResources().getDimensionPixelSize(R.dimen.gallery_image_btn_height);
        ImageButton imageButton = new ImageButton(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(15, 30, 15, 30);

        if(screenLayout == Configuration.SCREENLAYOUT_SIZE_SMALL) lp.setMargins(5, 10, 5, 10);

        imageButton.setLayoutParams(lp);
        imageButton.setBackgroundResource(R.drawable.map_gallery_border);

        Drawable drawable
                = CommonUtil.GetBitmapDrawableFromFile(
                    publication.GetImageFileName(), getString(R.string.image_folder_path), size, size);
        if(drawable == null)
            drawable = getResources().getDrawable(R.drawable.foodonet_logo_200_200);
        imageButton.setImageDrawable(drawable);
        imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
        imageButton.setOnClickListener(new View.OnClickListener() {
            int id = publication.getUniqueId();

            @Override
            public void onClick(View v) {
                ImageBtnFromGallerySelected(id);
                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "Map and list", "Gallery item", "item pressed");
            }
        });
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] { android.R.attr.state_pressed }, getResources().getDrawable(R.drawable.map_my_location_button_pressed));
        states.addState(new int[]{}, getResources().getDrawable(R.drawable.map_my_location_button_normal));
        imageButton.setBackgroundDrawable(states);
        gallery_pubs.addView(imageButton);
    }

    public void ImageBtnFromGallerySelected(int id){
        for(Map.Entry<Marker, Integer> e : myMarkers.entrySet()){
            if(e.getValue().intValue() == id){
                AnimateCameraFocusOnLatLng(e.getKey().getPosition());
                e.getKey().showInfoWindow();
            }
        }
        //Toast.makeText(this, "selected image id: " + String.valueOf(id), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //endregion

    //region Callback

    @Override
    public void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        if (isMapLoaded) {
            RestartLoadingForMarkers();
        }
        RestartLoadingSideMenuMy();
        RestartLoadingSideMenuReg();
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        if (request == null) {
            Log.e(MY_TAG, "OnSQLTaskComplete got null internalRequest");
            return;
        }
        switch (request.ActionCommand) {
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
                if(progressDialog != null)
                    progressDialog.dismiss();
                break;
            default:
                Log.e(MY_TAG, "can't get publication for details!");
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RestartLoadingSideMenuMy();
        RestartLoadingSideMenuReg();
        RestartLoadingForMarkers();
    }
    //endregion

    //region Collapse-expand lists

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(500);//(int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(200);//((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        if(progressDialog != null)
            progressDialog.dismiss();
        switch (response.ActionCommand){
            case InternalRequest.ACTION_POST_FEEDBACK:
                Toast.makeText(this, getString(R.string.feedback_uimessage_thanks), Toast.LENGTH_LONG).show();
                break;
        }
    }

    //endregion
}
