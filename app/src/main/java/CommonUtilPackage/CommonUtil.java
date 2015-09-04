package CommonUtilPackage;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Asher on 01.09.2015.
 */
public class CommonUtil {

    public static String GetIMEI(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static double GetKilometersBetweenLatLongs(LatLng point1, LatLng point2){
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;
            double R = 6378.137; // Radius of earth in KM
            double dLat = (lat2 - lat1) * Math.PI / 180;
            double dLon = (lon2 - lon1) * Math.PI / 180;
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                            Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double d = R * c;
            return d; // meters
    }



}
