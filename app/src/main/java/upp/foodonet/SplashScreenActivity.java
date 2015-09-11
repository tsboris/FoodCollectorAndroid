package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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
import FooDoNetServiceUtil.ServicesBroadcastReceiver;


public class SplashScreenActivity
        extends FooDoNetCustomActivityConnectedToService  {
    private int min_splash_screen_duration;
    ArrayList<FCPublication> publicationsFromDB, publicationsFromServer, publicationsUpdatedList;
    boolean flagWaitTaskFinished, registerTaskFinished;

    private final String PREFERENCES_KEY_BOOL_IF_REGISTERED = "ifRegistered";
    private final String MY_TAG = "food_splashscreen";

    private boolean AllLoaded() {
        return registerTaskFinished && flagWaitTaskFinished;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        min_splash_screen_duration = getResources().getInteger(R.integer.min_splash_screen_time);
        Point p = new Point(min_splash_screen_duration, 0);
        RegisterIfNotRegisteredYet();
        SplashScreenHolder ssh = new SplashScreenHolder();
        ssh.execute(p);
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

    private class SplashScreenHolder extends AsyncTask<Point, Void, Void> {


        @Override
        protected Void doInBackground(final Point... params) {
            final int secondsToSleep = params[0].x;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(secondsToSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            flagWaitTaskFinished = true;
            splashScreenHoldWaitCompeleteHandler.sendEmptyMessage(0);
        }
    }

    private void StartNextActivity() {
        // if register succeed or already registered
        // start scheduler service
        startService(new Intent(this, FooDoNetService.class));
        // and start next activity
        Intent intent = new Intent(this, EntranceActivity.class);
        this.startActivity(intent);
    }

    private void RegisterIfNotRegisteredYet() {
        SharedPreferences preference = getPreferences(Context.MODE_PRIVATE);
        if (preference.getBoolean(PREFERENCES_KEY_BOOL_IF_REGISTERED, false)) {
            registerTaskFinished = true;
            if(AllLoaded())
                StartNextActivity();
            return;
        } else {
            FooDoNetInstanceIDListenerService.StartRegisterToGCM(this);
        }

    }

    @Override
    public void onBroadcastReceived(Intent intent) {
        int regResult = 0;
        regResult = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, 0);
        switch (regResult){
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_SUCCESS:
                SharedPreferences sp = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(PREFERENCES_KEY_BOOL_IF_REGISTERED, true);
                editor.commit();
                registerTaskFinished = true;
                if(AllLoaded())
                    StartNextActivity();
                break;

        }
    }
}
