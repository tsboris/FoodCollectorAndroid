package CommonUtilPackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;

import com.google.android.gms.maps.model.LatLng;

import upp.foodonet.R;

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

    public static Bitmap decodeScaledBitmapFromSdCard(String filePath,
                                                      int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static Bitmap decodeScaledBitmapFromByteArray(byte[] bytes,
                                                           int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static Bitmap decodeScaledBitmapFromDrawableResource(Resources resources, int drawableID,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, drawableID, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, drawableID, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static String GetTokenFromSharedPreferences(Context context, int tokenRepositoryID, int tokenKeyID){
        String token = "";
        SharedPreferences sp = context.getSharedPreferences(context.getResources().getString(tokenRepositoryID), Context.MODE_PRIVATE);
        token = sp.getString(context.getResources().getString(tokenKeyID), "");
        return token;
    }

    public static String GetDistanceString(LatLng point1, LatLng point2, Context context){
        if(point1 == null || point2 == null){
            return context.getResources().getString(R.string.pub_det_cant_get_distance);
        }
        if(context == null)
            throw new NullPointerException("got null context");
        double distance = CommonUtil.GetKilometersBetweenLatLongs(point1, point2);
        if(distance > 1){
            distance = Math.round(distance);
            return String.valueOf(((int) distance))
                    + " " + context.getResources().getString(R.string.pub_det_km_from_you);
        } else {
            distance = Math.round(distance * 1000);
            return String.valueOf(((int) distance))
                    + " " + context.getResources().getString(R.string.pub_det_metr_from_you);
        }
    }

}
