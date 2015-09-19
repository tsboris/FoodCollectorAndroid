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
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.Time;
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
import android.widget.RelativeLayout;
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
import UIUtil.DateTimePicker;


public class AddNewFCPublicationActivity extends FragmentActivity
implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,View.OnClickListener, DateTimePicker.DateWatcher {

    private static final String MY_TAG = "food_newPublication";
    public static final String PUBLICATION_KEY = "publication";
    public static final String START_DATE_PICKER_KEY = "startDatePicker";
    public static final String START_TIME_PICKER_KEY = "startTimePicker";
    public static final String END_DATE_PICKER_KEY = "endDatePicker";
    public static final String END_TIME_PICKER_KEY = "endTimePicker";

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;

    private EditText mTitleText;


    //private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView atv_address;
    private TextView tv_title;
    private TextView mAddressTextView;
    private TextView mIdTextView;
    private TextView mPhoneTextView;
    private TextView mWebTextView;
    private TextView mAttTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    private static String timeStartString;
    private static String dateStartString;
    private static String timeEndString;
    private static String dateEndString;

    private static TextView startDateView;
    private static TextView startTimeView;
    private static TextView endDateView;
    private static TextView endTimeView;

    private static Button startDatePickerButton;
    private static Button startTimePickerButton;
    private static Button endDatePickerButton;
    private static Button endTimePickerButton;

    private EditText etStartDate;
    private EditText etEndDate;

    private static CheckBox chkCallToPublisher;

    private ImageView mAddPicImageView;
    private Uri imageURI;

    private static ImageButton submitButton,cameraBtn;

    private Date mDate;

    private static FCPublication publication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_publication);


        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            publication = new FCPublication();
        } else {
            publication = (FCPublication)extras.get(PUBLICATION_KEY);
        }

        mTitleText = (EditText) findViewById(R.id.et_title_new_publication);

        // Auto complete
        mGoogleApiClient = new GoogleApiClient.Builder(AddNewFCPublicationActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(AddNewFCPublicationActivity.this, GOOGLE_API_CLIENT_ID, AddNewFCPublicationActivity.this)
                .addConnectionCallbacks(this)
                .build();
        atv_address = (AutoCompleteTextView) findViewById(R.id
                .actv_address_new_publication);
        atv_address.setThreshold(3);
        //mNameTextView = (TextView) findViewById(R.id.name1);
        mAddressTextView = (TextView) findViewById(R.id.address);
        //mIdTextView = (TextView) findViewById(R.id.place_id);
        mPhoneTextView = (TextView) findViewById(R.id.add_phoneNumber);
        mPhoneTextView.setInputType(InputType.TYPE_CLASS_NUMBER);
        //mWebTextView = (TextView) findViewById(R.id.web);
        //mAttTextView = (TextView) findViewById(R.id.att);
        atv_address.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        atv_address.setAdapter(mPlaceArrayAdapter);

        // OnClickListener for the Date button, calls showDatePickerDialog() to show the Date dialog
//        startDatePickerButton = (Button) findViewById(R.id.start_date_picker_button);
//        startDatePickerButton.setOnClickListener(this);
//
//
//
//        startTimePickerButton = (Button) findViewById(R.id.start_time_picker_button);
//        startTimePickerButton.setOnClickListener(this);
//
//        endDatePickerButton = (Button) findViewById(R.id.end_date_picker_button);
//        endDatePickerButton.setOnClickListener(this);
//
//        endTimePickerButton = (Button) findViewById(R.id.end_time_picker_button);
//        endTimePickerButton.setOnClickListener(this);

        etStartDate = (EditText) findViewById(R.id.etStartDateTime);
        etEndDate = (EditText) findViewById(R.id.etEndDateTime);

        // Set the default date and time
//        setDefaultDateTime();

        //chkCallToPublisher = (CheckBox) findViewById(R.id.chkCallToPublisher);
        //chkCallToPublisher.setOnClickListener(this);

        mAddPicImageView = (ImageView)findViewById(R.id.imgAddPicture);
        mAddPicImageView.setOnClickListener(this);

        cameraBtn = (ImageButton)findViewById(R.id.btn_camera_add_pub);
        cameraBtn.setOnClickListener(this);

        submitButton = (ImageButton) findViewById(R.id.publishButton);
        submitButton.setOnClickListener(this);
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewFCPublicationActivity.this);
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
                    startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
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
            if (requestCode == REQUEST_CAMERA)
            {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                publication.setImageByteArrayFromBitmap(thumbnail);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
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
            }
            else if (requestCode == SELECT_FILE)
            {
                Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
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
            mAddressTextView.setText(address);
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

    public double getLatitudeFromAddress(String strAddress)
    {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double latitude = 0;

        try
        {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                Address location = address.get(0);
                latitude = location.getLatitude();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return latitude;
    }

    public double getLongitudeFromAddress(String strAddress)
    {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double longitude = 0;

        try
        {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                Address location = address.get(0);
                longitude = location.getLongitude();
            }
        }
        catch (Exception ex)
        {
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


    public void etStartDateTime_click(View view){
        // Create the dialog
        final Dialog mDateTimeDialog = new Dialog(this);
        // Inflate the root layout
        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
        // Grab widget instance
        final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
        mDateTimePicker.setDateChangedListener(this);

        // Update demo TextViews when the "OK" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mDateTimePicker.clearFocus();
                // TODO Auto-generated method stub
                String result_string = String.valueOf(mDateTimePicker.getDay()) + "/" + mDateTimePicker.getMonth() + "/" + String.valueOf(mDateTimePicker.getYear())
                        + "  " + String.valueOf(mDateTimePicker.getHour()) + ":" + String.valueOf(mDateTimePicker.getMinute());
//				if(mDateTimePicker.getHour() > 12) result_string = result_string + "PM";
//				else result_string = result_string + "AM";
                etStartDate.setText(result_string);
                mDateTimeDialog.dismiss();
            }
        });

        // Cancel the dialog when the "Cancel" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimeDialog.cancel();
            }
        });

        // Reset Date and Time pickers when the "Reset" button is clicked

        ((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimePicker.reset();
            }
        });

        // Setup TimePicker
        // No title on the dialog window
        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the dialog content view
        mDateTimeDialog.setContentView(mDateTimeDialogView);
        // Display the dialog
        mDateTimeDialog.show();
    }

    private void setDefaultDateTime() {

        // Default is current time + 7 days
        mDate = new Date();
        mDate = new Date(mDate.getTime());

        Calendar c = Calendar.getInstance();
        c.setTime(mDate);

        // Set default date
        setDateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        startDatePickerButton.setText(dateStartString);

        c.add(Calendar.DATE, 1);
        setDateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        endDatePickerButton.setText(dateEndString);

//
//        // Set default time
//        Time today = new Time(Time.getCurrentTimezone());
//        today.setToNow();

        setTimeString(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                c.get(Calendar.MILLISECOND));

        startTimePickerButton.setText(timeStartString);
        endTimePickerButton.setText(timeEndString);
    }

    private static void setDateString(int year, int monthOfYear, int dayOfMonth) {

        // Increment monthOfYear for Calendar/Date -> Time Format setting
        monthOfYear++;
        String mon = "" + monthOfYear;
        String day = "" + dayOfMonth;

        if (monthOfYear < 10)
            mon = "0" + monthOfYear;
        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;

        dateStartString = day + "-" + mon + "-" + year;
        dateEndString = day + "-" + mon + "-" + year;
    }

    private static void setTimeString(int hourOfDay, int minute, int mili) {
        String hour = "" + hourOfDay;
        String min = "" + minute;

        if (hourOfDay < 10)
            hour = "0" + hourOfDay;
        if (minute < 10)
            min = "0" + minute;

        timeStartString = hour + ":" + min;// + ":00";
        timeEndString = hour + ":" + min;
    }

    @Override
    public void onDateChanged(Calendar c) {
        Log.e("",
                "" + c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH)
                        + " " + c.get(Calendar.YEAR));
    }

    // DialogFragment used to pick a ToDoItem deadline date

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current date as the default date in the picker

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            setDateString(year, monthOfYear, dayOfMonth);

            if(this.getTag() == START_DATE_PICKER_KEY)
                startDatePickerButton.setText(dateStartString);
            else
                endDatePickerButton.setText(dateEndString);
        }

    }

    // DialogFragment used to pick a ToDoItem deadline time

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            setTimeString(hourOfDay, minute, 0);

            if(this.getTag() == START_TIME_PICKER_KEY)
                startTimePickerButton.setText(timeStartString);
            else
                endTimePickerButton.setText(timeEndString);
        }
    }

    private void showStartDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), START_DATE_PICKER_KEY);
    }

    private void showStartTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), START_TIME_PICKER_KEY);
    }

    private void showEndDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), END_DATE_PICKER_KEY);
    }

    private void showEndTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), END_TIME_PICKER_KEY);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
//            case R.id.start_date_picker_button:
//                showStartDatePickerDialog();
//                break;
//            case R.id.start_time_picker_button:
//                showStartTimePickerDialog();
//                break;
//            case R.id.end_date_picker_button:
//                showEndDatePickerDialog();
//                break;
//            case R.id.end_time_picker_button:
//                showEndTimePickerDialog();
//                break;
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

//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent,"Select image for publication"), 1);


//                Bitmap bmp = BitmapFactory.decodeFile(miFoto);
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
//                InputStream in = new ByteArrayInputStream(bos.toByteArray());
//                ContentBody foto = new InputStreamBody(in, "image/jpeg", "filename");
                break;
            case R.id.publishButton:
                Log.i(MY_TAG, "Entered submitButton.OnClickListener.onClick()");

                // TODO - gather ToDoItem data

//                // Title
//                String titleString = mTitleText.getText().toString();
//
//                // Date
//                String fullDate = dateString + " " + timeString;

                String title = mTitleText.getText().toString();
                publication.setTitle(title);

                String dtStart = startDatePickerButton.getText().toString() + " " + startTimePickerButton.getText().toString() + ":00";
                Date dateStart = new Date();
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                try {
                    dateStart = format.parse(dtStart);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                publication.setStartingDate(dateStart);

                String dtEnd = endDatePickerButton.getText().toString() + " " + endTimePickerButton.getText().toString() + ":00";
                Date dateEnd = new Date();
                try {
                    dateEnd = format.parse(dtEnd);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                publication.setEndingDate(dateEnd);

                if(TextUtils.isEmpty(publication.getPublisherUID()))
                    publication.setPublisherUID(CommonUtil.GetIMEI(this));
                publication.setTypeOfCollecting( FCTypeOfCollecting.FreePickUp);//tmp todo no checkbox
                //        chkCallToPublisher.isChecked()
                //                ? FCTypeOfCollecting.ContactPublisher : FCTypeOfCollecting.FreePickUp);
                publication.setVersion(publication.getVersion() + 1);
                publication.setIsOnAir(true);

                Intent dataPublicationIntent = new Intent();
                dataPublicationIntent.putExtra(PUBLICATION_KEY, publication);
                // return data Intent and finish
                setResult(RESULT_OK, dataPublicationIntent);
                finish();
                break;
        }
    }
}
