package DataModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Asher on 21.08.2015.
 */
public class RegisteredUserForPublication {

    private static final String MY_TAG = "food_RegForPublication";

    public static final String REGISTERED_FOR_PUBLICATION_KEY_ID = "_id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_ID_SERVER = "id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID = "publication_id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION = "publication_version";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_DATE = "date_of_registration";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID = "active_device_dev_uuid";

    private int id;
    public int getId(){
        return id;
    }
    public void setId(int val){
        id = val;
    }

    private int publication_id;
    public int getPublication_id(){
        return publication_id;
    }
    public void setPublication_id(int val){
        publication_id = val;
    }

    private int publication_version;
    public int getPublication_version(){return publication_version;}
    public void setPublication_version(int val){
        publication_version = val;
    }

    private Date date_registered;
    public Date getDate_registered(){return date_registered;}
    public long getDate_registered_unix_time(){return date_registered.getTime();}
    public void setDate_registered(Date val){
        date_registered = val;
    }
    public void setDate_registered(long val){
        date_registered = new Date(val * 1000);
    }

    private String device_registered_uuid;
    public String getDevice_registered_uuid(){
        return device_registered_uuid;
    }
    public void setDevice_registered_uuid(String val){
        device_registered_uuid = val;
    }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        REGISTERED_FOR_PUBLICATION_KEY_ID,
                        REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID,
                        REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION,
                        REGISTERED_FOR_PUBLICATION_KEY_DATE,
                        REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID
                };
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_ID, getId());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID, getPublication_id());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION, getPublication_version());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_DATE, getDate_registered_unix_time());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID, getDevice_registered_uuid());
        return cv;
    }

    public static ArrayList<RegisteredUserForPublication> GetArrayListOfRegisteredForPublicationsFromCursor(Cursor cursor) {
        ArrayList<RegisteredUserForPublication> result = new ArrayList<RegisteredUserForPublication>();

        if (cursor.moveToFirst()) {
            do {
                RegisteredUserForPublication rufp = new RegisteredUserForPublication();
                rufp.setId(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_ID)));
                rufp.setPublication_id(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID)));
                rufp.setPublication_version(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION)));
                rufp.setDate_registered(cursor.getLong(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_DATE)));
                rufp.setDevice_registered_uuid(cursor.getString(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID)));
                result.add(rufp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static ArrayList<RegisteredUserForPublication> GetArrayListOfRegisteredForPublicationsFromJSON(JSONArray ja) {
        ArrayList<RegisteredUserForPublication> result = new ArrayList<RegisteredUserForPublication>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                Log.i(MY_TAG, ja.getJSONObject(i).toString());
                result.add(ParseSingleRegisteredForPublicationFromJSON(ja.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static RegisteredUserForPublication ParseSingleRegisteredForPublicationFromJSON(JSONObject jo) {
        if (jo == null) return null;
        RegisteredUserForPublication rufp = new RegisteredUserForPublication();
        try {
            rufp.setId(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_ID_SERVER));
            rufp.setPublication_id(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID));
            rufp.setPublication_version(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION));
            rufp.setDate_registered(jo.getLong(REGISTERED_FOR_PUBLICATION_KEY_DATE));
            rufp.setDevice_registered_uuid(jo.getString(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return rufp;
    }


}
