package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.gcm.GcmPubSub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import FooDoNetServerClasses.DownloadImageTask;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;


public class SplashScreenActivity
        extends FooDoNetCustomActivityConnectedToService
        implements IFooDoNetServerCallback {
    private int min_splash_screen_duration;
    ArrayList<FCPublication> publicationsFromDB, publicationsFromServer, publicationsUpdatedList;
    boolean flagWaitTaskFinished, registerTaskFinished;

    private final String PREFERENCES_KEY_BOOL_IF_REGISTERED = "ifRegistered";
    private final String MY_TAG = "food_splashscreen";

    private LinearLayout ll_first_load_info;
    private TextView tv_progress_text;

    private boolean isGoogleFacebookChecked;
    private boolean isLoadDataServiceStarted = false;

    private boolean AllLoaded() {
        return registerTaskFinished && flagWaitTaskFinished && isGoogleFacebookChecked;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ll_first_load_info = (LinearLayout) findViewById(R.id.ll_first_load_info);
        tv_progress_text = (TextView) findViewById(R.id.tv_progress_text_splash_screen);
        tv_progress_text.setText(getString(R.string.progress_foodonet_loading));

        flagWaitTaskFinished = true;

        RegisterIfNotRegisteredYet();

        //waiter
/*
        min_splash_screen_duration = getResources().getInteger(R.integer.min_splash_screen_time);
        Point p = new Point(min_splash_screen_duration, 0);
        SplashScreenHolder ssh = new SplashScreenHolder();
        ssh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    protected void onResume() {
        super.onResume();

        AppEventsLogger.activateApp(this);

        if (CommonUtil.GetFromPreferencesIsDataLoaded(this))
            registerTaskFinished = true;
        else if (CommonUtil.GetFromPreferencesIsDeviceRegistered(this) && !isLoadDataServiceStarted) {
            tv_progress_text.setText(getString(R.string.progress_first_load));
            startService(new Intent(this, FooDoNetService.class));
            isLoadDataServiceStarted = true;
        }
        if (AllLoaded())
            StartNextActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    /*
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = FCPublication.GetColumnNamesArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            Log.e("myTag", "cursor null");
            return;
        }

        //publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(data, false);

        //flagSQLLoaderFinished = true;

        if (AllLoaded())
            StartNextActivity();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
*/

/*
    @Override
    public void OnNotifiedToFetchData() {
        Log.i("food", "mainAct callback called");
    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {
        Log.i("food", "LoadUpdatedListOfPublications mainActivity");
    }
*/

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    private void ContinueAfterRegister() {

    }

    /*
    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        publicationsFromServer = new ArrayList<FCPublication>();
        publicationsFromServer.addAll(response.publications);
        flagHttpGetFinished = true;
        CompareAndUpdateLocalAndServer();
        //if(AllLoaded())
            //StartNextActivity();
    }

    @Override
    public void OnUpdateLocalDBComplete(ArrayList<FCPublication> publications) {
        publicationsUpdatedList = publications;
        flagSQLLoaderFinished = true;
        if(AllLoaded())
            StartNextActivity();
    }
    */

    Handler splashScreenHoldWaitCompeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (AllLoaded())
                        StartNextActivity();
                    break;
                default:
                    Log.e(MY_TAG, "Handler got unexpected msg.what");
                    break;
            }
        }
    };

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        if(response.newUserID > 0)
            CommonUtil.SaveMyUserID(this, response.newUserID);
        startService(new Intent(this, FooDoNetService.class));
        isLoadDataServiceStarted = true;
        tv_progress_text.setText(getString(R.string.progress_first_load));
    }

    private class SplashScreenHolder extends AsyncTask<Point, Void, Void> {


        @Override
        protected Void doInBackground(final Point... params) {
            final int secondsToSleep = params[0].x;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(secondsToSleep);
                        flagWaitTaskFinished = true;
                        splashScreenHoldWaitCompeleteHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    private void StartNextActivity() {
        // and start next activity
        Intent intent = new Intent(this, EntranceActivity.class);
        this.startActivity(intent);
        finish();
    }

    private void RegisterIfNotRegisteredYet() {
        if (CommonUtil.GetFromPreferencesIsDeviceRegistered(this)) {
            RegisterToGoogleFacebookIfNeeded();
        } else {
            File directory = new File(Environment.getExternalStorageDirectory()
                    + getResources().getString(R.string.image_folder_path));
            if (!directory.exists())
                directory.mkdirs();
            FooDoNetInstanceIDListenerService.StartRegisterToGCM(this);
        }
    }

    private void RegisterToGoogleFacebookIfNeeded() {
        if (CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this)) {
            registerTaskFinished = true;
            RepairPushNotifications();
            startService(new Intent(this, FooDoNetService.class));
            isGoogleFacebookChecked = true;
            if (AllLoaded())
                StartNextActivity();
        } else {
            Intent signInIntent = new Intent(this , SignInActivity.class);
            startActivityForResult(signInIntent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isGoogleFacebookChecked = true;
        switch (resultCode){
            case 1:
                InternalRequest ir = (InternalRequest)data.getSerializableExtra(InternalRequest.INTERNAL_REQUEST_EXTRA_KEY);
                if(ir != null){
                    if(ir.PhotoURL != null){
                        DownloadImageTask imageTask = new DownloadImageTask(ir.PhotoURL, 100, getString(R.string.image_folder_path));
                        imageTask.execute();
                    }
                    HttpServerConnectorAsync connectorAsync
                            = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                    connectorAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                    return;
                }
                Log.e(MY_TAG, "InternalRequest extra null");
                break;
            default:
                Log.i(MY_TAG, "User decided not to login with google/facebook");
                OnServerRespondedCallback(null);
                break;
        }
    }

    @Override
    public void onBroadcastReceived(Intent intent) {
        int regResult = 0;
        regResult = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, 0);
        switch (regResult) {
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_FAIL:
                Toast.makeText(getBaseContext(), "problem registering device!", Toast.LENGTH_LONG).show();
                // return;
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_SUCCESS:
                // if register succeed
                // check registration google/
                RegisterToGoogleFacebookIfNeeded();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_RELOAD_DATA_SUCCESS:
                registerTaskFinished = true;
                if (AllLoaded())
                    StartNextActivity();
                break;
        }
    }

    private void RepairPushNotifications() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_token), MODE_PRIVATE);
        String token = sp.getString(getString(R.string.shared_preferences_token_key), "");
        if (!TextUtils.isEmpty(token)) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            try {
                pubSub.unsubscribe(token, "/topics/global");
                pubSub.subscribe(token, getString(R.string.push_notification_prefix), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
