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

import FooDoNetServerClasses.InternalRequest;

public class FooDoNetInstanceIDListenerService extends IntentService {

    private static final String MY_TAG = "food_intentService_ID";

    private static final String ACTION_REGISTER_TO_GCM = "1";

    private static final String REGISTRATION_FIELD_DEVICE_ID = "dev_uuid";
    private static final String REGISTRATION_FIELD_PUSH_TOKEN = "remote_notification_token";
    private static final String REGISTRATION_FIELD_IS_IOS = "is_ios";
    private static final String REGISTRATION_FIELD_LATITUDE = "last_location_latitude";
    private static final String REGISTRATION_FIELD_LONGITUDE = "";

    public static void StartRegisterToGCM(Context context) {
        Intent intent = new Intent(context, FooDoNetInstanceIDListenerService.class);
        intent.setAction(ACTION_REGISTER_TO_GCM);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
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
                    throw new UnsupportedOperationException("Not yet implemented, code: " + action);
            }
        }
    }

    private void RegisterToGCM(){
        InstanceID instanceID = InstanceID.getInstance(this);
        String token;
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
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);



    }

    private JSONObject GetRegistrationJSONObject(String imei, String pushKey, Location location){
        //JSONObject res = new JSONObject();
        //res.put()
        return null;
    }

}
