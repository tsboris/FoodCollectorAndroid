package CommonUtilPackage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.amazonaws.util.IOUtils;
import com.facebook.Profile;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.provider.Settings.Secure;
import android.widget.EditText;

import DataModel.FCPublication;
import upp.foodonet.R;

/**
 * Created by Asher on 01.09.2015.
 */
public class CommonUtil {

    private static final String MY_TAG = "food_CommonUtil";

    public static String GetIMEI(Context context) {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        return tm.getDeviceId();
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
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
        if(fileName == null || fileName.length() == 0) return null;
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        if (!photo.exists()) return null;
        BitmapDrawable result = null;
        try {
            FileInputStream fis = new FileInputStream(photo.getPath());
            byte[] imageBytes = IOUtils.toByteArray(fis);
            Bitmap bImage = CommonUtil.decodeScaledBitmapFromByteArray(imageBytes, width, heigth);
            result = new BitmapDrawable(bImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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

    public static InputStream ConvertFileToInputStream(String fileName, String imageSubFolder) {
        InputStream is = null;

        File photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);

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

    public static String GetDateTimeStringFromGate(Date date){
        if(date == null)
            return "";
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return GetDateTimeStringFromCalendar(c);
    }

    public static String GetDateTimeStringFromCalendar(Calendar calendar) {
        if (calendar == null)
            return "";
        String hours = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minutes = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.MINUTE));
        String days = (calendar.get(Calendar.DATE) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.DATE));
        String month = (calendar.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String years = String.valueOf(calendar.get(Calendar.YEAR));
        return hours + ":" + minutes + " " + days + "/" + month + "/" + years;
    }

    public static Map<String, LatLng> GetPreviousAddressesMapFromCursor(Cursor cursor){
        Map<String, LatLng> result = new HashMap<>();
        if(cursor.moveToFirst())
            do{
                result.put(cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_ADDRESS_KEY)),
                        new LatLng(cursor.getDouble(cursor.getColumnIndex(FCPublication.PUBLICATION_LATITUDE_KEY)),
                                cursor.getDouble(cursor.getColumnIndex(FCPublication.PUBLICATION_LONGITUDE_KEY))));
            } while (cursor.moveToNext());
        return result;
    }

    public static BitmapDrawable GetImageFromFileForPublicationCursor(Context context, Cursor cursor, int imageSize){
        final int id = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY));
        final int version = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_VERSION_KEY));
        final boolean cursorHasPhotoUrl = cursor.getColumnIndex(FCPublication.PUBLICATION_PHOTO_URL) != -1;
        String imagePath = "";
        if(cursorHasPhotoUrl)
            imagePath = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_PHOTO_URL));
        return GetImageFromFileForPublication(context, id, version, imagePath, imageSize);
    }

    public static BitmapDrawable GetImageFromFileForPublication(Context context, int id, int version, String imagePath, int imageSize){
        BitmapDrawable imageDrawable = null;
        if (id <= 0) {
            Log.i(MY_TAG, "negative id");
            imageDrawable = CommonUtil.GetBitmapDrawableFromFile("n" + (id * -1) + "." + version + ".jpg",
                    context.getString(R.string.image_folder_path), imageSize, imageSize);
        } else
            imageDrawable = CommonUtil.GetBitmapDrawableFromFile(id + "." + version + ".jpg",
                    context.getString(R.string.image_folder_path), imageSize, imageSize);
        if(imageDrawable == null && imagePath != null && imagePath.length() > 0){
            imageDrawable = CommonUtil.GetBitmapDrawableFromFile(imagePath, "", imageSize, imageSize);
        }
        return imageDrawable;
    }

    public static ProgressDialog ShowProgressDialog(Context context, String message){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(message);
        progressDialog.show();
        return progressDialog;
    }

    public static String GetFileNameByPublication(FCPublication publication){
        return String.valueOf(publication.getUniqueId() > 0
                ? String.valueOf(publication.getUniqueId())
                : "n" + String.valueOf(publication.getUniqueId() * -1))
                + "." + String.valueOf(publication.getVersion()) + ".jpg";
    }

    public static void PutCommonPreferenceIsDataLoaded(Context context, boolean isDataLoaded){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_data_loaded), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_data_loaded_key), isDataLoaded);
        editor.commit();
        Log.i(MY_TAG, "IsDataLoaded set to: " + String.valueOf(isDataLoaded));
    }

    public static boolean GetFromPreferencesIsDataLoaded(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_data_loaded), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.shared_preferences_data_loaded_key), false);
    }

    public static void PutCommonPreferenceIsRegisteredDevice(Context context, boolean isDataLoaded){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_is_device_registered), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_is_device_registered_key), isDataLoaded);
        editor.commit();
        Log.i(MY_TAG, "IsRegistered set to: " + String.valueOf(isDataLoaded));
    }

    public static boolean GetFromPreferencesIsDeviceRegistered(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_is_device_registered), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.shared_preferences_is_device_registered_key), false);
    }

    public static void PutCommonPreferencesIsRegisteredGoogleFacebook(Context context, GoogleSignInAccount account){
        PutCommonPreferencesSocialAccountData(context, "google", account.getDisplayName(), account.getIdToken());
    }

    private static void PutCommonPreferencesIsRegisteredGoogleFacebook(Context context, Profile account){
        PutCommonPreferencesSocialAccountData(context, "facebook", account.getName(), account.getId());
    }

    private static void PutCommonPreferencesSocialAccountData(Context context, String socialAccountType, String socialAccountName, String socialAccountToken){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_is_registered_to_google_facebook), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_is_registered_to_google_facebook_key), true);
        editor.putString(context.getString(R.string.shared_preferences_social_account_type_key), socialAccountType);
        editor.putString(context.getString(R.string.shared_preferences_social_account_name_key), socialAccountName);
        editor.putString(context.getString(R.string.shared_preferences_social_account_token_key), socialAccountToken);
        editor.commit();
        Log.i(MY_TAG, "Registered to " + socialAccountType + ", name: " + socialAccountName);
    }

    public static boolean GetFromPreferencesIsRegisteredToGoogleFacebook(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_is_registered_to_google_facebook), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.shared_preferences_is_registered_to_google_facebook_key), false);
    }

    public static boolean RemoveImageByPublication(FCPublication publication, Context context){
        File img = new File(Environment.getExternalStorageDirectory()
                + context.getString(R.string.image_folder_path), GetFileNameByPublication(publication));
        if(!img.exists()) return true;
        return img.delete();
    }

    public static void ReportLocationToServer(Context context){
        GetMyLocationAsync locationAsync = new GetMyLocationAsync(
                (LocationManager)context.getSystemService(Context.LOCATION_SERVICE), context);
        locationAsync.switchToReportLocationMode(true);
        locationAsync.setIMEI(GetIMEI(context));
        locationAsync.execute();
    }

    public static boolean CheckPhoneNumberString(Context context, String phoneNumber) {
        String phonePattern = context.getString(R.string.regex_israel_phone_number);
        return phoneNumber.matches(phonePattern);
    }

    public static void SetEditTextIsValid(Context context, EditText field, boolean isValid) {
        field.getBackground()
                .setColorFilter(isValid ? context.getResources().getColor(R.color.validation_green_text_color) :
                        context.getResources().getColor(R.color.validation_red_text_color), PorterDuff.Mode.SRC_ATOP);
        Bitmap validationBitmap = CommonUtil.decodeScaledBitmapFromDrawableResource(context.getResources(),
                isValid ? R.drawable.validation_ok : R.drawable.validation_wrong,
                context.getResources().getDimensionPixelSize(R.dimen.address_dialog_validation_img_size),
                context.getResources().getDimensionPixelSize(R.dimen.address_dialog_validation_img_size));
        Drawable validationDrawable = new BitmapDrawable(validationBitmap);
        field.setCompoundDrawablesWithIntrinsicBounds(validationDrawable, null, null, null);
    }

    public static void RemoveValidationFromEditText(Context context, EditText field) {
        field.getBackground().setColorFilter(context.getResources()
                .getColor(R.color.basic_blue), PorterDuff.Mode.SRC_ATOP);
        field.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private static Tracker GetGoogleAnalyticsTracker(Context context){
        return GoogleAnalytics.getInstance(context).newTracker(context.getString(R.string.google_analytics_id));
    }

    public static void PostGoogleAnalyticsUIEvent(Context context, String screenName, String uiControlName, String uiEventType){
        Tracker tracker = GetGoogleAnalyticsTracker(context);
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.EventBuilder().setCategory("UI").setAction(uiEventType).setLabel(uiControlName).build());
    }

    public static void PostGoogleAnalyticsActivityOpened(Context context, String screenName){
        Tracker tracker = GetGoogleAnalyticsTracker(context);
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.EventBuilder().setCategory("ActivityOpened").build());
    }

}
