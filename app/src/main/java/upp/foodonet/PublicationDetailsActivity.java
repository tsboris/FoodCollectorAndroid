package upp.foodonet;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Adapters.PublicationDetailsReportsAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;
import UIUtil.RoundedImageView;


public class PublicationDetailsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, IFooDoNetServerCallback, IFooDoNetSQLCallback {
    public static final String PUBLICATION_PARAM = "publication";
    public static final String IS_OWN_PUBLICATION_PARAM = "is_own";
    private static final String MY_TAG = "food_PubDetails";
    public static final String DETAILS_ACTIVITY_RESULT_KEY = "details_result";

    public static final int REQUEST_CODE_EDIT_PUBLICATION = 1;
    private static final String PERMISSION = "publish_actions";

    private FCPublication publication;

    private AlertDialog cancelPublicationDialog;
    private Bitmap photoBmp;
    private boolean isOwnPublication;
    private boolean isRegisteredForCurrentPublication = false;
    boolean isImageFitToScreen = false;

    //new:
    ImageButton btn_menu;
    Button btn_leave_report;
    TextView tv_title;
    RoundedImageView riv_image;
    ImageView iv_num_of_reged;
    TextView tv_num_of_reged;
    TextView tv_address;
    TextView tv_distance;
    ImageButton btn_facebook_my;
    ImageButton btn_twitter_my;
    ImageButton btn_call_owner;
    ImageButton btn_sms_owner;
    ImageButton btn_call_reg;
    ImageButton btn_sms_reg;
    ImageButton btn_navigate;
    ImageButton btn_reg_unreg;
    TextView tv_subtitle;
    ListView lv_reports;
    TextView tv_no_reports;
    TextView tv_start_dateTime_details, tv_end_dateTime_details;

    LinearLayout ll_button_panel_my;
    LinearLayout ll_button_panel_others;
    LinearLayout ll_reports_panel;

    PublicationDetailsReportsAdapter adapter;

    PopupMenu popup;

    public static final String SHARED_PREF_PENDING_BROADCAST_KEY = "pending_broadcast";

    //region Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_publication_details);

        Intent i = getIntent();
        this.publication = (FCPublication) i.getSerializableExtra(PUBLICATION_PARAM);
        if (publication == null) {
            Log.e(MY_TAG, "no publication found in extras!");
            finish();
        }


        isRegisteredForCurrentPublication = checkIfRegisteredForThisPublication();

        btn_menu = (ImageButton) findViewById(R.id.btn_menu_pub_details);
        btn_leave_report = (Button) findViewById(R.id.btn_leave_report_pub_details);
        tv_title = (TextView) findViewById(R.id.tv_title_pub_details);
        riv_image = (RoundedImageView) findViewById(R.id.riv_image_pub_details);
        iv_num_of_reged = (ImageView) findViewById(R.id.iv_user_reg_icon_pub_details);
        tv_num_of_reged = (TextView) findViewById(R.id.tv_num_of_reged_pub_details);
        tv_address = (TextView) findViewById(R.id.tv_address_pub_details);
        tv_distance = (TextView) findViewById(R.id.tv_distance_pub_details);
        btn_facebook_my = (ImageButton) findViewById(R.id.btn_facebook_my_pub_details);
        btn_twitter_my = (ImageButton) findViewById(R.id.btn_tweet_my_pub_details);
        btn_call_owner = (ImageButton) findViewById(R.id.btn_call_owner_pub_details);
        btn_sms_owner = (ImageButton) findViewById(R.id.btn_message_owner_pub_details);
        btn_call_reg = (ImageButton) findViewById(R.id.btn_call_reged_pub_details);
        btn_sms_reg = (ImageButton) findViewById(R.id.btn_message_reged_pub_details);
        btn_reg_unreg = (ImageButton) findViewById(R.id.btn_register_unregister_pub_details);
        btn_navigate = (ImageButton) findViewById(R.id.btn_navigate_pub_details);
        tv_subtitle = (TextView) findViewById(R.id.tv_subtitle_pub_details);
        lv_reports = (ListView) findViewById(R.id.lv_list_of_reports_pub_details);
        ll_reports_panel = (LinearLayout) findViewById(R.id.ll_reports_list_pub_details);
        ll_button_panel_my = (LinearLayout) findViewById(R.id.ll_my_pub_dets_buttons_panel);
        ll_button_panel_others = (LinearLayout) findViewById(R.id.ll_others_pub_dets_buttons_panel);
        tv_start_dateTime_details = (TextView) findViewById(R.id.tv_start_time_pub_details);
        tv_end_dateTime_details = (TextView) findViewById(R.id.tv_end_time_pub_details);
        tv_no_reports = (TextView)findViewById(R.id.tv_no_reports_for_pub);


        tv_title.setText(publication.getTitle());
        tv_subtitle.setText(publication.getSubtitle());//publication.getSubtitle());
        tv_address.setText(publication.getAddress());
        SetImage();
        StartNumOfRegedLoader();
        CalculateDistanceAndSetText();
        ChooseButtonPanel();
        SetReportsList();

        startEndTimeSet();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
/*
        switch (v.getId()){
            case R.id.btn_menu_pub_details:
                menu.add(0, 1, 0, "Red");
                menu.add(0, 2, 0, "Green");
                menu.add(0, 3, 0, "Blue");
                break;
        }
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(MY_TAG, "Entered onActivityResult()");

        FCPublication pub = new FCPublication();
        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CODE_EDIT_PUBLICATION) {

                FCPublication publication
                        = (FCPublication) data.getExtras().get(AddEditPublicationActivity.PUBLICATION_KEY);
                if (publication == null) {
                    Log.i(MY_TAG, "got no pub from AddNew");
                    return;
                }
                //=============>
                AddEditPublicationService.StartSaveEditedPublication(getApplicationContext(), publication);
            }
        }
        if (resultCode == RESULT_CANCELED) {

        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(DETAILS_ACTIVITY_RESULT_KEY, InternalRequest.ACTION_NO_ACTION);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(progressDialog != null && progressDialog.isShowing()){
            SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_pending_broadcast), MODE_PRIVATE);
            if(!sp.contains(getString(R.string.shared_preferences_pending_broadcast_value)))
                Log.e(MY_TAG, "progress bar showing, but no pending broadcast");
            Intent intent = new Intent();
            intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                    sp.getInt(getString(R.string.shared_preferences_pending_broadcast_value), -1));
            onBroadcastReceived(intent);
        }
    }

    //region new: My methods

    private void CalculateDistanceAndSetText() {
        if (publication.getLatitude() == 0 && publication.getLongitude() == 0) {
            tv_distance.setText(getResources().getString(R.string.pub_det_cant_get_distance));
            return;
        }
        tv_distance.setText(getResources().getString(R.string.pub_det_calculating_distance));
        StartGetMyLocation();
    }

    private void ChooseButtonPanel() {
        if (publication.isOwnPublication) {
            ll_button_panel_my.setVisibility(View.VISIBLE);
            ll_button_panel_others.setVisibility(View.GONE);
            btn_leave_report.setVisibility(View.GONE);
            btn_menu.setScaleType(ImageView.ScaleType.FIT_CENTER);
            btn_menu.setOnClickListener(this);
            if (publication.getIsOnAir()) {
                btn_facebook_my.setOnClickListener(this);
                btn_twitter_my.setOnClickListener(this);
                btn_call_reg.setOnClickListener(this);
                btn_sms_reg.setOnClickListener(this);
                btn_call_reg.setEnabled(false);
                btn_sms_reg.setEnabled(false);
            } else {
                btn_call_reg.setEnabled(false);
                btn_sms_reg.setEnabled(false);
                //todo: set gray drawables to buttons facebook and twitter
            }
        } else {
            ll_button_panel_my.setVisibility(View.GONE);
            ll_button_panel_others.setVisibility(View.VISIBLE);
            SetupRegisterUnregisterButton();
            btn_reg_unreg.setOnClickListener(this);
            btn_navigate.setOnClickListener(this);
            if (publication.getTypeOfCollecting() == FCTypeOfCollecting.ContactPublisher.ordinal()) {
                btn_call_owner.setOnClickListener(this);
                btn_sms_owner.setOnClickListener(this);
            } else {
                btn_call_owner.setImageDrawable(getResources().getDrawable(R.drawable.btn_call_inactive_pub_det));
                btn_sms_owner.setImageDrawable(getResources().getDrawable(R.drawable.btn_sms_inactive_pub_det));
                btn_call_owner.setEnabled(false);
                btn_sms_owner.setEnabled(false);
                //todo: change icon to gray - waiting for design from Olga
            }
            btn_menu.setVisibility(View.GONE);
            //todo: visibility/functions of this button depend if user registered to this publication
            btn_leave_report.setOnClickListener(this);
        }
    }

    private void SetupRegisterUnregisterButton() {
        Drawable image = getResources()
                .getDrawable((isRegisteredForCurrentPublication
                        ? R.drawable.cancel_rishum_pub_det_btn
                        : R.drawable.rishum_pub_det_btn));


        btn_reg_unreg.setImageDrawable(image);


        iv_num_of_reged.setImageDrawable(isRegisteredForCurrentPublication
                ? getResources().getDrawable(R.drawable.user_icon_green)
                : getResources().getDrawable(R.drawable.user_icon_blue));
        tv_num_of_reged.setTextColor(isRegisteredForCurrentPublication
                ? getResources().getColor(R.color.number_of_reged_users_green)
                : getResources().getColor(R.color.number_of_reged_users_blue));
        btn_leave_report.setVisibility(isRegisteredForCurrentPublication ? View.VISIBLE : View.GONE);
    }

    private boolean checkIfRegisteredForThisPublication() {
        if (publication == null || publication.getRegisteredForThisPublication() == null
                || publication.getRegisteredForThisPublication().size() == 0)
            return false;
        String imei = CommonUtil.GetIMEI(this);
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication()) {
            if (reg.getDevice_registered_uuid().compareTo(imei) == 0)
                return true;
        }
        return false;
    }

    private void SetImage() {
        int imageSize = getResources().getDimensionPixelSize(R.dimen.pub_details_image_size);
        Drawable image = CommonUtil.GetImageFromFileForPublication(
                this, publication.getUniqueId(), publication.getVersion(), publication.getPhotoUrl(), imageSize);
        if (image != null) {
            riv_image.setImageDrawable(image);
            riv_image.setOnClickListener(this);
        } else {
            riv_image.setImageDrawable(getResources().getDrawable(R.drawable.foodonet_logo_200_200));
        }

/*
        if (publication.getImageByteArray() != null && publication.getImageByteArray().length > 0) {
            int imageSize = getResources().getDimensionPixelSize(R.dimen.pub_details_image_size);
            bImage = CommonUtil.decodeScaledBitmapFromByteArray(publication.getImageByteArray(), imageSize, imageSize);
            Drawable image = new BitmapDrawable(bImage);//BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length)
            riv_image.setImageDrawable(image);
            riv_image.setOnClickListener(this);
        } else {
            bImage = BitmapFactory.decodeResource(getResources(), R.drawable.foodonet_logo_200_200);
            riv_image.setImageDrawable(getResources().getDrawable(R.drawable.foodonet_logo_200_200));
        }
*/
    }


    private void SetReportsList() {
        if (publication.getPublicationReports() == null
                || publication.getPublicationReports().size() == 0) {
            tv_no_reports.setVisibility(View.VISIBLE);
            lv_reports.setVisibility(View.GONE);
            return;
        }
        else
            tv_no_reports.setVisibility(View.GONE);


        adapter = new PublicationDetailsReportsAdapter(this,
                R.layout.pub_details_report_item, publication.getPublicationReports());
        lv_reports.setAdapter(adapter);
    }

    //endregion

    //region Facebook method
    private void PostOnFacebook() {

        Intent facebookIntent = new Intent(Intent.ACTION_SEND);
        String msg = publication.getTitle() + "\n " + getString(R.string.facebook_page_url) + "\n ";
        facebookIntent.putExtra(Intent.EXTRA_TEXT, msg);

        String fileName = publication.getUniqueId() + "." + publication.getVersion() + ".jpg";
        String imageSubFolder = getString(R.string.image_folder_path);
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        facebookIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
        facebookIntent.setType("image/*");
        //facebookIntent.setType("text/plain");

        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(facebookIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.facebook.katana")) {
                facebookIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivity(facebookIntent);
        } else {
            Toast.makeText(this, "Facebook app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana"));
            startActivity(intent);
        }

    }
    // endregion

    // region  Twitter method

    private void SendTweet() {

        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        String msg = getString(R.string.hashtag) + " : " + publication.getTitle() + "\n " +
                getString(R.string.facebook_page_url);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, msg);

        String fileName = publication.getUniqueId() + "." + publication.getVersion() + ".jpg";
        String imageSubFolder = getString(R.string.image_folder_path);
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        tweetIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
        tweetIntent.setType("text/plain");

        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivity(tweetIntent);
        } else {
            Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.twitter.android"));
            startActivity(intent);
        }
    }
    // endregion

    //region methods making parts of activity (to call in OnCreate)
/*
    private void makeBlueButtons()
    {
        btnCall = (Button)findViewById(R.id.btn_call);
        Drawable img_call = getResources().getDrawable( R.drawable.call_2x);
        img_call.setBounds(0, 0, 30, 30);
        btnCall.setCompoundDrawables(null, null, img_call, null);

        btnRishum = (Button)findViewById(R.id.btn_rishum);
        Drawable img_rishum = getResources().getDrawable( R.drawable.rishum_2x);
        img_rishum.setBounds(0, 0, 30, 30);
        btnRishum.setCompoundDrawables(null, null, img_rishum, null);

        btnSms = (Button)findViewById(R.id.btn_sms);
        Drawable img_sms = getResources().getDrawable( R.drawable.sms_2x);
        img_sms.setBounds(0, 0, 30, 30);
        btnSms.setCompoundDrawables(null, null, img_sms, null);

        btnNavigate = (Button)findViewById(R.id.btn_navigate);
        Drawable img_navigate = getResources().getDrawable( R.drawable.navigate_2x);
        img_navigate.setBounds(0, 0, 30, 30);
        btnNavigate.setCompoundDrawables(null, null, img_navigate, null);
    }


    private void makeCancelButton() {
        cancelPublicationButton        = (Button) findViewById(R.id.btn_cancel_publication);
        cancelPublicationButton.setText(getString(R.string.cancel_publication));
        cancelPublicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPublicationDialog.show();
            }
        });
    }
*/

    private void makeTheCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.cancel_publication);
        builder.setMessage(R.string.cancel_publication_question);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                PublicationDetailsActivity.this.finishWithCancelPublicationResult();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        cancelPublicationDialog = builder.create();
    }

    private void finishWithCancelPublicationResult() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra("userAction", "cancelPublication");
        finish();
    }

/*
    private void makeInterestedsList() {
        interestedPersonsCountTextView = (TextView) findViewById(R.id.interested_persons_count);
        interestedPersonsCountTextView.setText(getString(R.string.going_to_collect) + "  "
                + Integer.toString(publication.getRegisteredForThisPublication().size()));

        interestedsListView = (ListView)findViewById(R.id.lst_interested_persons_list);
        ArrayList<String> interestedPersonsInfoList = mapRegisteredPersonsToStringArray(
                publication.getRegisteredForThisPublication()
        );
        interestedsAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                interestedPersonsInfoList);
        interestedsListView.setAdapter(interestedsAdapter);
    }
*/

    private ArrayList<String> mapRegisteredPersonsToStringArray(ArrayList<RegisteredUserForPublication> arg) {
        ArrayList<String> l = new ArrayList<String>();
        for (RegisteredUserForPublication u : arg) {
            l.add(Integer.toString(u.getId()));
        }
        return l;
    }
    //endregion

    private void LoadPhoto(String photoUrlString) {
        if (photoUrlString == null || photoUrlString.isEmpty()) {
            Log.i(MY_TAG, "photo url was null or empty");
            return;
        }

        /*
        URL url = null;
        try {
            url = new URL(photoUrlString);
            photoBmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            Bitmap roundBmp = RoundedImageView.getRoundedCroppedBitmap(photoBmp, PHOTO_RADIUS);
            photoButton = (ImageButton)findViewById(R.id.photoButton);
            photoButton.setImageBitmap(roundBmp);
        } catch (MalformedURLException e) {
            Log.e(MY_TAG, "malformed photo url: " + e.getMessage());
        } catch (IOException e) {
            Log.e(MY_TAG, "error opening internet connection to photo url: " + e.getMessage());
        }

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " photo clicked!", Toast.LENGTH_LONG).show();
            }
        }); */

        //new DownloadImageTask(this).execute(photoUrlString);
    }

/*
    @Override
    public void OnImageDownloaded(Bitmap result) {
        this.photoBmp = result;
        Bitmap roundBmp = RoundedImageView.getRoundedCroppedBitmap(photoBmp, PHOTO_RADIUS);
        photoButton = (ImageButton)findViewById(R.id.btn_photoButton);
        photoButton.setImageBitmap(roundBmp);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " photo clicked!", Toast.LENGTH_LONG).show();
            }
        });
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_publication_details, menu);
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

    //region FooDoNetCustomActivityConnectedToService implementation

    @Override
    public void OnGooglePlayServicesCheckError() {
        //TODO
    }

    @Override
    public void OnInternetNotConnected() {
        //TODO
    }

    @Override
    public void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_SUCCESS:
                Location location = (Location) intent.getParcelableExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_LOCATION_KEY);
                if (location != null) {
                    tv_distance.setText(CommonUtil.GetDistanceString(new LatLng(location.getLatitude(), location.getLongitude()),
                            new LatLng(publication.getLatitude(), publication.getLongitude()), this));
                }
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_FAIL:
                tv_distance.setText("can't get distance");
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_SUCCESS:
                Log.i(MY_TAG, "successfully registered to publication " + publication.getUniqueId());
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL:
                Log.i(MY_TAG, "failed to register to publication");
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.pub_det_uimessage_failed_register_to_pub), Toast.LENGTH_LONG).show();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_UNREGISTER_FROM_PUBLICATION_SUCCESS:
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_UNREGISTER_FROM_PUBLICATION_FAIL:
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.pub_det_uimessage_failed_unregister_from_pub), Toast.LENGTH_LONG).show();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_ADD_MYSELF_TO_REGS_FOR_PUBLICATION:
                Log.i(MY_TAG, "successfully added myself to regs! refreshing number");
                isRegisteredForCurrentPublication = true;
                SetupRegisterUnregisterButton();
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                RestartNumOfRegedLoader();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION:
                Log.i(MY_TAG, "successfully removed myself from regs! refreshing number");
                isRegisteredForCurrentPublication = false;
                SetupRegisterUnregisterButton();
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                RestartNumOfRegedLoader();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REPORT_TO_PUBLICATION_SUCCESS:
                Log.i(MY_TAG, "successfully left report for publication!");
                isRegisteredForCurrentPublication = false;
                SetupRegisterUnregisterButton();
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                RestartNumOfRegedLoader();
                break;
        }
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_pending_broadcast), MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(getString(R.string.shared_preferences_pending_broadcast_value));
        editor.commit();
    }

    //endregion

    //region Click
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_menu_pub_details:
                //todo: implement menu switch depending on isOwn
                if (popup != null) {
                    popup.dismiss();
                    popup = null;
                }
                popup = new PopupMenu(this, v);
                popup.getMenuInflater().inflate(R.menu.pub_details_popup_menu, popup.getMenu());
                MenuItem itemEdit = popup.getMenu().getItem(0);
                SpannableString stringEdit = new SpannableString(getString(R.string.pub_det_menu_item_edit));
                stringEdit.setSpan(new ForegroundColorSpan(Color.WHITE), 0, stringEdit.length(), 0);
                itemEdit.setTitle(stringEdit);

                MenuItem itemTakeOffAir = popup.getMenu().getItem(1);
                SpannableString stringTakeOffAir = new SpannableString(getString(R.string.pub_det_menu_item_deactivate));
                stringTakeOffAir.setSpan(new ForegroundColorSpan(Color.WHITE), 0, stringTakeOffAir.length(), 0);
                itemTakeOffAir.setTitle(stringTakeOffAir);

                MenuItem itemDelete = popup.getMenu().getItem(2);
                SpannableString stringDelete = new SpannableString(getString(R.string.pub_det_menu_item_delete));
                stringDelete.setSpan(new ForegroundColorSpan(Color.WHITE), 0, stringDelete.length(), 0);
                itemDelete.setTitle(stringDelete);

/*
                MenuItem itemDeactivate = popup.getMenu().getItem(2);
                SpannableString stringDeactivate = new SpannableString("Deactivate");
                stringDeactivate.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, stringDeactivate.length(), 0);
                itemDeactivate.setTitle(stringDeactivate);
*/

                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
            case R.id.btn_leave_report_pub_details:

                if (!CheckInternetForAction(getString(R.string.action_leave_report)))
                    return;
                if (CheckIfMyLocationAvailableAndAskReportConfirmation())
                    ShowReportDialog();

                break;
            case R.id.btn_facebook_my_pub_details:
                growAnim(R.drawable.facebook_green_xxh, R.drawable.pub_det_facebook, btn_facebook_my);
                if (!CheckInternetForAction(getString(R.string.action_facebook)))
                    return;
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
                PostOnFacebook();
                break;
            case R.id.btn_navigate_pub_details:
                growAnim(R.drawable.navigate_green_xxh, R.drawable.navigate_pub_det_btn, btn_navigate);
                try {
                    String url = "waze://?ll=" + publication.getLatitude() + "," + publication.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                    startActivity(intent);
                }
                break;
            case R.id.btn_tweet_my_pub_details:
                growAnim(R.drawable.twitter_green_xxh, R.drawable.pub_det_twitter, btn_twitter_my);
                if (!CheckInternetForAction(getString(R.string.action_tweet)))
                    return;
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
                SendTweet();
                break;
            case R.id.btn_register_unregister_pub_details:
                growAnim(R.drawable.cancel_rishum_pub_det_btn, R.drawable.rishum_pub_det_btn, btn_reg_unreg);

                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.collector_details_dialog);
                Button cancel = (Button)dialog.findViewById(R.id.btn_cancel_dialog_collector);
                Button register = (Button)dialog.findViewById(R.id.btn_sign_dialog_collector);
                final EditText collectorName = (EditText)dialog.findViewById(R.id.et_name_dialog_collector);
                final EditText collectorPhone = (EditText)dialog.findViewById(R.id.et_phone_dialog_collector);
                collectorName.setText(GetContactInfoNameFromSharedPreferences());
                collectorPhone.setText(GetContactInfoPhoneFromSharedPreferences());

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        collectorNameAndPhonePutter(collectorName, collectorPhone);

                        if(collectorNameAndPhonevalidate){
                            if (!CheckInternetForAction(isRegisteredForCurrentPublication
                                    ? getString(R.string.action_unregister_from_pub)
                                    : getString(R.string.action_register_to_pub)))
                                return;
                            progressDialog = CommonUtil.ShowProgressDialog(PublicationDetailsActivity.this,
                                    isRegisteredForCurrentPublication
                                            ? getString(R.string.progress_unregistering_from_pub)
                                            : getString(R.string.progress_registration_to_pub));
                            RegisteredUserForPublication newRegistrationForPub
                                    = new RegisteredUserForPublication();
                            newRegistrationForPub.setDate_registered(new Date());
                            newRegistrationForPub.setDevice_registered_uuid(CommonUtil.GetIMEI(PublicationDetailsActivity.this));
                            newRegistrationForPub.setPublication_id(publication.getUniqueId());
                            newRegistrationForPub.setPublication_version(publication.getVersion());
                            if (isRegisteredForCurrentPublication) {
                                RegisterUnregisterReportService.startActionUnRegisterFromPub(PublicationDetailsActivity.this, newRegistrationForPub);
                            } else {
                                RegisterUnregisterReportService.startActionRegisterToPub(PublicationDetailsActivity.this, newRegistrationForPub);
                            }
                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();
                break;
            case R.id.btn_call_owner_pub_details:
                growAnim(R.drawable.call_green_xxh, R.drawable.pub_det_call, btn_call_reg);
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + publication.getContactInfo()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.btn_message_owner_pub_details:
                growAnim(R.drawable.sms_green_xxh, R.drawable.pub_det_sms, btn_sms_reg);
                Intent intentSMS = new Intent(Intent.ACTION_SENDTO);
                intentSMS.setType(HTTP.PLAIN_TEXT_TYPE);
                intentSMS.setData(Uri.parse("smsto:" + publication.getContactInfo()));// + publication.getContactInfo()));  // This ensures only SMS apps respond
                intentSMS.putExtra("sms_body", getString(R.string.pub_det_sms_default_text) + ": " + publication.getTitle());
                //intentSMS.putExtra(Intent.EXTRA_STREAM, attachment);
                if (intentSMS.resolveActivity(getPackageManager()) != null) {
                    startActivity(intentSMS);
                }
                break;
            case R.id.riv_image_pub_details:
                WindowManager manager = (WindowManager) getSystemService(PublicationDetailsActivity.WINDOW_SERVICE);
                int width, height;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                    width = manager.getDefaultDisplay().getWidth();
                    height = manager.getDefaultDisplay().getHeight();
                } else {
                    Point point = new Point();
                    manager.getDefaultDisplay().getSize(point);
                    width = point.x;
                    height = point.y;
                }
                Drawable imageD = CommonUtil.GetBitmapDrawableFromFile(
                        CommonUtil.GetFileNameByPublication(publication),
                        getString(R.string.image_folder_path), width, height);
                Intent intentFullSizeActivity = new Intent(PublicationDetailsActivity.this, FullSizeImgActivity.class);
                intentFullSizeActivity.putExtra("fileName", publication.getUniqueId() + "." + publication.getVersion() + ".jpg");
                startActivity(intentFullSizeActivity);
                break;
        }
    }
public boolean collectorNameAndPhonevalidate = false;
public void collectorNameAndPhonePutter(EditText name,EditText phone){
    if(collectorNameValidate(name)&&collectorPhoneValidate(phone)){
        PutNameAndPhoneToSharedPreferences(name.getText().toString(),phone.getText().toString());
        collectorNameAndPhonevalidate = true;
    }
    else {collectorNameAndPhonevalidate = false;}
}

public boolean collectorNameValidate(EditText collectorName){
    if (collectorName.getText().toString().length() < 2) {
        CommonUtil.SetEditTextIsValid(this, collectorName, false);
        Toast.makeText(this, getString(R.string.collector_details_name_too_short), Toast.LENGTH_LONG).show();
    return false;}
    else{
        CommonUtil.SetEditTextIsValid(this, collectorName, true);
        return true;
    }
}
    public boolean collectorPhoneValidate(EditText collectorPhone){
        if (collectorPhone.getText().toString().length() < 10) {
            CommonUtil.SetEditTextIsValid(this, collectorPhone, false);
            Toast.makeText(this, getString(R.string.collector_details_phone_too_short), Toast.LENGTH_LONG).show();
            return false;
        }
       /* else if(!CommonUtil.CheckPhoneNumberString(this,collectorPhone.toString())){
            CommonUtil.SetEditTextIsValid(this, collectorPhone, false);
            Toast.makeText(this, getString(R.string.collector_details_phone_incorrect), Toast.LENGTH_LONG).show();
            return false;
        }*/
        else{
            CommonUtil.SetEditTextIsValid(this, collectorPhone, true);

        return true;}
    }

    private void PutNameAndPhoneToSharedPreferences(String name, String phoneNum){
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_contact_info), MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(sp.contains(getString(R.string.shared_preferences_contact_info_name)))
            editor.remove(getString(R.string.shared_preferences_contact_info_name));
        if(sp.contains(getString(R.string.shared_preferences_contact_info_phone)))
            editor.remove(getString(R.string.shared_preferences_contact_info_phone));
        editor.putString(getString(R.string.shared_preferences_contact_info_name), name);
        editor.putString(getString(R.string.shared_preferences_contact_info_phone), phoneNum);
        editor.commit();
    }

    private String GetContactInfoNameFromSharedPreferences(){
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_contact_info), MODE_PRIVATE);
        return sp.getString(getString(R.string.shared_preferences_contact_info_name), "");
    }

    private String GetContactInfoPhoneFromSharedPreferences(){
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_contact_info), MODE_PRIVATE);
        return sp.getString(getString(R.string.shared_preferences_contact_info_phone), "");
    }

    private boolean CheckIfMyLocationAvailableAndAskReportConfirmation() {
        LatLng myLocation = CommonUtil.GetFilterLocationFromPreferences(this);
        if (myLocation.latitude == -1000 || myLocation.longitude == -1000)
            return true;
        double distance = CommonUtil.GetDistanceInKM(new LatLng(publication.getLatitude(), publication.getLongitude()), myLocation);
        if (distance < 2)
            return true;
        else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            ShowReportDialog();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            return;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Distance > 2km, leave report?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pub_det_menu_item_edit:
                if (!CheckInternetForAction(getString(R.string.action_edit_publication)))
                    return false;
                Intent intent = new Intent(this, AddEditPublicationActivity.class);
                intent.putExtra(AddEditPublicationActivity.PUBLICATION_KEY, publication);
                startActivityForResult(intent, REQUEST_CODE_EDIT_PUBLICATION);

                break;
            case R.id.pub_det_menu_item_deactivate:
                if (!CheckInternetForAction(getString(R.string.action_take_off_air)))
                    return false;
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_taking_pub_off_air));
                publication.setIsOnAir(false);
                HttpServerConnectorAsync connector1
                        = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                String subPath = getString(R.string.server_edit_publication_path);
                subPath = subPath.replace("{0}", String.valueOf(publication.getUniqueId()));
                InternalRequest ir1
                        = new InternalRequest(InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR,
                        subPath, publication);
                ir1.publicationForSaving = publication;
                connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir1);
                break;
            case R.id.pub_det_menu_item_delete:
                if (!CheckInternetForAction(getString(R.string.action_delete)))
                    return false;
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                DeletePublication();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                return;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.confirmAction))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();


                //Toast.makeText(this, "delete publication not implemented for now", Toast.LENGTH_LONG).show();
                break;
        }
        //Toast.makeText(getBaseContext(), "You selected the action : " + item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void DeletePublication(){
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_delete_pub));
        HttpServerConnectorAsync connector2
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
        String subPath1 = getString(R.string.server_edit_publication_path);
        subPath1 = subPath1.replace("{0}", String.valueOf(publication.getUniqueId()));
        InternalRequest ir2 = new InternalRequest(InternalRequest.ACTION_DELETE_PUBLICATION, subPath1);
        connector2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir2);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand) {
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                Toast.makeText(this, (response.Status == InternalRequest.STATUS_OK
                        ? getString(R.string.action_succeeded)
                        : getString(R.string.action_failed)).replace("{0}",
                        getString(R.string.action_delete)), Toast.LENGTH_LONG).show();
                FooDoNetSQLExecuterAsync sqlExecutor
                        = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                sqlExecutor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        new InternalRequest(response.ActionCommand, publication));
                break;
            case InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR:
                if (response.Status == InternalRequest.STATUS_OK) {
                    publication.setIsOnAir(response.Status == InternalRequest.STATUS_FAIL);
                    getContentResolver().update(Uri.parse(
                                    FooDoNetSQLProvider.CONTENT_URI + "/" + publication.getUniqueId()),
                            publication.GetContentValuesRow(), null, null);
                } else
                    Toast.makeText(this, getString(R.string.action_failed).replace("{0}", getString(R.string.action_delete)), Toast.LENGTH_LONG).show();
                if (progressDialog != null)
                    progressDialog.dismiss();
                break;
        }
    }

    //endregion

    //region Loader

    private static final int LOADER_ID_NUM_OF_REGED = 0;

    private void StartNumOfRegedLoader() {
        if (publication.getUniqueId() > 0)
            getSupportLoaderManager().initLoader(LOADER_ID_NUM_OF_REGED, null, this);
    }

    private void RestartNumOfRegedLoader() {
        if (publication.getUniqueId() > 0)
            getSupportLoaderManager().restartLoader(LOADER_ID_NUM_OF_REGED, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = RegisteredUserForPublication.GetColumnNamesArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this,
                Uri.parse(FooDoNetSQLProvider.URI_GET_REGISTERED_BY_PUBLICATION_ID + "/" + publication.getUniqueId()),
                projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) return;
        ArrayList<RegisteredUserForPublication> regs
                = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(data);
        if (tv_num_of_reged != null) {
            tv_num_of_reged.setText(String.valueOf(regs != null ? regs.size() : 0));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    //endregion

    //region Report dialog

    private void ShowReportDialog() {
        final Dialog reportDialog = new Dialog(this);
        reportDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        reportDialog.setContentView(R.layout.report_dialog);

        Button btn_whole = (Button) reportDialog.findViewById(R.id.btn_report_dialog_collected_part);
        Button btn_half = (Button) reportDialog.findViewById(R.id.btn_report_dialog_collected_all);
        Button btn_nothing = (Button) reportDialog.findViewById(R.id.btn_report_dialog_found_nothing);
        Button btn_cancel = (Button) reportDialog.findViewById(R.id.btn_report_dialog_found_nothing);
        Button btn_report = (Button) reportDialog.findViewById(R.id.btn_report_dialog_found_nothing);

        ReportButtonListener listener = new ReportButtonListener(reportDialog, this);
        btn_whole.setOnClickListener(listener);
        btn_half.setOnClickListener(listener);
        btn_nothing.setOnClickListener(listener);
        btn_cancel.setOnClickListener(listener);
        btn_report.setOnClickListener(listener);

        reportDialog.show();

    }

    public void ReportMade(int reportID) {
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_leaving_report));
        PublicationReport report = new PublicationReport();
        report.setReport(reportID);
        report.setPublication_id(publication.getUniqueId());
        report.setPublication_version(publication.getVersion());
        report.setDevice_uuid(CommonUtil.GetIMEI(this));
        report.setDate_reported(new Date());

        RegisterUnregisterReportService.startActionReportForPublication(this, report);
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                CommonUtil.RemoveImageByPublication(publication, this);
                if(progressDialog != null)
                    progressDialog.dismiss();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(DETAILS_ACTIVITY_RESULT_KEY, InternalRequest.ACTION_DELETE_PUBLICATION);
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
        }
    }

    class ReportButtonListener implements View.OnClickListener {

        public int reportId = -1;
        private Dialog dialog;
        private PublicationDetailsActivity callback;

        public ReportButtonListener(Dialog dialog, PublicationDetailsActivity callback) {
            this.dialog = dialog;
            this.callback = callback;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_report_dialog_collected_part:
                    reportId = 1;
                    break;
                case R.id.btn_report_dialog_collected_all:
                    reportId = 3;
                    break;
                case R.id.btn_report_dialog_found_nothing:
                    //reportId = 5;
                    break;
                case R.id.btn_report_dialog_cancel:
                        dialog.dismiss();
                    break;
                case R.id.btn_report_dialog_report:

                    break;
            }
            dialog.dismiss();
            callback.ReportMade(reportId);
        }
    }

    public void growAnim(final int iconGreen, final int iconBlue, final ImageButton btn) {
        ScaleAnimation grow = new ScaleAnimation(1, 1.2f, 1, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        grow.setDuration(200);
        btn.startAnimation(grow);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                btn.setImageDrawable(getResources().getDrawable(iconGreen));

            }
        }, 100);

        //final Handler handlerb = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                btn.setImageDrawable(getResources().getDrawable(iconBlue));

            }
        }, 200);

    }

    private void startEndTimeSet() {
        tv_start_dateTime_details.setText(tv_start_dateTime_details.getText() + " "
                + CommonUtil.GetDateTimeStringFromGate(publication.getStartingDate()));
        tv_end_dateTime_details.setText(tv_end_dateTime_details.getText() + " "
                + CommonUtil.GetDateTimeStringFromGate(publication.getEndingDate()));
    }
    //endregion
}

