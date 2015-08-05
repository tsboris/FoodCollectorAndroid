package FooDoNetServiceUtil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServerClasses.HttpServerConnecterAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.InternalRequest;
import upp.foodonet.FooDoNetService;

/**
 * Created by Asher on 31-Jul-15.
 */
public abstract class FooDoNetCustomActivityConnectedToService extends FragmentActivity implements IFooDoNetServiceCallback {

    FooDoNetService fooDoNetService;
    boolean isBoundedToService;

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

    @Override
    public abstract void OnNotifiedToFetchData();

    @Override
    public abstract void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList);
}
