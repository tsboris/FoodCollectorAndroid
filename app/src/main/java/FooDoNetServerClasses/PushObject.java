package FooDoNetServerClasses;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Asher on 28.10.2015.
 */
public class PushObject {

    public static final String PUSH_OBJECT_KEY_TYPE = "type";
    public static final String PUSH_OBJECT_KEY_DATA = "data";
    public static final String PUSH_OBJECT_KEY_ID = "id";
    public static final String PUSH_OBJECT_KEY_PUBLICATION_ID = "publication_id";
    public static final String PUSH_OBJECT_KEY_PUBLICATION_VERSION = "publication_version";
    public static final String PUSH_OBJECT_KEY_DATE_OF_REPORT = "date_of_report";

    public static final String PUSH_OBJECT_VALUE_NEW = "new_publication";
    public static final String PUSH_OBJECT_VALUE_DELETE = "deleted_publication";
    public static final String PUSH_OBJECT_VALUE_REPORT = "publication_report";
    public static final String PUSH_OBJECT_VALUE_REG = "registeration_for_publication";

    public String PushObjectType;
    public int ID;
    public int PublicationID;
    public int PublicationVersion;
    public Date DateOfReport;
    public int Report;

    public static PushObject DecodePushObject(Bundle data) {
        //try {
        PushObject result = new PushObject();

        try {

            //JSONObject jo = new JSONObject(jsonString);
            result.PushObjectType = data.getString(PUSH_OBJECT_KEY_TYPE);
            //JSONObject joInner = jo.getJSONObject(PUSH_OBJECT_KEY_DATA);
            switch (result.PushObjectType) {
                case PUSH_OBJECT_VALUE_NEW:
                case PUSH_OBJECT_VALUE_DELETE:
                case PUSH_OBJECT_VALUE_REG:
                    result.ID = data.getInt(PUSH_OBJECT_KEY_ID);
                    break;
                case PUSH_OBJECT_VALUE_REPORT:
                    result.PublicationID = data.getInt(PUSH_OBJECT_KEY_PUBLICATION_ID);
                    result.PublicationVersion = data.getInt(PUSH_OBJECT_KEY_PUBLICATION_VERSION);
                    result.DateOfReport = new Date(data.getLong(PUSH_OBJECT_KEY_DATE_OF_REPORT));
                    result.Report = data.getInt(PUSH_OBJECT_KEY_DATE_OF_REPORT);
                    break;

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return result;
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
    }
}
