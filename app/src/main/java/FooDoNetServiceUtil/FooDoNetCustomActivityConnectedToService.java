package FooDoNetServiceUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
        implements IBroadcastReceiverCallback {
    //implements IFooDoNetServiceCallback, IGotMyLocationCallback {

    //FooDoNetService fooDoNetService;
    //boolean isBoundedToService;
    //protected Messenger boundedService;
    //private static boolean isServiceRunning;
    //protected Intent serviceIntent;

    ServicesBroadcastReceiver servicesBroadcastReceiver;

    private final String MY_TAG = "food_abstract_fActivity";

    //public IFooDoNetServiceCallback serviceCallback = this;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        //boundedService = getIntent().getExtras().getParcelable("service");
    }

    @Override
    protected void onStart() {
        if (servicesBroadcastReceiver == null) {
            servicesBroadcastReceiver = new ServicesBroadcastReceiver(this);
            IntentFilter filter = new IntentFilter(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
            registerReceiver(servicesBroadcastReceiver, filter);
        }
/*
        if(!isServiceRunning){
            serviceIntent = new Intent(this, FooDoNetService.class);
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            isServiceRunning = true;
        }
        if (boundedService != null) {
            isBoundedToService = true;
            Message m = Message.obtain(null, FooDoNetService.ACTION_START);
            m.replyTo = callbackMessenger;
            try {
                boundedService.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
*/
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (servicesBroadcastReceiver != null) {
            unregisterReceiver(servicesBroadcastReceiver);
            servicesBroadcastReceiver = null;
        }

        super.onPause();
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

/*
    @Override
    public abstract void OnNotifiedToFetchData();

    @Override
    public abstract void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList);
*/

    @Override
    public void onBroadcastReceived(Intent intent) {
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_SUCCESS:
                Location location = (Location)intent.getParcelableExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_LOCATION_KEY);
                OnGotMyLocationCallback(location);
                break;
        }
    }

    public abstract void OnGooglePlayServicesCheckError();

    public abstract void OnInternetNotConnected();

    public void OnGotMyLocationCallback(Location location) {
    }

    /*
        protected ServiceConnection mConnection = new ServiceConnection() {
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

    */
    class IncomingHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GetMyLocationAsync.ACTION_GET_MY_LOCATION:
                    OnGotMyLocationCallback((Location) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger callbackMessenger = new Messenger(new IncomingHandler());


    protected void StartGetMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        GetMyLocationAsync locationAsync = new GetMyLocationAsync(locationManager, this);
        locationAsync.execute();
    }

}
