package FooDoNetServerClasses;


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

    public static PushObject DecodePushObject(String jsonString){
        try {
            PushObject result = new PushObject();
            JSONObject jo = new JSONObject(jsonString);
            result.PushObjectType = jo.getString(PUSH_OBJECT_KEY_TYPE);
            JSONObject joInner = jo.getJSONObject(PUSH_OBJECT_KEY_DATA);
            switch (result.PushObjectType){
                case PUSH_OBJECT_VALUE_NEW:
                case PUSH_OBJECT_VALUE_DELETE:
                case PUSH_OBJECT_VALUE_REG:
                    result.ID = joInner.getInt(PUSH_OBJECT_KEY_ID);
                    break;
                case PUSH_OBJECT_VALUE_REPORT:
                    result.PublicationID = joInner.getInt(PUSH_OBJECT_KEY_PUBLICATION_ID);
                    result.PublicationVersion = joInner.getInt(PUSH_OBJECT_KEY_PUBLICATION_VERSION);
                    result.DateOfReport = new Date(joInner.getLong(PUSH_OBJECT_KEY_DATE_OF_REPORT));
                    result.Report = joInner.getInt(PUSH_OBJECT_KEY_DATE_OF_REPORT);
                    break;
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
