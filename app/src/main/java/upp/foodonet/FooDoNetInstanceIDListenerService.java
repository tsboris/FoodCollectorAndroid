package upp.foodonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

import java.io.IOException;

import DataModel.UserRegisterData;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.InternalRequest;

public class FooDoNetInstanceIDListenerService extends IntentService implements IFooDoNetServerCallback {

    private static final String MY_TAG = "food_intentService_ID";

    private static final String ACTION_REGISTER_TO_GCM = "1";

    private static IFooDoNetServerCallback parentForCallback;

    private static final String REGISTRATION_FIELD_DEVICE_ID = "dev_uuid";
    private static final String REGISTRATION_FIELD_PUSH_TOKEN = "remote_notification_token";
    private static final String REGISTRATION_FIELD_IS_IOS = "is_ios";
    private static final String REGISTRATION_FIELD_LATITUDE = "last_location_latitude";
    private static final String REGISTRATION_FIELD_LONGITUDE = "";

    public static void StartRegisterToGCM(Context context) {
        Intent intent = new Intent(context, FooDoNetInstanceIDListenerService.class);
        intent.setAction(ACTION_REGISTER_TO_GCM);
        parentForCallback = (IFooDoNetServerCallback)context;
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

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        Log.w(MY_TAG, "Got imei: " + imei);

        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if (locationManager == null)
        {
            Log.e(MY_TAG, "could not get location!");
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
        {
            Log.e(MY_TAG, "could not get location!");
            return;
        }

        UserRegisterData userData = new UserRegisterData(imei, token, location.getLatitude(), location.getLongitude());

        HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), this);
        connector.execute(
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
         parentForCallback.OnServerRespondedCallback(response);
    }
}
