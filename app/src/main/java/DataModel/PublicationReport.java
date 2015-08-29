package DataModel;

import android.util.JsonWriter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Asher on 30.08.2015.
 */
public class PublicationReport implements Serializable, ICanWriteSelfToJSONWriter {

    public static final String PUBLICATION_REPORT_FIELD_KEY_ID = "_id";
    public static final String PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID = "publication_unique_id";
    public static final String PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION = "publication_version";
    public static final String PUBLICATION_REPORT_FIELD_KEY_REPORT = "report";
    public static final String PUBLICATION_REPORT_FIELD_KEY_DATE = "date_of_report";
    public static final String PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID = "reporting_device_uuid";

    public PublicationReport(int id, int pub_id, int pub_version, String report, Date date, String dev_UID){
        setId(id);
        setPublication_id(pub_id);
        setPublication_version(pub_version);
        setReport(report);
        setDate_reported(date);
        setDevice_uuid(dev_UID);
    }

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

    private Date date_reported;
    public Date getDate_reported(){return date_reported;}
    public long getDate_reported_unix_time(){return date_reported.getTime();}
    public void setDate_reported(Date val){
        date_reported = val;
    }
    public void setDate_reported(long val){
        date_reported = new Date(val * 1000);
    }

    private String device_uuid;
    public String getDevice_uuid(){
        return device_uuid;
    }
    public void setDevice_uuid(String val){
        device_uuid = val;
    }

    private String report;
    public String getReport(){
        return report;
    }
    public void setReport(String val){
        report = val;
    }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        PUBLICATION_REPORT_FIELD_KEY_ID,
                        PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID,
                        PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION,
                        PUBLICATION_REPORT_FIELD_KEY_DATE,
                        PUBLICATION_REPORT_FIELD_KEY_REPORT,
                        PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID
                };
    }

    //todo: continue

    @Override
    public void WriteSelfToJSONWriter(JsonWriter writer) {

    }
}
