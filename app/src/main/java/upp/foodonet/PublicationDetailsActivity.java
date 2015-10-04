package upp.foodonet;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import Adapters.PublicationDetailsReportsAdapter;
import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;
import UIUtil.RoundedImageView;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;


public class PublicationDetailsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String PUBLICATION_PARAM = "publication";
    public static final String IS_OWN_PUBLICATION_PARAM = "is_own";
    private static final String MY_TAG = "food_PubDetails";

    public static final int REQUEST_CODE_POST_FACEBOOK = 64206;
    public static final int REQUEST_CODE_EDIT_PUBLICATION = 1;
    private static final String PERMISSION = "publish_actions";

    private FCPublication publication;

    private AlertDialog cancelPublicationDialog;
    private Bitmap photoBmp;
    private boolean isOwnPublication;
    private boolean isRegisteredForCurrentPublication = false;

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

    LinearLayout ll_button_panel_my;
    LinearLayout ll_button_panel_others;

    PublicationDetailsReportsAdapter adapter;

    PopupMenu popup;
    Bitmap bImageFacebook;
    // Twitter
    InputStream isImageTwitter;
    private final Handler mTwitterHandler = new Handler();
    final Runnable mUpdateTwitterNotification = new Runnable() {
        public void run() {
            Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
        }
    };

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
        ll_button_panel_my = (LinearLayout) findViewById(R.id.ll_my_pub_dets_buttons_panel);
        ll_button_panel_others = (LinearLayout) findViewById(R.id.ll_others_pub_dets_buttons_panel);

        tv_title.setText(publication.getTitle());
        tv_subtitle.setText(publication.getSubtitle());//publication.getSubtitle());
        tv_address.setText(publication.getAddress());
        SetImage();
/* replaced by loader
        if(publication.getRegisteredForThisPublication() != null)
            tv_num_of_reged.setText(String.valueOf(publication.getRegisteredForThisPublication().size()));
        else
            tv_num_of_reged.setText("0");
*/
        StartNumOfRegedLoader();
        CalculateDistanceAndSetText();
        ChooseButtonPanel();
        SetReportsList();
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
            if (requestCode == REQUEST_CODE_POST_FACEBOOK) {
                super.onActivityResult(requestCode, resultCode, data);
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
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
        setResult(RESULT_OK);
        finish();
    }

    //region new: My methods

    private void CalculateDistanceAndSetText() {
        //todo:
        // 1. set default text "calculating.."
        // 2. call get last position async
        // 3. update text of tv_distance
        if (publication.getLatitude() == 0 && publication.getLongitude() == 0) {
            tv_distance.setText(getResources().getString(R.string.pub_det_cant_get_distance));
            return;
        }
        tv_distance.setText(getResources().getString(R.string.pub_det_calculating_distance));
        //GetMyLocationAsync getLocationTask = new GetMyLocationAsync((LocationManager) getSystemService(LOCATION_SERVICE), this);
        //getLocationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        StartGetMyLocation();
    }

    private void ChooseButtonPanel() {
        if (publication.isOwnPublication) {
            ll_button_panel_my.setVisibility(View.VISIBLE);
            ll_button_panel_others.setVisibility(View.GONE);
            btn_facebook_my.setOnClickListener(this);
            btn_twitter_my.setOnClickListener(this);
            btn_call_reg.setOnClickListener(this);
            btn_sms_reg.setOnClickListener(this);
            btn_leave_report.setVisibility(View.GONE);
            btn_menu.setScaleType(ImageView.ScaleType.FIT_CENTER);
            btn_menu.setOnClickListener(this);
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
        Drawable image = CommonUtil.GetBitmapDrawableFromFile(
                publication.getUniqueId() + "." + publication.getVersion() + ".jpg", imageSize, imageSize);
        if (image != null) {
            // TODO - move init of facebook and twitter pictures
            bImageFacebook = ((BitmapDrawable) image).getBitmap();
            isImageTwitter = CommonUtil.ConvertFileToInputStream(publication.getUniqueId() + "." + publication.getVersion() + ".jpg");
            riv_image.setImageDrawable(image);
            riv_image.setOnClickListener(this);
        } else {
            bImageFacebook = BitmapFactory.decodeResource(getResources(), R.drawable.foodonet_logo_200_200);//Facebook
            isImageTwitter = getResources().openRawResource(+ R.drawable.no_photo_placeholder);
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
        adapter = new PublicationDetailsReportsAdapter(this,
                R.layout.pub_details_report_item, publication.getPublicationReports());
        lv_reports.setAdapter(adapter);
    }

    //endregion

    //region Facebook methods
    private void PostOnFacebook()
    {
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        List<String> permissionNeeds = Arrays.asList(PERMISSION);

        //this loginManager helps you eliminate adding a LoginButton to your UI
        loginManager = LoginManager.getInstance();

        loginManager.logInWithPublishPermissions(PublicationDetailsActivity.this, permissionNeeds);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                sharePhotoToFacebook();

            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
            }
        });

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);

        canPresentShareDialogWithPhotos = ShareDialog.canShow(SharePhotoContent.class);

    }

    private void sharePhotoToFacebook() {
        //Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.foodonet_logo_200_200);
        if (bImageFacebook != null) {
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bImageFacebook)
                            //.setCaption("יש לי " + tv_title.getText() + ". מי בא לקחת? " + "\n" + Uri.parse("https://www.facebook.com/foodonet"))
                    .build();


            SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            // Share with dialog
            if (canPresentShareDialogWithPhotos)
                shareDialog.show(sharePhotoContent);

//            ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                    .setContentTitle("Hello Facebook")
//                    .setContentDescription("יש לי " + tv_title.getText() + ". מי בא לקחת?")
//                    .setContentUrl(Uri.parse("https://www.facebook.com/foodonet"))
//                    .build();

            // Share without dialog
//            if (hasPublishPermission())
//                ShareApi.share(content, shareCallback);


//            if (canPresentShareDialogWithPhotos) {
//                shareDialog.show(content);
//            } else if (hasPublishPermission()) {
//                ShareApi.share(content, shareCallback);
//            }
        }
    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains(PERMISSION);
    }
    // endregion

    // region Twitter methods
    public void SendTweet() {
        Thread t = new Thread() {
            public void run() {

                try {
                    //InputStream isImageTwitter = getResources().openRawResource(R.drawable.foodonet_logo_200_200);
                    PostTweet(getTweetMsg(), isImageTwitter);
                    mTwitterHandler.post(mUpdateTwitterNotification);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        };
        t.start();
    }


    public void PostTweet(String msg, InputStream is) throws Exception {
        String token = getString(R.string.access_token);
        String secret = getString(R.string.access_token_secret);
        twitter4j.auth.AccessToken a = new twitter4j.auth.AccessToken(token,secret);
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));
        twitter.setOAuthAccessToken(a);

        twitter.getAccountSettings();
        // Update status
        StatusUpdate statusUpdate = new StatusUpdate(msg);
        statusUpdate.setMedia("test.jpg", is);

        twitter.updateStatus(statusUpdate);
    }

    private String getTweetMsg() {
        return getString(R.string.hashtag) + " : " + getString(R.string.tweet_text) + " " + publication.getTitle() + ". " +
                getString(R.string.tweet_question) + " " + new Date().toLocaleString() + "\n " +
                getString(R.string.facebook_page_url);
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
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.pub_det_uimessage_successfully_registered_to_pub), Toast.LENGTH_LONG);
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL:
                Log.i(MY_TAG, "failed to register to publication");
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.pub_det_uimessage_failed_register_to_pub), Toast.LENGTH_LONG);
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_ADD_MYSELF_TO_REGS_FOR_PUBLICATION:
                Log.i(MY_TAG, "successfully added myself to regs! refreshing number");
                isRegisteredForCurrentPublication = true;
                SetupRegisterUnregisterButton();
                RestartNumOfRegedLoader();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION:
                Log.i(MY_TAG, "successfully removed myself from regs! refreshing number");
                isRegisteredForCurrentPublication = false;
                SetupRegisterUnregisterButton();
                RestartNumOfRegedLoader();
                break;
        }
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
                //TODO: implement
                MenuItem itemEdit = popup.getMenu().getItem(0);
                SpannableString stringEdit = new SpannableString(getString(R.string.pub_det_menu_item_edit));
                stringEdit.setSpan(new ForegroundColorSpan(Color.WHITE), 0, stringEdit.length(), 0);
                itemEdit.setTitle(stringEdit);

                MenuItem itemDelete = popup.getMenu().getItem(1);
                SpannableString stringDelete = new SpannableString(getString(R.string.pub_det_menu_item_deactivate));
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
                ShowReportDialog();
                break;
            case R.id.btn_facebook_my_pub_details:
                PostOnFacebook();
                break;
            case R.id.btn_navigate_pub_details:
                //todo
                break;
            case R.id.btn_tweet_my_pub_details:
                SendTweet();
                break;
            case R.id.btn_register_unregister_pub_details:
                RegisteredUserForPublication newRegistrationForPub
                        = new RegisteredUserForPublication();
                newRegistrationForPub.setDate_registered(new Date());
                newRegistrationForPub.setDevice_registered_uuid(CommonUtil.GetIMEI(this));
                newRegistrationForPub.setPublication_id(publication.getUniqueId());
                newRegistrationForPub.setPublication_version(publication.getVersion());
                if (isRegisteredForCurrentPublication) {
                    RegisterUnregisterReportService.startActionUnRegisterFromPub(this, newRegistrationForPub);
                } else {
                    RegisterUnregisterReportService.startActionRegisterToPub(this, newRegistrationForPub);
                }
                break;
            case R.id.btn_call_owner_pub_details:
                //todo
                break;
            case R.id.btn_message_owner_pub_details:
                //todo
                break;
            case R.id.riv_image_pub_details:
                //todo
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pub_det_menu_item_edit:
                Intent intent = new Intent(this, AddEditPublicationActivity.class);
                intent.putExtra(AddEditPublicationActivity.PUBLICATION_KEY, publication);
                startActivityForResult(intent, REQUEST_CODE_EDIT_PUBLICATION);

                break;
            case R.id.pub_det_menu_item_deactivate:
                break;
        }
        //Toast.makeText(getBaseContext(), "You selected the action : " + item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
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

    //region facebook

    // For Facebook
    private boolean canPresentShareDialogWithPhotos;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private ShareDialog shareDialog;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("Facebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("Facebook", String.format("Error: %s", error.toString()));
            String message = error.getMessage();
            showResult(message);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("Facebook", "Success!");
            if (result.getPostId() != null) {
                //String id = result.getPostId();
                String message = getString(R.string.successfully_posted_post);
                showResult(message);
            }
        }

        private void showResult(String message) {
            Toast.makeText(PublicationDetailsActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
        }
    };

    //endregion

    //region Report dialog

    private void ShowReportDialog() {
        final Dialog reportDialog = new Dialog(this);
        reportDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        reportDialog.setContentView(R.layout.select_report_dialog);

        Button btn_whole = (Button) reportDialog.findViewById(R.id.btn_whole_report_dialog);
        Button btn_half = (Button) reportDialog.findViewById(R.id.btn_half_report_dialog);
        Button btn_few = (Button) reportDialog.findViewById(R.id.btn_few_report_dialog);

        ReportButtonListener listener = new ReportButtonListener(reportDialog, this);
        btn_whole.setOnClickListener(listener);
        btn_half.setOnClickListener(listener);
        btn_few.setOnClickListener(listener);

        reportDialog.show();

    }

    public void ReportMade(int reportID) {
        PublicationReport report = new PublicationReport();
        report.setReport(String.valueOf(reportID));
        report.setPublication_id(publication.getUniqueId());
        report.setPublication_version(publication.getVersion());
        report.setDevice_uuid(CommonUtil.GetIMEI(this));
        report.setDate_reported(new Date());

        RegisterUnregisterReportService.startActionReportForPublication(this, report);
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
                case R.id.btn_whole_report_dialog:
                    reportId = 1;
                    break;
                case R.id.btn_half_report_dialog:
                    reportId = 3;
                    break;
                case R.id.btn_few_report_dialog:
                    reportId = 5;
                    break;
            }
            dialog.dismiss();
            callback.ReportMade(reportId);
        }
    }

    //endregion
}
