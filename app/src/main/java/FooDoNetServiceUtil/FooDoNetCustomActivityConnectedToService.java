package FooDoNetServiceUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServerClasses.ConnectionDetector;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.InternalRequest;
import upp.foodonet.FooDoNetService;

/**
 * Created by Asher on 31-Jul-15.
 */
public abstract class FooDoNetCustomActivityConnectedToService extends FragmentActivity implements IFooDoNetServiceCallback {

    FooDoNetService fooDoNetService;
    boolean isBoundedToService;

    private final String MY_TAG = "food_abstract_fActivity";

    public IFooDoNetServiceCallback serviceCallback = this;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onStart() {
        Intent intent = new Intent(this, FooDoNetService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(mConnection);
        super.onStop();
    }

    @Override
    protected void onResume() {
        if(!CheckInternetConnection())
            OnInternetNotConnected();
        if(!CheckPlayServices())
            OnGooglePlayServicesCheckError();
        super.onResume();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FooDoNetService.FooDoNetCustomServiceBinder mBinder = (FooDoNetService.FooDoNetCustomServiceBinder) service;
            fooDoNetService = mBinder.getService();
            fooDoNetService.StartScheduler(serviceCallback);
            isBoundedToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBoundedToService = false;
        }
    };

    protected boolean CheckPlayServices() {
        Log.i(MY_TAG, "checking isGooglePlayServicesAvailable...");
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.e(MY_TAG, "UserRecoverableError: " + resultCode);
            }
            Log.e(MY_TAG, "Google Play Services Error: " + resultCode);
            return false;
        }
        Log.w(MY_TAG, "Google Play Services available!");
        return true;
    }

    protected boolean CheckInternetConnection(){
        Log.i(MY_TAG, "Checking internet connection...");
        ConnectionDetector cd = new ConnectionDetector(getBaseContext());
        return cd.isConnectingToInternet();
    }

    @Override
    public abstract void OnNotifiedToFetchData();

    @Override
    public abstract void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList);

    public abstract void OnGooglePlayServicesCheckError();

    public abstract void OnInternetNotConnected();
}
