package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
import DataModel.RegisteredUserForPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class EntranceActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, IFooDoNetSQLCallback, IFooDoNetServerCallback{



    private static final String MY_TAG = "food_EntanceActivity";
    Button btn_pick, btn_share, btn_ask;

    public static final int REQUEST_ADD_NEW_PUBLICATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        //startMyPublicationDetailsActivity();

        // Why do we need this?
        //TextView welcomeView = (TextView) findViewById(R.id.welcome);
        //welcomeView.setBackgroundColor(Color.BLUE);
        //=====

        btn_share = (Button)findViewById(R.id.btn_share_welcomeScreen);
        btn_pick = (Button)findViewById(R.id.btn_pick_welcomeScreen);
        btn_ask = (Button)findViewById(R.id.btn_ask_welcomeScreen);
        btn_pick.setOnClickListener(this);
        btn_share.setOnClickListener(this);

        btn_ask.setOnClickListener(this);

        Drawable img_give = getResources().getDrawable( R.drawable.donate_v6_3x );
        Drawable img_take = getResources().getDrawable( R.drawable.collect_v6_3x);
        Drawable img_ask = getResources().getDrawable( R.drawable.collect_v6_3x);
        img_give.setBounds(0, 0, 90, 90);
        img_take.setBounds(0, 0, 90, 90);
        img_ask.setBounds(0, 0, 90, 90);
        btn_share.setCompoundDrawables(null, null, img_give, null);
            btn_share.setCompoundDrawablePadding(10);
        btn_pick.setCompoundDrawables(null, null, img_take, null);
        btn_ask.setCompoundDrawables(null, null, img_ask, null);

        //Bidi bidi = new Bidi(btn_give.getText().toString(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        //Log.i("food", "bidi.getBaseLevel() = " + bidi.getBaseLevel());



/*
        TextView publishTextView = (TextView) findViewById(R.id.publishText);
        publishTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EntranceActivity.this, MainActivity.class);
                startActivity(intent);
            }
            });

        RoundedImageView publishImageView = (RoundedImageView) findViewById(R.id.publishImage);
        //publishImageView.setBackgroundColor(Color.BLUE);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.first_screen_collect);
        publishImageView.setImageBitmap(icon);

        publishImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EntranceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        */



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entrance, menu);
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

/*
    @Override
    public void OnNotifiedToFetchData() {

    }
*/

/*
    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {

    }
*/

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pick_welcomeScreen:
                Intent mapListIntent = new Intent(this, MapAndListActivity.class);
                //mapListIntent.putExtra("service", boundedService);
                startActivity(mapListIntent);
                break;
            case R.id.btn_share_welcomeScreen:
                Intent myPubsIntent = new Intent(this, MyPublicationsActivity.class);
                //myPubsIntent.putExtra("service", boundedService);
                startActivity(myPubsIntent);
                break;
            case R.id.btn_ask_welcomeScreen:
                Intent addNewPubIntent = new Intent(this, AddEditPublicationActivity.class);
                startActivityForResult(addNewPubIntent, REQUEST_ADD_NEW_PUBLICATION);
                TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String imei = tm.getDeviceId();
                Date sDate = new Date();
                Date eDate;
                Calendar c = Calendar.getInstance();
                c.setTime(sDate);
                c.add(Calendar.DATE, 5);
                eDate = c.getTime();
                FCPublication newPublication
                        = new FCPublication(0, imei, "test pub title", "", "some address",
                                                FCTypeOfCollecting.ContactPublisher, 0, 0,
                                                sDate, eDate, "", "", true);

                //FooDoNetSQLExecuterAsync saveNewTask = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                //saveNewTask.execute(new InternalRequest(InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION, newPublication));

                HttpServerConnectorAsync postNewPubTask
                        = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_PUBLICATION, newPublication);
                ir.ServerSubPath = getResources().getString(R.string.server_add_new_publication_path);
                postNewPubTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);


                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(MY_TAG, "Entered onActivityResult()");

        FCPublication pub = new FCPublication();
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_NEW_PUBLICATION)
            {
                pub = (FCPublication) data.getExtras().get("publication");
            }
        }
        if (resultCode == RESULT_CANCELED) {

        }
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        Toast.makeText(getBaseContext(), "this code should not be reached", Toast.LENGTH_LONG);
/*
        if(request.publicationForSaving == null){
            Log.e(MY_TAG, "got null request.publicationForSaving");
            return;
        }
        HttpServerConnectorAsync postNewPubTask
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), this);
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_PUBLICATION, request.publicationForSaving);
        ir.ServerSubPath = getResources().getString(R.string.server_add_new_publication_path);
        postNewPubTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
*/
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {

    }
}
