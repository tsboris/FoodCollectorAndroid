package upp.foodonet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Adapters.PlaceArrayAdapter;
import Adapters.PreviousAddressesHashMapAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.GetMyLocationAsync;
import CommonUtilPackage.IGotMyLocationCallback;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;


public class AddEditPublicationActivity extends FragmentActivity
        implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnTouchListener, DialogInterface.OnDismissListener, DialogInterface.OnCancelListener, IGotMyLocationCallback, AdapterView.OnItemClickListener, TextWatcher {

    private static final String MY_TAG = "food_newPublication";
    public static final String PUBLICATION_KEY = "publication";

    //region Variables

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;

    //private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private EditText et_publication_title;
    private EditText et_address;

    private EditText et_subtitle;
    //private TextView mAddressTextView;
    private TextView mIdTextView;
    private EditText et_additional_info;
    private TextView mWebTextView;
    private TextView mAttTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_ISRAEL_VIEW = new LatLngBounds(
            new LatLng(29.525670, 34.991455), new LatLng(33.252470, 35.897827));

    private Button btn_date_start;
    private Button btn_date_end;

    private static CheckBox chkCallToPublisher;

    private ImageView mAddPicImageView;
    private Uri imageURI;

    private static ImageButton submitButton, cameraBtn;

    private Date mDate;

    //address picker
    private AutoCompleteTextView atv_address;
    private CheckBox cb_use_my_current_location;
    private ImageView iv_address_dialog_location_validation;
    private Button btn_address_dialog_ok;
    private Button btn_address_dialog_cancel;
    private boolean addressDialogStarted = false;
    private Dialog addressDialog;
    private ProgressDialog progressDialog;
    private ListView prevAddressesList;
    private PreviousAddressesHashMapAdapter prevAddressAdapter;
    private boolean isGoogleAddress;

    private static FCPublication publication;
    private static FCPublication publicationOldVersion;

    private Date startDate;
    private Date endDate;

    private boolean isNew = false;

    private double latitude = -1000;
    private double latitudeTmpForEdit = -1000;
    private double longitude = -1000;
    private double longitudeTmpForEdit = -1000;
    private String address = "";
    private String addressTmpForEdit = "";
    Context context = this;

    //endregion

    //region Activity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_publication);


        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            publication = new FCPublication();
            publication.setLatitude(latitude);
            publication.setLongitude(longitude);
            publication.setAddress(address);
            isNew = true;
        } else {
            publication = (FCPublication) extras.get(PUBLICATION_KEY);
            isNew = false;
        }
        publicationOldVersion = new FCPublication(publication);

        et_publication_title = (EditText) findViewById(R.id.et_title_new_publication);
        et_publication_title.setOnClickListener(this);

        et_address = (EditText) findViewById(R.id.et_address_edit_add_pub);
        et_address.setOnClickListener(this);
        et_address.setOnTouchListener(this);

        et_additional_info = (EditText) findViewById(R.id.et_additional_info_add_edit_pub);
        et_additional_info.setInputType(InputType.TYPE_CLASS_NUMBER);
        et_subtitle = (EditText) findViewById(R.id.et_subtitle_add_edit_pub);

        if (!isNew) {
            et_publication_title.setText(publication.getTitle());
            et_subtitle.setText(publication.getSubtitle());
            et_additional_info.setText(publication.getContactInfo());
            et_address.setText(publication.getAddress());
//            String imagePath = CommonUtil.GetFileNameByPublication(publication);
//            imagePath = Environment.getExternalStorageDirectory() + getString(R.string.image_folder_path) + "/" + imagePath;
//            Bitmap bm = CommonUtil.decodeScaledBitmapFromSdCard(imagePath, 200, 200);
//            mAddPicImageView.setImageBitmap(bm);
        }

        // OnClickListener for the Date button, calls showDatePickerDialog() to show the Date dialog
        btn_date_start = (Button) findViewById(R.id.btn_start_date_time_add_pub);
        btn_date_end = (Button) findViewById(R.id.btn_end_date_time_add_pub);
        btn_date_start.setOnClickListener(this);
        btn_date_end.setOnClickListener(this);

        if (isNew || publication.getEndingDate().compareTo(new Date()) <= 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            startDate = calendar.getTime();
            publication.setStartingDate(startDate);
            publicationOldVersion.setStartingDate(startDate);
            calendar.add(Calendar.DATE, 1);
            endDate = calendar.getTime();
            publication.setEndingDate(endDate);
            publicationOldVersion.setEndingDate(endDate);
        } else {
            startDate = publication.getStartingDate();
            endDate = publication.getEndingDate();
        }
        setDateTimeTextToButton(R.id.btn_start_date_time_add_pub);
        setDateTimeTextToButton(R.id.btn_end_date_time_add_pub);

        // Set the default date and time
        //setDefaultDateTime();

        //chkCallToPublisher = (CheckBox) findViewById(R.id.chkCallToPublisher);
        //chkCallToPublisher.setOnClickListener(this);

        mAddPicImageView = (ImageView) findViewById(R.id.imgAddPicture);
        mAddPicImageView.setOnClickListener(this);

        if (!isNew) {
            TryLoadExistingImage();
        }

        cameraBtn = (ImageButton) findViewById(R.id.btn_camera_add_pub);
        cameraBtn.setOnClickListener(this);

        submitButton = (ImageButton) findViewById(R.id.publishButton);
        submitButton.setOnClickListener(this);

        ArrangePublicationFromInput(publicationOldVersion);

        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_ISRAEL_VIEW, null);
        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(AddEditPublicationActivity.this)
                    .addApi(Places.GEO_DATA_API)
                    .enableAutoManage(AddEditPublicationActivity.this, GOOGLE_API_CLIENT_ID, AddEditPublicationActivity.this)
                    .addConnectionCallbacks(this)
                    .build();
    }

    @Override
    protected void onPause() {
        if (progressDialog != null)
            progressDialog.dismiss();
        super.onPause();
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_fcpublication, menu);
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
        ArrangePublicationFromInput(publication);
        if (!publication.IsEqualTo(publicationOldVersion)) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "add edit pub", "back btn", "exit without saving");
                            ForceReturn();
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
        } else {
            CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "add edit pub", "back btn", "exit without saving");
            ForceReturn();
        }
    }

    private void ForceReturn() {
        super.onBackPressed();
    }

    //endregion

    //region image

    private void TryLoadExistingImage() {
        int imageSize = mAddPicImageView.getLayoutParams().height;
        Drawable imageDrawable = CommonUtil.GetBitmapDrawableFromFile(
                CommonUtil.GetFileNameByPublication(publication),
                getString(R.string.image_folder_path), imageSize, imageSize);
        if (imageDrawable != null)
            mAddPicImageView.setImageDrawable(imageDrawable);
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEditPublicationActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    // For image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(MY_TAG, "Entered onActivityResult()");

        if (resultCode == RESULT_OK) {
            publication.pictureWasChangedDuringEditing = true;
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                thumbnail = CommonUtil.CompressBitmapByMaxSize(thumbnail,
                        getResources().getInteger(R.integer.max_image_width_height));
                publication.setImageByteArrayFromBitmap(thumbnail);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File destination = new File(Environment.getExternalStorageDirectory()
                        + getResources().getString(R.string.image_folder_path),
                        System.currentTimeMillis() + getString(R.string.file_name_part_just_shot) + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    publication.setPhotoUrl(destination.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAddPicImageView.setImageBitmap(thumbnail);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                if (cursor == null)
                    throw new NullPointerException("can't get picture cursor, critical error");
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                publication.setPhotoUrl(selectedImagePath);
                Bitmap bm = CommonUtil.decodeScaledBitmapFromSdCard(selectedImagePath, 200, 200);
                mAddPicImageView.setImageBitmap(bm);
                publication.setImageByteArrayFromBitmap(bm);
            }
        }
        if (resultCode == RESULT_CANCELED) {

        }
    }

    //endregion

    //region Address autocomplete

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (progressDialog != null)
                progressDialog.dismiss();
            progressDialog = CommonUtil.ShowProgressDialog(context, getString(R.string.progress_loading));
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(MY_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(MY_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(MY_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            // mNameTextView.setText(Html.fromHtml(place.getName() + ""));
            String address = Html.fromHtml(place.getAddress() + "").toString();
            //mAddressTextView.setText(address);
            longitudeTmpForEdit = getLongitudeFromAddress(address);
            latitudeTmpForEdit = getLatitudeFromAddress(address);
            isGoogleAddress = true;
            if (cb_use_my_current_location != null)
                cb_use_my_current_location.setChecked(false);
            if (iv_address_dialog_location_validation != null)
                iv_address_dialog_location_validation.setVisibility(View.GONE);

            addressTmpForEdit = atv_address.getText().toString();
            if (progressDialog != null)
                progressDialog.dismiss();
//            mIdTextView.setText(Html.fromHtml(place.getId() + ""));
//            mPhoneTextView.setText(Html.fromHtml(place.getPhoneNumber() + ""));
//            mWebTextView.setText(place.getWebsiteUri() + "");
//            if (attributions != null) {
//                mAttTextView.setText(Html.fromHtml(attributions.toString()));
//            }
        }
    };

    public double getLatitudeFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double latitude = 0;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                Address location = address.get(0);
                latitude = location.getLatitude();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return latitude;
    }

    public double getLongitudeFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double longitude = 0;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                Address location = address.get(0);
                longitude = location.getLongitude();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return longitude;
    }

    public String GetCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;

        geocoder = new Geocoder(this, new Locale("he"));
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        return address + "," + city;
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (mPlaceArrayAdapter != null) {
            mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
            Log.i(MY_TAG, "Google Places API connected.");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(MY_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(getBaseContext(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mPlaceArrayAdapter != null) {
            mPlaceArrayAdapter.setGoogleApiClient(null);
            Log.e(MY_TAG, "Google Places API connection suspended.");
        }
    }

    //endregion

    //region DateTime methods and picker dialog

    private void setDateTimeTextToButton(int id) {
        switch (id) {
            case R.id.btn_start_date_time_add_pub:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                btn_date_start.setText(CommonUtil.GetDateTimeStringFromCalendar(calendar));
                break;
            case R.id.btn_end_date_time_add_pub:
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(endDate);
                btn_date_end.setText(CommonUtil.GetDateTimeStringFromCalendar(calendar1));
                break;
        }
    }

    private void showDatePickerDialog(int btnId) {

        final int id = btnId;

        final Dialog dtpDialog = new Dialog(this);
        dtpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dtpDialog.setContentView(R.layout.date_time_picker_dialog);

        //dtpDialog.setTitle(getResources().getString(R.string.date_time_picker_dialog_title));
        final DatePicker dp = (DatePicker) dtpDialog.findViewById(R.id.dp_date_time_dialog);
        final TimePicker tp = (TimePicker) dtpDialog.findViewById(R.id.tp_date_time_dialog);
        Button btnOk = (Button) dtpDialog.findViewById(R.id.btn_ok_date_time_dialog);
        Button btnCancel = (Button) dtpDialog.findViewById(R.id.btn_cancel_date_time_dialog);
        TextView tvTitle = (TextView) dtpDialog.findViewById(R.id.tv_date_time_picker_title);

        final Calendar calendar = Calendar.getInstance();
        switch (btnId) {
            case R.id.btn_start_date_time_add_pub:
                calendar.setTime(startDate);
                break;
            case R.id.btn_end_date_time_add_pub:
                calendar.setTime(endDate);
                break;
        }

        dp.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dp.setCalendarViewShown(false);
        tp.setIs24HourView(true);
        tp.setCurrentHour(calendar.get(Calendar.HOUR));
        tp.setCurrentMinute(calendar.get(Calendar.MINUTE));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar resCalendar = Calendar.getInstance();
                resCalendar.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                int h = tp.getCurrentHour();
                int h1 = resCalendar.get(Calendar.HOUR);
                int h2 = resCalendar.get(Calendar.HOUR_OF_DAY);
                resCalendar.set(Calendar.HOUR, tp.getCurrentHour());
                resCalendar.set(Calendar.MINUTE, tp.getCurrentMinute());
                switch (id) {
                    case R.id.btn_start_date_time_add_pub:
                        startDate = resCalendar.getTime();
                        break;
                    case R.id.btn_end_date_time_add_pub:
                        endDate = resCalendar.getTime();
                        break;
                }
                setDateTimeTextToButton(id);
                dtpDialog.dismiss();
            }
        });
        dtpDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtpDialog.dismiss();
            }
        });
    }

    //endregion

    //region Address dialog

    private void ShowAddressDialog() {

        if (publication.getAddress().toString().length() != 0) {
            address = publication.getAddress();
            addressTmpForEdit = publication.getAddress();
        }

        if (publication.getLatitude() != -1000) {
            latitude = publication.getLatitude();
            latitudeTmpForEdit = publication.getLatitude();
            longitude = publication.getLongitude();
            longitudeTmpForEdit = publication.getLongitude();
        }

        addressDialogStarted = true;
        addressDialog = new Dialog(this);
        addressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addressDialog.setContentView(R.layout.address_search_dialog);
        addressDialog.setCanceledOnTouchOutside(false);
        addressDialog.setOnDismissListener(this);
        addressDialog.setOnCancelListener(this);

        // Auto complete
        atv_address = (AutoCompleteTextView) addressDialog.findViewById(R.id
                .actv_address_new_publication);
        atv_address.setThreshold(3);
        atv_address.setOnItemClickListener(mAutocompleteClickListener);
        atv_address.setOnClickListener(this);
        atv_address.addTextChangedListener(this);
        atv_address.setAdapter(mPlaceArrayAdapter);

        atv_address.setText(address);

        cb_use_my_current_location = (CheckBox) addressDialog.findViewById(R.id.cb_use_current_location);
        cb_use_my_current_location.setOnClickListener(this);
        cb_use_my_current_location.setChecked(false);
        iv_address_dialog_location_validation = (ImageView) addressDialog.findViewById(R.id.iv_address_dialog_my_loc_validation);

        btn_address_dialog_ok = (Button) addressDialog.findViewById(R.id.btn_address_dialog_ok);
        btn_address_dialog_ok.setOnClickListener(this);
        btn_address_dialog_cancel = (Button) addressDialog.findViewById(R.id.btn_address_dialog_cancel);
        btn_address_dialog_cancel.setOnClickListener(this);

        prevAddressesList = (ListView) addressDialog.findViewById(R.id.lv_address_history);
        prevAddressesList.setOnItemClickListener(this);
        Cursor prevAddressesCursor
                = getContentResolver().query(FooDoNetSQLProvider.URI_PREVIOUS_ADDRESSES,
                new String[]{FCPublication.PUBLICATION_ADDRESS_KEY,
                        FCPublication.PUBLICATION_LATITUDE_KEY,
                        FCPublication.PUBLICATION_LONGITUDE_KEY}, null, null, null);
        Map<String, LatLng> prevAddresses = new HashMap<>();
        if (prevAddressesCursor != null)
            prevAddresses = CommonUtil.GetPreviousAddressesMapFromCursor(prevAddressesCursor);
        prevAddressAdapter = new PreviousAddressesHashMapAdapter(prevAddresses);
        prevAddressesList.setAdapter(prevAddressAdapter);
        if (prevAddresses.size() > 0) {

        }

        mGoogleApiClient.connect();
        addressDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        addressDialogStarted = false;
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        addressDialogStarted = false;
        mGoogleApiClient.disconnect();
    }

    //endregion

    //region Click

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_date_time_add_pub:
            case R.id.btn_end_date_time_add_pub:
                SetDatesButtonsValid(true);
                showDatePickerDialog(view.getId());
                break;
            case R.id.btn_camera_add_pub:
                selectImage();
                break;
      /*      case R.id.btn_menu_add_pub:
                if (currentPageIndex == PAGE_MAP) {
                    drawerLayout.openDrawer(ll_sideMenu);
                }*/
         /*   case R.id.chkCallToPublisher:
                if(chkCallToPublisher.isChecked())
                    publication.setTypeOfCollecting(FCTypeOfCollecting.ContactPublisher);
                else
                    publication.setTypeOfCollecting(FCTypeOfCollecting.FreePickUp);
                break;*/
            case R.id.imgAddPicture:
                selectImage();
                break;
            case R.id.publishButton:
                if(publication.IsEqualTo(publicationOldVersion))
                    onBackPressed();
                if (!ValidateInputData()) return;
                ArrangePublicationFromInput(publication);
                ReturnPublication();
                break;
            case R.id.et_title_new_publication:
                CommonUtil.RemoveValidationFromEditText(this, et_publication_title);
                break;
            case R.id.et_address_edit_add_pub:
//                et_publication_title.getBackground()
//                        .setColorFilter(getResources()
//                                .getColor(R.color.edit_text_input_default_color), PorterDuff.Mode.SRC_ATOP);
                //ShowAddressDialog();
                break;
            case R.id.actv_address_new_publication:
                CommonUtil.RemoveValidationFromEditText(this, atv_address);
                break;
            case R.id.btn_address_dialog_ok:
                if (!ValidateAddressLineAndLocationData()) return;
                if (atv_address != null)
                    addressTmpForEdit = atv_address.getText().toString();
                if (addressDialog != null)
                    addressDialog.dismiss();
                publication.setLatitude(latitudeTmpForEdit);
                publication.setLongitude(longitudeTmpForEdit);
                publication.setAddress(addressTmpForEdit);
                et_address.setText(publication.getAddress());
                //SetEditTextIsValid(et_address, true);
                onDismiss(addressDialog);
                break;
            case R.id.btn_address_dialog_cancel:
                if (addressDialog != null)
                    addressDialog.dismiss();
                publication.setLatitude(latitude);
                publication.setLongitude(longitude);
                publication.setAddress(address);
                onCancel(addressDialog);
                break;
            case R.id.cb_use_current_location:
                String message = getString(R.string.progress_waiting_for_location);
                progressDialog = CommonUtil.ShowProgressDialog(this, message);
                GetMyLocationAsync getLocAsync = new GetMyLocationAsync((LocationManager) getSystemService(LOCATION_SERVICE), this);
                getLocAsync.setGotLocationCallback(this);
                getLocAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    //endregion

    //region My methods

    private void ArrangePublicationFromInput(FCPublication publication) {
        publication.setTitle(et_publication_title.getText().toString());
        publication.setSubtitle(et_subtitle.getText().toString());
        publication.setContactInfo(et_additional_info.getText().toString());
        publication.setStartingDate(startDate);
        publication.setEndingDate(endDate);

        if (TextUtils.isEmpty(publication.getPublisherUID()))
            publication.setPublisherUID(CommonUtil.GetIMEI(this));
        publication.setTypeOfCollecting(FCTypeOfCollecting.ContactPublisher);
        publication.setVersion(publication.getVersion());
        publication.setIsOnAir(true);
        publication.setIfTriedToGetPictureBefore(true);
    }

    private void ReturnPublication() {
        Intent dataPublicationIntent = new Intent();
        dataPublicationIntent.putExtra(PublicationDetailsActivity.DETAILS_ACTIVITY_RESULT_KEY, InternalRequest.ACTION_POST_NEW_PUBLICATION);
        dataPublicationIntent.putExtra(PUBLICATION_KEY, publication);
        // return data Intent and finish
        setResult(RESULT_OK, dataPublicationIntent);
        finish();
    }

    private boolean ValidateInputData() {
        return ValidateTitle()
                && ValidateAddress()
                && ValidateAdditionalInfoField()
                && ValidateDates();
    }

    private boolean ValidateTitle() {
        //check title
        if (et_publication_title.getText().toString().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, et_publication_title, false);
            Toast.makeText(this, getString(R.string.validation_title_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        //todo: check max lenght of title
        if (et_publication_title.getText().toString().length() >= 10000) {//HARDCODE!!!
            CommonUtil.SetEditTextIsValid(this, et_publication_title, false);
            Toast.makeText(this, getString(
                            R.string.validation_title_too_long).replace("{0}", String.valueOf(10000)),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_publication_title, true);
        return true;
    }

    private boolean ValidateAddress() {
        if (et_address.getText().toString().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, et_address, false);
            Toast.makeText(this, getString(R.string.validation_address_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_address, true);
        return true;
    }

    private boolean ValidateAddressLineAndLocationData() {
        if (atv_address.getText().toString().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, atv_address, false);
            Toast.makeText(this, getString(R.string.validation_address_empty), Toast.LENGTH_LONG).show();
            return false;
        } else
            CommonUtil.SetEditTextIsValid(this, atv_address, true);
        if (latitudeTmpForEdit == -1000 || longitudeTmpForEdit == -1000) {
            iv_address_dialog_location_validation.setImageDrawable(
                    getResources().getDrawable(R.drawable.validation_wrong));
            iv_address_dialog_location_validation.setVisibility(View.VISIBLE);
            Toast.makeText(this, getString(R.string.validation_location_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, atv_address, true);
        return true;
    }

    private boolean ValidateAdditionalInfoField() {
        if (et_additional_info.getText().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, et_additional_info, false);
            Toast.makeText(this, getString(R.string.validation_phone_number_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!CommonUtil.CheckPhoneNumberString(this, et_additional_info.getText().toString())) {
            CommonUtil.SetEditTextIsValid(this, et_additional_info, false);
            Toast.makeText(this, getString(R.string.validation_phone_number_invalid), Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_additional_info, true);
        return true;
    }

    private boolean ValidateDates() {
        if (startDate.getTime() > endDate.getTime()) {
            SetDatesButtonsValid(false);
//            btn_date_start.setTextColor(getResources().getColor(R.color.validation_red_text_color));
//            btn_date_end.setTextColor(getResources().getColor(R.color.validation_red_text_color));
            Toast.makeText(this, getString(R.string.validation_start_date_after_end_date), Toast.LENGTH_LONG).show();
            return false;
        }
        SetDatesButtonsValid(true);
//        btn_date_start.setTextColor(getResources().getColor(R.color.edit_text_input_default_color));
//        btn_date_end.setTextColor(getResources().getColor(R.color.edit_text_input_default_color));
        return true;
    }

    private void SetDatesButtonsValid(boolean isValid) {
        btn_date_start.setTextColor(getResources().getColor(isValid ? R.color.edit_text_input_default_color : R.color.validation_red_text_color));
        btn_date_end.setTextColor(getResources().getColor(isValid ? R.color.edit_text_input_default_color : R.color.validation_red_text_color));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.et_address_edit_add_pub:
                CommonUtil.RemoveValidationFromEditText(this, et_publication_title);
                if (!addressDialogStarted)
                    ShowAddressDialog();
                break;
        }
        return true;
    }

    @Override
    public void OnGotMyLocationCallback(Location location) {
        if (progressDialog != null)
            progressDialog.dismiss();
        if (location != null) {
            latitudeTmpForEdit = location.getLatitude();
            longitudeTmpForEdit = location.getLongitude();
            String myCurrentAddress = GetCurrentAddress(latitudeTmpForEdit, longitudeTmpForEdit);
            isGoogleAddress = false;
            if (iv_address_dialog_location_validation != null) {
                atv_address.setText(myCurrentAddress);
                addressTmpForEdit = atv_address.getText().toString();
                iv_address_dialog_location_validation.setImageDrawable(getResources().getDrawable(R.drawable.validation_ok));
                iv_address_dialog_location_validation.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, getString(R.string.validation_cant_get_my_location), Toast.LENGTH_LONG).show();
            if (cb_use_my_current_location != null)
                cb_use_my_current_location.setChecked(false);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
        Map.Entry<String, LatLng> selectedAddress = prevAddressAdapter.getItem(position);
        if (selectedAddress != null) {
            addressTmpForEdit = selectedAddress.getKey();
            atv_address.setText(selectedAddress.getKey());
            latitudeTmpForEdit = selectedAddress.getValue().latitude;
            longitudeTmpForEdit = selectedAddress.getValue().longitude;
            isGoogleAddress = true;
            if (atv_address != null)
                CommonUtil.SetEditTextIsValid(this, atv_address, true);
            if (iv_address_dialog_location_validation != null)
                iv_address_dialog_location_validation.setVisibility(View.GONE);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isGoogleAddress) {
            latitudeTmpForEdit = -1000;
            longitudeTmpForEdit = -1000;
            isGoogleAddress = false;
            iv_address_dialog_location_validation.setImageDrawable(
                    getResources().getDrawable(R.drawable.validation_wrong));
            iv_address_dialog_location_validation.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    public void onFocusChange(View v, boolean hasFocus) {
//        switch (v.getId()) {
//            case R.id.et_title_new_publication:
//                if (hasFocus) {
//                    et_publication_title.setTextColor(getResources().getColor(R.color.edit_text_input_default_color));
//                    et_publication_title.getBackground()
//                            .setColorFilter(getResources()
//                                    .getColor(R.color.edit_text_input_default_color), PorterDuff.Mode.SRC_ATOP);
//                }
//                break;
//        }
//    }

    //endregion
}
