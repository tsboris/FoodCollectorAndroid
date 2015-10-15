package upp.foodonet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.List;

import Adapters.PlaceArrayAdapter;
import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;


public class AddEditPublicationActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener {

    private static final String MY_TAG = "food_newPublication";
    public static final String PUBLICATION_KEY = "publication";

    //region Variables

    public static final String START_DATE_PICKER_KEY = "startDatePicker";
    public static final String START_TIME_PICKER_KEY = "startTimePicker";
    public static final String END_DATE_PICKER_KEY = "endDatePicker";
    public static final String END_TIME_PICKER_KEY = "endTimePicker";

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;


    //private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private EditText et_publication_title;
    private EditText et_address;
    private AutoCompleteTextView atv_address;
    private EditText et_subtitle;
    //private TextView mAddressTextView;
    private TextView mIdTextView;
    private EditText et_additional_info;
    private TextView mWebTextView;
    private TextView mAttTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    private Button btn_date_start;
    private Button btn_date_end;

    private static CheckBox chkCallToPublisher;

    private ImageView mAddPicImageView;
    private Uri imageURI;

    private static ImageButton submitButton, cameraBtn;

    private Date mDate;

    private static FCPublication publication;
    private static FCPublication publicationOldVersion;

    private Date startDate;
    private Date endDate;

    private boolean isNew = false;

    //endregion

    //region Activity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_publication);


        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            publication = new FCPublication();
            isNew = true;
        } else {
            publication = (FCPublication) extras.get(PUBLICATION_KEY);
            isNew = false;
        }
        publicationOldVersion = new FCPublication(publication);

        et_publication_title = (EditText) findViewById(R.id.et_title_new_publication);
        et_publication_title.setOnClickListener(this);

        et_address = (EditText) findViewById(R.id.et_address_edit_add_pub);
        et_publication_title.setOnClickListener(this);

        et_additional_info = (EditText) findViewById(R.id.et_additional_info_add_edit_pub);
        et_additional_info.setInputType(InputType.TYPE_CLASS_NUMBER);
        et_subtitle = (EditText) findViewById(R.id.et_subtitle_add_edit_pub);

        if (!isNew) {
            et_publication_title.setText(publication.getTitle());
            et_subtitle.setText(publication.getSubtitle());
            et_additional_info.setText(publication.getContactInfo());
        }

        // Auto complete
        mGoogleApiClient = new GoogleApiClient.Builder(AddEditPublicationActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(AddEditPublicationActivity.this, GOOGLE_API_CLIENT_ID, AddEditPublicationActivity.this)
                .addConnectionCallbacks(this)
                .build();
        atv_address = (AutoCompleteTextView) findViewById(R.id
                .actv_address_new_publication);
        atv_address.setThreshold(3);
        atv_address.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        atv_address.setAdapter(mPlaceArrayAdapter);

        if (!isNew) {
            atv_address.setText(publication.getAddress());
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

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
                            ForceReturn();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            return;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else {
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
                publication.getUniqueId() + "." + publication.getVersion() + ".jpg",
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
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    publication.setPhotoUrl(destination.getPath());
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
/*
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
*/
                publication.setImageByteArrayFromBitmap(bm);
                mAddPicImageView.setImageBitmap(bm);
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
            double longitude = getLongitudeFromAddress(address);
            double latitude = getLatitudeFromAddress(address);

            publication.setAddress(atv_address.getText().toString());
            publication.setLongitude(longitude);
            publication.setLatitude(latitude);

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

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(MY_TAG, "Google Places API connected.");

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
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(MY_TAG, "Google Places API connection suspended.");
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
    }

    //endregion

    //region Click

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_date_time_add_pub:
            case R.id.btn_end_date_time_add_pub:
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
                if (!ValidateInputDate()) return;
                ArrangePublicationFromInput(publication);
                ReturnPublication();
                break;
            case R.id.et_title_new_publication:
                et_publication_title.getBackground()
                        .setColorFilter(getResources()
                                .getColor(R.color.edit_text_input_default_color), PorterDuff.Mode.SRC_ATOP);
                break;
            case R.id.et_address_edit_add_pub:
                et_publication_title.getBackground()
                        .setColorFilter(getResources()
                                .getColor(R.color.edit_text_input_default_color), PorterDuff.Mode.SRC_ATOP);
                StartAddressDialog();
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
        publication.setTypeOfCollecting(FCTypeOfCollecting.FreePickUp);//tmp todo no checkbox
        //        chkCallToPublisher.isChecked()
        //                ? FCTypeOfCollecting.ContactPublisher : FCTypeOfCollecting.FreePickUp);
        publication.setVersion(publication.getVersion());
        publication.setIsOnAir(true);
        publication.setIfTriedToGetPictureBefore(true);
    }

    private void ReturnPublication() {
        Intent dataPublicationIntent = new Intent();
        dataPublicationIntent.putExtra(PUBLICATION_KEY, publication);
        // return data Intent and finish
        setResult(RESULT_OK, dataPublicationIntent);
        finish();
    }

    private boolean ValidateInputDate() {
        //check title
        if (et_publication_title.getText().toString().length() == 0) {
            //et_publication_title.setTextColor(getResources().getColor(R.color.validation_red_text_color));
            et_publication_title.getBackground()
                    .setColorFilter(getResources()
                            .getColor(R.color.validation_red_text_color), PorterDuff.Mode.SRC_ATOP);
            Toast.makeText(this, getString(R.string.validation_title_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        //todo: check max lenght of title
        if (et_publication_title.getText().toString().length() >= 10000) {//HARDCODE!!!
            et_publication_title.getBackground()
                    .setColorFilter(getResources()
                            .getColor(R.color.validation_red_text_color), PorterDuff.Mode.SRC_ATOP);
            Toast.makeText(this, getString(
                    R.string.validation_title_too_long).replace("{0}", String.valueOf(10000)),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean CheckPhoneNumberString(String phoneNumber) {
        String phonePattern = getString(R.string.regex_israel_phone_number);
        return phoneNumber.matches(phonePattern);
    }

    private void StartAddressDialog() {

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
