package CommonUtilPackage;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Asher on 01.09.2015.
 */
public class CommonUtil {

    public static String GetIMEI(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

}
