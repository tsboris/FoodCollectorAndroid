package upp.foodonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import FooDoNetServerClasses.InternalRequest;

public class FooDoNetInstanceIDListenerService extends IntentService {

    private static final String MY_TAG = "food_intentService_ID";

    private static final String ACTION_REGISTER_TO_GCM = "1";


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
                    throw new UnsupportedOperationException("Not yet implemented");
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


    }



}
