package upp.foodonet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import CodeWeDontUse.GcmSenderTest;
import FooDoNetServerClasses.DownloadImageTask;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IDownloadImageCallBack;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class EntranceActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, IFooDoNetSQLCallback, IFooDoNetServerCallback {


    private static final String MY_TAG = "food_EntanceActivity";
    //Button btn_pick, btn_share, btn_ask;
    //TextView tv_pick, tv_share;
    RelativeLayout rl_btn_pick, rl_btn_share;
    boolean isUIBlocked;
    ProgressDialog progressDialog;

    private FCPublication publication;

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

/*
        btn_share = (Button) findViewById(R.id.btn_share_welcomeScreen);
        tv_share = (TextView) findViewById(R.id.tv_share_welcomeScreen);
        btn_pick = (Button) findViewById(R.id.btn_pick_welcomeScreen);
        tv_pick = (TextView) findViewById(R.id.tv_pick_welcomeScreen);
        btn_ask = (Button) findViewById(R.id.btn_ask_welcomeScreen);
        btn_pick.setOnClickListener(this);
        tv_pick.setOnClickListener(this);
        btn_share.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        btn_ask.setOnClickListener(this);
        Drawable img_give = getResources().getDrawable(R.drawable.first_screen_collect_xxh);
        Drawable img_take = getResources().getDrawable(R.drawable.first_screen_donate_xxh);
        Drawable img_ask = getResources().getDrawable(R.drawable.collect_v6_3x);
        img_give.setBounds(0, 0, 80, 80);
        img_take.setBounds(0, 0, 80, 80);
        btn_share.setCompoundDrawables(null, null, img_give, null);
        btn_pick.setCompoundDrawables(null, null, img_take, null);
        btn_ask.setCompoundDrawables(null, null, img_ask, null);
*/

        rl_btn_share = (RelativeLayout)findViewById(R.id.rl_btn_give_entrance_screen);
        rl_btn_share.setOnClickListener(this);
        rl_btn_pick = (RelativeLayout)findViewById(R.id.rl_btn_take_entrance_screen);
        rl_btn_pick.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        if (progressDialog != null)
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
        if (isUIBlocked)
            return;
        isUIBlocked = true;
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
        switch (v.getId()) {
//            case R.id.btn_pick_welcomeScreen:
//            case R.id.tv_pick_welcomeScreen:
            case R.id.rl_btn_take_entrance_screen:
                Intent mapListIntent = new Intent(this, MapAndListActivity.class);
                startActivity(mapListIntent);
                break;
//            case R.id.btn_share_welcomeScreen:
//            case R.id.tv_share_welcomeScreen:
            case R.id.rl_btn_give_entrance_screen:
                Intent myPubsIntent = new Intent(this, MyPublicationsActivity.class);
                startActivity(myPubsIntent);
                break;
            case R.id.rl_btn_test:

                break;
        }
    }

    public void StartFeedbackPopup(Context context){

    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                Log.i(MY_TAG, "fetched single pub from server: " + (request.Status == InternalRequest.STATUS_OK ? "ok" : "fail"));
                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                Log.i(MY_TAG, "deleted pub from db: " + request.publicationForSaving.getTitle() + ": " + (request.Status == InternalRequest.STATUS_OK ? "ok" : "fail"));
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                Log.i(MY_TAG, "new report added: " + request.publicationForSaving.getTitle() + ": " + (request.Status == InternalRequest.STATUS_OK ? "ok" : "fail"));
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
        switch (response.ActionCommand) {
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                if (response.Status == InternalRequest.STATUS_OK) {
                    FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                    sqlExecuterAsync.execute(response);
                }
                break;
        }
    }
}
