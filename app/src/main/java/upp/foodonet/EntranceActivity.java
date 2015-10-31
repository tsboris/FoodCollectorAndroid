package upp.foodonet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Date;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import CodeWeDontUse.GcmSenderTest;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class EntranceActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, IFooDoNetSQLCallback, IFooDoNetServerCallback{



    private static final String MY_TAG = "food_EntanceActivity";
    Button btn_pick, btn_share, btn_ask;
    boolean isUIBlocked;
    ProgressDialog progressDialog;

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
    protected void onPause() {
        if(progressDialog != null)
            progressDialog.dismiss();
        super.onPause();
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
        Toast.makeText(this, getString(R.string.no_internet_connection_available_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(isUIBlocked)
            return;
        isUIBlocked = true;
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
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
        switch (request.ActionCommand){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                Log.i(MY_TAG, "fetched single pub from server: " + (request.Status == InternalRequest.STATUS_OK? "ok":"fail"));
                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                Log.i(MY_TAG, "deleted pub from db: " + request.publicationForSaving.getTitle() + ": " + (request.Status == InternalRequest.STATUS_OK? "ok":"fail"));
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                Log.i(MY_TAG, "new report added: " + request.publicationForSaving.getTitle() + ": " + (request.Status == InternalRequest.STATUS_OK? "ok":"fail"));
                break;
        }

/*        Toast.makeText(getBaseContext(), "this code should not be reached", Toast.LENGTH_LONG);

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
        switch (response.ActionCommand){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                if(response.Status == InternalRequest.STATUS_OK){
                    FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                    sqlExecuterAsync.execute(response);
                }
                break;
        }
    }
}
