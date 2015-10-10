package CommonUtilPackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.amazonaws.util.IOUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import upp.foodonet.R;

/**
 * Created by Asher on 01.09.2015.
 */
public class CommonUtil {

    private static final String MY_TAG = "food_CommonUtil";

    public static String GetIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static double GetKilometersBetweenLatLongs(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;
        double R = 6378.137; // Radius of earth in KM
        double dLat = (lat2 - lat1) * Math.PI / 180;
        double dLon = (lon2 - lon1) * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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

    public static String GetTokenFromSharedPreferences(Context context, int tokenRepositoryID, int tokenKeyID) {
        String token = "";
        SharedPreferences sp = context.getSharedPreferences(context.getResources().getString(tokenRepositoryID), Context.MODE_PRIVATE);
        token = sp.getString(context.getResources().getString(tokenKeyID), "");
        return token;
    }

    public static String GetDistanceString(LatLng point1, LatLng point2, Context context) {
        if (point1 == null || point2 == null) {
            return context.getResources().getString(R.string.pub_det_cant_get_distance);
        }
        if (context == null)
            throw new NullPointerException("got null context");
        double distance = CommonUtil.GetKilometersBetweenLatLongs(point1, point2);
        if (distance > 1) {
            distance = Math.round(distance);
            return String.valueOf(((int) distance))
                    + " " + context.getResources().getString(R.string.pub_det_km_from_you);
        } else {
            distance = Math.round(distance * 1000);
            return String.valueOf(((int) distance))
                    + " " + context.getResources().getString(R.string.pub_det_metr_from_you);
        }
    }

    public static double GetDistanceInKM(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null)
            return -1;
        return GetKilometersBetweenLatLongs(point1, point2);
    }

    public static BitmapDrawable GetBitmapDrawableFromFile(String fileName, String imageSubFolder, int width, int heigth) {
        File photo = new File(fileName);
        if(!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        if (!photo.exists()) return null;
        try {
            FileInputStream fis = new FileInputStream(photo.getPath());
            byte[] imageBytes = IOUtils.toByteArray(fis);
            Bitmap bImage = CommonUtil.decodeScaledBitmapFromByteArray(imageBytes, width, heigth);
            return new BitmapDrawable(bImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void CopyFile(File src, File dst) throws IOException {
        if (!src.exists())
            throw new IOException("CopyFile - source file doesn't exists");
        if (dst.exists()) {
            Log.w(MY_TAG, "CopyFile - destination file exists and will be overwritten");
            dst.delete();
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String GetFilterStringFromPreferences(Context context) {
        SharedPreferences sp
                = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_text_filter_key),
                Context.MODE_PRIVATE);
        String result = sp.getString(context.getString(R.string.shared_preferences_text_filter_text_key), "");
        return result;
    }

    public static LatLng GetFilterLocationFromPreferences(Context context) {
        SharedPreferences sp
                = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_my_location_key), Context.MODE_PRIVATE);
        float lat = sp.getFloat(context.getString(R.string.shared_preferences_my_latitude_key), -1000);
        float lon = sp.getFloat(context.getString(R.string.shared_preferences_my_longitude_key), -1000);
        return new LatLng(lat, lon);
    }

    public static void UpdateFilterMyLocationPreferences(Context context, LatLng myLocation) {
        if (myLocation == null) {
            Log.e(MY_TAG, "UpdateFilterMyLocationPreferences got null location");
        }
        Log.i(MY_TAG, "UpdateFilterMyLocationPreferences saves myLocation: lat:" + myLocation.latitude + " long:" + myLocation.longitude);
        SharedPreferences sp
                = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_my_location_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (sp.contains(context.getString(R.string.shared_preferences_my_latitude_key))) {
            editor.remove(context.getString(R.string.shared_preferences_my_latitude_key));
            editor.commit();
        }
        if (sp.contains(context.getString(R.string.shared_preferences_my_longitude_key))) {
            editor.remove(context.getString(R.string.shared_preferences_my_longitude_key));
            editor.commit();
        }
        editor.putFloat(context.getString(R.string.shared_preferences_my_latitude_key), ((float) myLocation.latitude));
        editor.putFloat(context.getString(R.string.shared_preferences_my_longitude_key), ((float) myLocation.longitude));
        editor.commit();
    }

    public static InputStream ConvertFileToInputStream(String fileName) {
        InputStream is = null;
        File photo = new File(Environment.getExternalStorageDirectory(), fileName);
        if (!photo.exists()) return null;

        try {
            is = new FileInputStream(photo.getPath());

            //is.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return is;
    }

    public static byte[] CompressImageByteArrayByMaxSize(byte[] result, int maxImageWidthHeight) {
//        bitmap = BitmapFactory.decodeByteArray(result, 0,
//                result.length);
        Bitmap bitmap = decodeScaledBitmapFromByteArray(result, 800, 800);
        bitmap = CompressBitmapByMaxSize(bitmap, maxImageWidthHeight);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap CompressBitmapByMaxSize(Bitmap bitmap, int maxImageWidthHeight) {
        int finalWidth = bitmap.getWidth();
        int finalHeight = bitmap.getHeight();
        double scaleRate = 1;
        if (finalWidth > maxImageWidthHeight || finalHeight > maxImageWidthHeight)
            scaleRate = (finalWidth > finalHeight ? finalWidth : finalHeight) / maxImageWidthHeight;
        finalWidth = (int) Math.round(finalWidth / scaleRate);
        finalHeight = (int) Math.round(finalHeight / scaleRate);
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    public static byte[] GetByteArrayFromFile(String fullPath) {
        File file = new File(fullPath);
        if (file.exists()) {
            try {
                InputStream is = new FileInputStream(file);
                return IOUtils.toByteArray(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void CopyImageFileWithCompressionBySize(File fSource, File fDestination, int maxSize) {
        if (!fSource.exists()) return;
        byte[] result = CompressImageByteArrayByMaxSize(GetByteArrayFromFile(fSource.getAbsolutePath()), maxSize);
        if (fDestination.exists()) fDestination.delete();
        OutputStream out = null;
        try {
            out = new FileOutputStream(fDestination);
            out.write(result, 0, result.length);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String GetDateTimeStringFromCalendar(Calendar calendar){
        if(calendar == null)
            return "";
        String hours = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minutes = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.MINUTE));
        String days = (calendar.get(Calendar.DATE) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.DATE));
        String month = (calendar.get(Calendar.MONTH) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.MONTH));
        String years = String.valueOf(calendar.get(Calendar.YEAR));
        return hours + ":" + minutes + " " + days + "/" + month + "/" + years;
    }
}
