package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import DataModel.FCPublication;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class SplashScreenActivity
        extends FooDoNetCustomActivityConnectedToService
        implements LoaderManager.LoaderCallbacks<Cursor>,
        IFooDoNetServerCallback {
    private int min_splash_screen_duration;
    ArrayList<FCPublication> publicationsFromDB, publicationsFromServer, publicationsUpdatedList;
    boolean flagWaitTaskFinished, flagSQLLoaderFinished;

    private final String PREFERENCES_KEY_BOOL_IF_REGISTERED = "ifRegistered";
    private final String MY_TAG = "food_splashscreen";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        min_splash_screen_duration = getResources().getInteger(R.integer.min_splash_screen_time);
        Point p = new Point(min_splash_screen_duration, 0);
        RegisterIfNotRegisteredYet();
        SplashScreenHolder ssh = new SplashScreenHolder();
        ssh.execute(p);

        flagSQLLoaderFinished = true;
        /*HttpServerConnectorAsync connecter = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), this);
        connecter.execute(new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS));*/
        //getSupportLoaderManager().initLoader(0, null, this);
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

    @Override
    public void OnNotifiedToFetchData() {
        Log.i("food", "mainAct callback called");
    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {
        Log.i("food", "LoadUpdatedListOfPublications mainActivity");
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        if (response == null) {
            Log.e(MY_TAG, "server connection task called back with null response");
            return;
        }
        switch (response.Status) {
            case InternalRequest.STATUS_FAIL:
                Log.e(MY_TAG, "server callback status failed");
                return;
            case InternalRequest.STATUS_OK:
/*
                switch (response.ActionCommand){
                    case InternalRequest.ACTION_POST_REGISTER:
                        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(PREFERENCES_KEY_BOOL_IF_REGISTERED, true);
                        editor.commit();
                        break;
                    default:
                        Log.e(MY_TAG, "Unexpected callback to splashscreen from server connecter. Action: " + response.ActionCommand);
                        return;

                }*/
        }
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
            //Thread.sleep(1000 * params[0].x, 0);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (AllLoaded())
                StartNextActivity();
        }
    }

    private void StartNextActivity() {
        Intent intent = new Intent(this, EntranceActivity.class);
        //publicationsFromDB.addAll(publicationsFromServer);
        intent.putExtra("loaderResult", publicationsUpdatedList);
        this.startActivity(intent);
    }

    private void RegisterIfNotRegisteredYet() {
        SharedPreferences preference = getPreferences(Context.MODE_PRIVATE);
        if (preference.getBoolean(PREFERENCES_KEY_BOOL_IF_REGISTERED, false)) {
            OnServerRespondedCallback(new InternalRequest(InternalRequest.ACTION_POST_REGISTER, true));
            return;
        } else {
            FooDoNetInstanceIDListenerService.StartRegisterToGCM(this);
        }

        //TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //String imei = tm.getDeviceId();

    }


    private boolean AllLoaded() {
        return flagSQLLoaderFinished && flagWaitTaskFinished;
    }
}
