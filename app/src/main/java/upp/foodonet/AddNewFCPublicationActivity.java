package upp.foodonet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import DataModel.FCPublication;


public class AddNewFCPublicationActivity extends FragmentActivity
implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private EditText mNameText;
    //private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteTextView;
    private static final String LOG_TAG = "MainActivity";
    private TextView mNameTextView;
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

    private RadioGroup mPickupRadioGroup;

    private ImageView mImageView;
    private Uri imageURI;


    private Date mDate;

    public enum PickupStatus {
        FREE, CALL
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_publication);

        mNameText = (EditText) findViewById(R.id.name);

        // Auto complete
        mGoogleApiClient = new GoogleApiClient.Builder(AddNewFCPublicationActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(AddNewFCPublicationActivity.this, GOOGLE_API_CLIENT_ID, AddNewFCPublicationActivity.this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);
        mAutocompleteTextView.setThreshold(3);
        //mNameTextView = (TextView) findViewById(R.id.name1);
        mAddressTextView = (TextView) findViewById(R.id.address);
        //mIdTextView = (TextView) findViewById(R.id.place_id);
        //mPhoneTextView = (TextView) findViewById(R.id.phone);
        //mWebTextView = (TextView) findViewById(R.id.web);
        //mAttTextView = (TextView) findViewById(R.id.att);
        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);

        // Date and Time
        startDateView = (TextView) findViewById(R.id.start_date);
        startTimeView = (TextView) findViewById(R.id.start_time);
        endDateView = (TextView) findViewById(R.id.end_date);
        endTimeView = (TextView) findViewById(R.id.end_time);



        // Set the default date and time
        setDefaultDateTime();

        // OnClickListener for the Date button, calls showDatePickerDialog() to show the Date dialog
        final Button startDatePickerButton = (Button) findViewById(R.id.start_date_picker_button);
        startDatePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // OnClickListener for the Time button, calls showTimePickerDialog() to show the Time Dialog
        final Button startTimePickerButton = (Button) findViewById(R.id.start_time_picker_button);
        startTimePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        final Button endDatePickerButton = (Button) findViewById(R.id.end_date_picker_button);
        endDatePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // OnClickListener for the Time button, calls showTimePickerDialog() to
        // show the Time Dialog
        final Button endTimePickerButton = (Button) findViewById(R.id.end_time_picker_button);
        endTimePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        mPickupRadioGroup = (RadioGroup) findViewById(R.id.pickupMethodGroup);

        mImageView = (ImageView)findViewById(R.id.imgView);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select image for publication"), 1);


//                Bitmap bmp = BitmapFactory.decodeFile(miFoto);
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
//                InputStream in = new ByteArrayInputStream(bos.toByteArray());
//                ContentBody foto = new InputStreamBody(in, "image/jpeg", "filename");
            }
        });

        final Button submitButton = (Button) findViewById(R.id.publishButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FOODONET", "Entered submitButton.OnClickListener.onClick()");

                // TODO - gather ToDoItem data



//                int radioButtonID = mPriorityRadioGroup.getCheckedRadioButtonId();
//                View radioButton = mPriorityRadioGroup.findViewById(radioButtonID);
//                int idx = mPriorityRadioGroup.indexOfChild(radioButton);
//				// Get Priority
//				Priority priority = Priority.values()[idx];
//                Priority priority = getPriority();
//
//
//                int radioButtonIDstatus = mStatusRadioGroup.getCheckedRadioButtonId();
//                View radioButtonStatus = mStatusRadioGroup.findViewById(radioButtonIDstatus);
//                int idxStatus = mStatusRadioGroup.indexOfChild(radioButtonStatus);
//                // Get Status
//                Status status = Status.values()[idxStatus];
//
//                // Title
//                String titleString = mTitleText.getText().toString();
//
//                // Date
//                String fullDate = dateString + " " + timeString;

                String title = mNameText.getText().toString();
                // Package FCPublication data into an Intent
                Intent data = new Intent();
                FCPublication.packageIntent(data, mNameText.getText().toString(), imageURI);

                // return data Intent and finish
                setResult(RESULT_OK, data);
                finish();



            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("FOODONET", "Entered onActivityResult()");


        if (requestCode == 1) {
            if(resultCode == RESULT_OK){

                imageURI = data.getData();
                mImageView.setImageURI(data.getData());
            }
            if (resultCode == RESULT_CANCELED) {

            }
        }

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

           // mNameTextView.setText(Html.fromHtml(place.getName() + ""));
            mAddressTextView.setText(Html.fromHtml(place.getAddress() + ""));
//            mIdTextView.setText(Html.fromHtml(place.getId() + ""));
//            mPhoneTextView.setText(Html.fromHtml(place.getPhoneNumber() + ""));
//            mWebTextView.setText(place.getWebsiteUri() + "");
//            if (attributions != null) {
//                mAttTextView.setText(Html.fromHtml(attributions.toString()));
//            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
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

    private void setDefaultDateTime() {

        // Default is current time + 7 days
        mDate = new Date();
        mDate = new Date(mDate.getTime());

        Calendar c = Calendar.getInstance();
        c.setTime(mDate);

        // Set default date
        setDateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        startDateView.setText(dateStartString);

        c.add(Calendar.DATE, 1);
        setDateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        endDateView.setText(dateEndString);

//
//        // Set default time
//        Time today = new Time(Time.getCurrentTimezone());
//        today.setToNow();

        setTimeString(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                c.get(Calendar.MILLISECOND));

        startTimeView.setText(timeStartString);
        endTimeView.setText(timeEndString);
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

            startDateView.setText(dateStartString);
            endDateView.setText(dateEndString);
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

            startTimeView.setText(timeStartString);
            endTimeView.setText(timeEndString);
        }
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    private PickupStatus getStatus() {

        switch (mPickupRadioGroup.getCheckedRadioButtonId()) {
            case R.id.freePickup: {
                return PickupStatus.FREE;
            }
            default: {
                return PickupStatus.CALL;
            }
        }
    }
}
