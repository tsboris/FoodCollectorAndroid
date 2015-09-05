package FooDoNetServiceUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

import CommonUtilPackage.GetMyLocationAsync;
import CommonUtilPackage.IGotMyLocationCallback;
import DataModel.FCPublication;
import FooDoNetServerClasses.ConnectionDetector;
import upp.foodonet.FooDoNetService;

/**
 * Created by Asher on 31-Jul-15.
 */
public abstract class FooDoNetCustomActivityConnectedToService
        extends FragmentActivity
        implements IFooDoNetServiceCallback, IGotMyLocationCallback {

    FooDoNetService fooDoNetService;
    boolean isBoundedToService;
    Messenger boundedService;

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
        if (!CheckInternetConnection())
            OnInternetNotConnected();
        if (!CheckPlayServices())
            OnGooglePlayServicesCheckError();
        super.onResume();
    }

/*
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
*/

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

    protected boolean CheckInternetConnection() {
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

    @Override
    public void OnGotMyLocationCallback(Location location){ }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundedService = new Messenger(service);
            isBoundedToService = true;
            Message m = Message.obtain(null, FooDoNetService.ACTION_START);
            m.replyTo = callbackMessenger;
            try {
                boundedService.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            boundedService = null;
            isBoundedToService = false;
        }
    };

    class IncomingHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FooDoNetService.ACTION_WORK_DONE:
                    OnNotifiedToFetchData();
                    break;
                case GetMyLocationAsync.ACTION_GET_MY_LOCATION:
                    OnGotMyLocationCallback((Location)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger callbackMessenger = new Messenger(new IncomingHandler());

    protected void StartGetMyLocation(){
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        GetMyLocationAsync locationAsync = new GetMyLocationAsync(locationManager, callbackMessenger);
        locationAsync.execute();
    }

}
