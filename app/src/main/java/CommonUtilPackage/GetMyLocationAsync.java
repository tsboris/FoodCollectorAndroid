package CommonUtilPackage;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by Asher on 04.09.2015.
 */
public class GetMyLocationAsync extends AsyncTask<Void, Void, Void> {

    public static final int ACTION_GET_MY_LOCATION = 3;
    Messenger handler;
    Location location;
    LocationManager locationManager;

    private static final String MY_TAG = "food_myLocationAsync";

    public GetMyLocationAsync(LocationManager lManager, Messenger h){
        locationManager = lManager;
        handler = h;
    }

    @Override
    protected Void doInBackground(Void... params) {
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
        {
            Log.e(MY_TAG, "could not get location!");
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Message m = Message.obtain(null, ACTION_GET_MY_LOCATION, location);
        try {
            handler.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
