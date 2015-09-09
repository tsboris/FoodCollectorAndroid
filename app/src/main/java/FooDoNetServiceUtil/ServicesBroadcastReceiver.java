package FooDoNetServiceUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Asher on 09.09.2015.
 */
public class ServicesBroadcastReceiver extends BroadcastReceiver {

    public static final String MY_TAG = "food_broadcastRec";

    public static final String BROADCAST_REC_INTENT_FILTER = "upp.foodonet.broadcast.receiver.common";

    private IBroadcastReceiverCallback callbackListener;

    public static final String BROADCAST_REC_EXTRA_ACTION_KEY = "action_extra_key";

    public static final int ACTION_CODE_REGISTRATION_SUCCESS = 1;
    public static final int ACTION_CODE_REGISTRATION_FAIL = 2;
    public static final int ACTION_CODE_RELOAD_DATA_SUCCESS = 11;
    public static final int ACTION_CODE_RELOAD_DATA_FAIL = 12;

    public ServicesBroadcastReceiver(IBroadcastReceiverCallback callback){
        callbackListener = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MY_TAG, "some broadcast received!");
        if(callbackListener != null)
            callbackListener.onBroadcastReceived(intent);
    }
}
