package DataModel;

import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Asher on 19.08.2015.
 */
public class UserRegisterData implements Serializable, ICanWriteSelfToJSONWriter {

    private static final String MY_TAG = "food_UserRegisterData";

    public static final String USER_DATA_DEV_UUID_FIELD_NAME = "dev_uuid";
    public static final String USER_DATA_PUSH_TOKEN = "remote_notiﬁcation_token";
    public static final String USER_DATA_LATITUDE = "last_location_latitude";
    public static final String USER_DATA_LONGITUDE = "last_location_longitude";
    public static final String USER_DATA_IS_IOS = "is_ios";

    public UserRegisterData(String imei, String token, double latitude, double longitude){
        set_Imei(imei);
        set_Push_Token(token);
        set_Latitude(latitude);
        set_Longitude(longitude);
    }

    private String _imei;
    public void set_Imei(String imei){
        _imei = imei;
    }
    public String get_Imei(){
        return _imei;
    }

    private String _push_token;
    public void set_Push_Token(String token){
        _push_token = token;
    }
    public String get_Push_Token(){
        return _push_token;
    }

    private double _latitude;
    public void set_Latitude(double latitude){
        _latitude = latitude;
    }
    public double get_Latitude(){
        return _latitude;
    }

    private double _longitude;
    public void set_Longitude(double longitude){
        _longitude = longitude;
    }
    public double get_Longitude(){
        return _longitude;
    }

    private final static boolean _is_ios = false;
    public boolean get_Is_IOS(){ return _is_ios; }

    public void WriteSelfToJSONWriter(JsonWriter writer){
        try {
            writer.beginObject();
            writer.name(USER_DATA_DEV_UUID_FIELD_NAME).value(get_Imei());
            writer.name(USER_DATA_PUSH_TOKEN).value(get_Push_Token());
            writer.name(USER_DATA_LATITUDE).value(get_Latitude());
            writer.name(USER_DATA_LONGITUDE).value(get_Longitude());
            writer.name(USER_DATA_IS_IOS).value(Boolean.toString(get_Is_IOS()));
            writer.endObject();
        } catch (IOException e) {
            Log.e(MY_TAG, e.toString());
            e.printStackTrace();
        }
    }
}
