package upp.foodonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

import java.io.IOException;

import CommonUtilPackage.CommonUtil;
import DataModel.UserRegisterData;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

public class FooDoNetInstanceIDListenerService extends IntentService implements IFooDoNetServerCallback {

    private static final String MY_TAG = "food_intentService_ID";

    private static final String ACTION_REGISTER_TO_GCM = "1";

    //private static IFooDoNetServerCallback parentForCallback;

    public static void StartRegisterToGCM(Context context) {
        Intent intent = new Intent(context, FooDoNetInstanceIDListenerService.class);
        intent.setAction(ACTION_REGISTER_TO_GCM);
        //parentForCallback = (IFooDoNetServerCallback)context;
        context.startService(intent);
    }

    public FooDoNetInstanceIDListenerService() {
        super("FooDoNetInstanceIDListenerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action){
                case ACTION_REGISTER_TO_GCM:
                    RegisterToGCM();
                    break;
                default:
                    return;
                //throw new UnsupportedOperationException("Not yet implemented, code: " + action);
            }
        }
    }

    private void RegisterToGCM(){
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = "";
        try {
            token = instanceID.getToken(getString(R.string.notifications_server_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.w(MY_TAG, "Got token: " + token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String imei = CommonUtil.GetIMEI(this);
        Log.w(MY_TAG, "Got imei: " + imei);

        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        Location location = null;
        double lat, lon;
        if (locationManager == null)
        {
            Log.e(MY_TAG, "could not get location!");
            return;
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location == null)
        {
            Log.e(MY_TAG, "could not get location!");
            lat = 0;
            lon = 0;
        } else {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        UserRegisterData userData = new UserRegisterData(imei, token, lat, lon);

        HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), this);
        connector.setContextForBroadcasting(this);
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new InternalRequest(InternalRequest.ACTION_POST_REGISTER,
                        getResources().getString(R.string.register_new_device), userData));
    }

    private JSONObject GetRegistrationJSONObject(String imei, String pushKey, Location location){
        //JSONObject res = new JSONObject();
        //res.put()
        return null;
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        //parentForCallback.OnServerRespondedCallback(response);
        Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
        switch (response.Status){
            case InternalRequest.STATUS_OK:
                Log.i(MY_TAG, "got server callback registration ok");
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_SUCCESS);
                break;
            case InternalRequest.STATUS_FAIL:
                Log.i(MY_TAG, "got server callback registration fail");
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_FAIL);
                break;
            default:
                Log.i(MY_TAG, "unexpected callback status from server!");
                return;
        }
        sendBroadcast(intent);
    }
}
