package DataModel;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by artyomshapet on 6/29/15.
 */
public class FCPublication implements Serializable, ICanWriteSelfToJSONWriter {

    public static final String PUBLICATION_UNIQUE_ID_KEY = "_id";
    public static final String PUBLICATION_PUBLISHER_UUID_KEY = "active_device_dev_uuid";
    public static final String PUBLICATION_UNIQUE_ID_KEY_JSON = "id";
    public static final String PUBLICATION_VERSION_KEY = "version";
    public static final String PUBLICATION_TITLE_KEY = "title";
    public static final String PUBLICATION_SUBTITLE_KEY = "subtitle";
    public static final String PUBLICATION_ADDRESS_KEY = "address";
    public static final String PUBLICATION_TYPE_OF_COLLECTION_KEY = "type_of_collecting";
    public static final String PUBLICATION_LATITUDE_KEY = "latitude";
    public static final String PUBLICATION_LONGITUDE_KEY = "longitude";
    public static final String PUBLICATION_STARTING_DATE_KEY = "starting_date";
    public static final String PUBLICATION_ENDING_DATE_KEY = "ending_date";
    public static final String PUBLICATION_CONTACT_INFO_KEY = "contact_info";
    public static final String PUBLICATION_PHOTO_URL = "photo_url";
    public static final String PUBLICATION_COUNT_OF_REGISTER_USERS_KEY = "pulbicationCountOfRegisteredUsersKey";
    public static final String PUBLICATION_IS_ON_AIR_KEY = "is_on_air";

    public static final String DID_REGISTER_FOR_CURRENT_PUBLICATION = "did_Register_for_current_publication";
    public static final String DID_MODIFY_COORDS = "did_modify_coords";
    public static final String REPORST_MESSAGE_ARRAY = "reportsMessageArray";

    public FCPublication() {
        countOfRegisteredUsers = 0;
    }

    public FCPublication(int id, String publisherUID, String title, String subtitle, String address, FCTypeOfCollecting typeOfCollecting, double latitude, double longitude, Date startingDate, Date endingDate, String contactInfo, String photoUrl, boolean isOnAir) {
        this();
        setUniqueId(id);
        setPublisherUID(publisherUID);
        setTitle(title);
        setSubtitle(subtitle);
        setAddress(address);
        setTypeOfCollecting(typeOfCollecting);
        setLatitude(latitude);
        setLongitude(longitude);
        setStartingDate(startingDate);
        setEndingDate(endingDate);
        setContactInfo(contactInfo);
        setPhotoUrl(photoUrl);
        setIsOnAir(isOnAir);
    }

    // Create a new Item from data packaged in an Intent
/*  public FCPublication(Intent intent) {

        title = intent.getStringExtra(PUBLICATION_TITLE_KEY);
        //photoUrl = Uri.parse( intent.getStringExtra(PUBLICATION_PHOTO_URL));
    }

    public static void packageIntent(Intent intent, String title, Uri photoUrl) {

        intent.putExtra(PUBLICATION_TITLE_KEY, title);
        intent.putExtra(PUBLICATION_PHOTO_URL, photoUrl);

    }
*/

    private int uniqueId;

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    private String publisherUID;

    public void setPublisherUID(String publisherUID) {
        this.publisherUID = publisherUID;
    }

    public String getPublisherUID() {
        return publisherUID;
    }

    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    private String title;

    public String getTitle() {
        return title == null ? "no title" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String subtitle;// make it nullable

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;

    private FCTypeOfCollecting typeOfCollecting;

    public int getTypeOfCollecting() {
        return typeOfCollecting.ordinal();
    }

    public void setTypeOfCollecting(int typeID) {
        typeOfCollecting = FCTypeOfCollecting.values()[typeID];
    }

    public void setTypeOfCollecting(FCTypeOfCollecting typeEnum) {
        typeOfCollecting = typeEnum;
    }

    private Double latitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    private Double longitude;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    private Date startingDate;

    public Date getStartingDate() {
        return startingDate;
    }

    public long getStartingDateUnixTime() {
        return startingDate.getTime();
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public void setStartingDate(long startingDate) {
        this.startingDate = new Date(startingDate * 1000);
    }

    private Date endingDate;

    public Date getEndingDate() {
        return endingDate;
    }

    public long getEndingDateUnixTime() {
        return endingDate.getTime();
    }

    public void setEndingDate(Date endingDate) {
        this.endingDate = endingDate;
    }

    public void setEndingDate(long endingDate) {
        this.endingDate = new Date(endingDate * 1000);
    }

    private String contactInfo; // make it nullable

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    private boolean isOnAir;

    public boolean getIsOnAir() {
        return isOnAir;
    }

    public void setIsOnAir(boolean isOnAir) {
        this.isOnAir = isOnAir;
    }


    private Boolean didModifyCoords;

    public Boolean getDidModifyCoords() {
        return didModifyCoords;
    }

    public void setDidModifyCoords(Boolean didModifyCoords) {
        this.didModifyCoords = didModifyCoords;
    }


    private String photoUrl;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Uri getPhotoUrlAsUri() {
        return Uri.parse(photoUrl);
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    private Double distanceFromUserLocation;

    public Double getDistanceFromUserLocation() {
        return distanceFromUserLocation;
    }


    private int countOfRegisteredUsers = 0;

    public int getCountOfRegisteredUsers() {
        return countOfRegisteredUsers;
    }

    public void setCountOfRegisteredUsers(int value) {
        countOfRegisteredUsers = value;
    }

    private ArrayList<RegisteredUserForPublication> registeredForThisPublication;

    public void setRegisteredForThisPublication(ArrayList<RegisteredUserForPublication> regedUsers){
        if(registeredForThisPublication == null)
            registeredForThisPublication = new ArrayList<>();
        registeredForThisPublication.addAll(regedUsers);
    }

    public ArrayList<RegisteredUserForPublication> getRegisteredForThisPublication(){
        if(registeredForThisPublication == null)
            registeredForThisPublication = new ArrayList<>();
        return registeredForThisPublication;
    }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        PUBLICATION_UNIQUE_ID_KEY,
                        PUBLICATION_PUBLISHER_UUID_KEY,
                        PUBLICATION_VERSION_KEY,
                        PUBLICATION_TITLE_KEY,
                        PUBLICATION_SUBTITLE_KEY,
                        PUBLICATION_ADDRESS_KEY,
                        PUBLICATION_TYPE_OF_COLLECTION_KEY,
                        PUBLICATION_LATITUDE_KEY,
                        PUBLICATION_LONGITUDE_KEY,
                        PUBLICATION_STARTING_DATE_KEY,
                        PUBLICATION_ENDING_DATE_KEY,
                        PUBLICATION_CONTACT_INFO_KEY,
                        PUBLICATION_PHOTO_URL,
                        PUBLICATION_COUNT_OF_REGISTER_USERS_KEY,
                        PUBLICATION_IS_ON_AIR_KEY,
                };
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        cv.put(PUBLICATION_UNIQUE_ID_KEY, getUniqueId());
        cv.put(PUBLICATION_PUBLISHER_UUID_KEY, getPublisherUID());
        cv.put(PUBLICATION_VERSION_KEY, getVersion());
        cv.put(PUBLICATION_TITLE_KEY, getTitle());
        cv.put(PUBLICATION_SUBTITLE_KEY, getSubtitle());
        cv.put(PUBLICATION_ADDRESS_KEY, getAddress());
        cv.put(PUBLICATION_TYPE_OF_COLLECTION_KEY, getTypeOfCollecting());
        cv.put(PUBLICATION_LATITUDE_KEY, getLatitude());
        cv.put(PUBLICATION_LONGITUDE_KEY, getLongitude());
        cv.put(PUBLICATION_STARTING_DATE_KEY, getStartingDateUnixTime());
        cv.put(PUBLICATION_ENDING_DATE_KEY, getEndingDateUnixTime());
        cv.put(PUBLICATION_CONTACT_INFO_KEY, getContactInfo());
        cv.put(PUBLICATION_PHOTO_URL, getPhotoUrl());
        cv.put(PUBLICATION_COUNT_OF_REGISTER_USERS_KEY, getCountOfRegisteredUsers());
        cv.put(PUBLICATION_IS_ON_AIR_KEY, getIsOnAir());
        return cv;
    }

    public static ArrayList<FCPublication> GetArrayListOfPublicationsFromCursor(Cursor cursor) {
        ArrayList<FCPublication> result = new ArrayList<FCPublication>();

        if (cursor.moveToFirst()) {
            do {
                FCPublication publication = new FCPublication();
                publication.setUniqueId(cursor.getInt(cursor.getColumnIndex(PUBLICATION_UNIQUE_ID_KEY)));
                publication.setPublisherUID(cursor.getString(cursor.getColumnIndex(PUBLICATION_PUBLISHER_UUID_KEY)));
                publication.setTitle(cursor.getString(cursor.getColumnIndex(PUBLICATION_TITLE_KEY)));
                publication.setSubtitle(cursor.getString(cursor.getColumnIndex(PUBLICATION_SUBTITLE_KEY)));
                publication.setAddress(cursor.getString(cursor.getColumnIndex(PUBLICATION_ADDRESS_KEY)));
                publication.setTypeOfCollecting(cursor.getInt(cursor.getColumnIndex(PUBLICATION_TYPE_OF_COLLECTION_KEY)));
                publication.setLatitude(cursor.getDouble(cursor.getColumnIndex(PUBLICATION_LATITUDE_KEY)));
                publication.setLongitude(cursor.getDouble(cursor.getColumnIndex(PUBLICATION_LONGITUDE_KEY)));
                publication.setStartingDate(cursor.getLong(cursor.getColumnIndex(PUBLICATION_STARTING_DATE_KEY)));
                publication.setEndingDate(cursor.getLong(cursor.getColumnIndex(PUBLICATION_ENDING_DATE_KEY)));
                publication.setContactInfo(cursor.getString(cursor.getColumnIndex(PUBLICATION_CONTACT_INFO_KEY)));
                publication.setPhotoUrl(cursor.getString(cursor.getColumnIndex(PUBLICATION_PHOTO_URL)));
                publication.setIsOnAir(cursor.getInt(cursor.getColumnIndex(PUBLICATION_IS_ON_AIR_KEY)) == 1);
                result.add(publication);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static ArrayList<FCPublication> GetArrayListOfPublicationsFromJSON(JSONArray ja) {
        ArrayList<FCPublication> result = new ArrayList<FCPublication>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                Log.i("mytag", ja.getJSONObject(i).toString());
                result.add(ParseSinglePublicationFromJSON(ja.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static FCPublication ParseSinglePublicationFromJSON(JSONObject jo) {
        if (jo == null) return null;
        FCPublication publication = new FCPublication();
        try {
            publication.setUniqueId(jo.getInt(PUBLICATION_UNIQUE_ID_KEY_JSON));
            publication.setPublisherUID(jo.getString(PUBLICATION_PUBLISHER_UUID_KEY));
            publication.setTitle(jo.getString(PUBLICATION_TITLE_KEY));
            publication.setSubtitle(jo.getString(PUBLICATION_SUBTITLE_KEY));
            publication.setVersion(jo.getInt(PUBLICATION_VERSION_KEY));
            publication.setAddress(jo.getString(PUBLICATION_ADDRESS_KEY));
            publication.setTypeOfCollecting(jo.getInt(PUBLICATION_TYPE_OF_COLLECTION_KEY) - 1);
            publication.setLatitude(jo.getDouble(PUBLICATION_LATITUDE_KEY));
            publication.setLongitude(jo.getDouble(PUBLICATION_LONGITUDE_KEY));
            publication.setStartingDate(jo.getLong(PUBLICATION_STARTING_DATE_KEY));
            publication.setEndingDate(jo.getLong(PUBLICATION_ENDING_DATE_KEY));
            publication.setContactInfo(jo.getString(PUBLICATION_CONTACT_INFO_KEY));
            publication.setPhotoUrl(jo.getString(PUBLICATION_PHOTO_URL));
            //publication.setCountOfRegisteredUsers(jo.getInt(PUBLICATION_COUNT_OF_REGISTER_USERS_KEY));
            publication.setIsOnAir(jo.getBoolean(PUBLICATION_IS_ON_AIR_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("mytag", e.getMessage());
            return null;
        }
        return publication;
    }

    public JSONObject GetJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PUBLICATION_UNIQUE_ID_KEY, getUniqueId());
            jsonObject.put(PUBLICATION_PUBLISHER_UUID_KEY, getPublisherUID());
            jsonObject.put(PUBLICATION_TITLE_KEY, getTitle());
            jsonObject.put(PUBLICATION_SUBTITLE_KEY, getSubtitle());
            jsonObject.put(PUBLICATION_VERSION_KEY, getVersion());
            jsonObject.put(PUBLICATION_ADDRESS_KEY, getAddress());
            jsonObject.put(PUBLICATION_TYPE_OF_COLLECTION_KEY, getTypeOfCollecting());
            jsonObject.put(PUBLICATION_LATITUDE_KEY, getLatitude());
            jsonObject.put(PUBLICATION_LONGITUDE_KEY, getLongitude());
            jsonObject.put(PUBLICATION_STARTING_DATE_KEY, getStartingDateUnixTime());
            jsonObject.put(PUBLICATION_ENDING_DATE_KEY, getEndingDateUnixTime());
            jsonObject.put(PUBLICATION_CONTACT_INFO_KEY, getContactInfo());
            jsonObject.put(PUBLICATION_PHOTO_URL, getPhotoUrl());
            jsonObject.put(PUBLICATION_COUNT_OF_REGISTER_USERS_KEY, getCountOfRegisteredUsers());
            jsonObject.put(PUBLICATION_IS_ON_AIR_KEY, getIsOnAir());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public void WriteSelfToJSONWriter(JsonWriter writer){
        try {
            writer.beginObject();
            writer.name(PUBLICATION_UNIQUE_ID_KEY_JSON).value(getUniqueId());
            writer.name(PUBLICATION_VERSION_KEY).value(getVersion());
            writer.name(PUBLICATION_TITLE_KEY).value(getTitle());
            writer.name(PUBLICATION_SUBTITLE_KEY).value(getSubtitle());
            writer.name(PUBLICATION_PUBLISHER_UUID_KEY).value(getPublisherUID());
            writer.name(PUBLICATION_ADDRESS_KEY).value(getAddress());
            writer.name(PUBLICATION_TYPE_OF_COLLECTION_KEY).value(getTypeOfCollecting());
            writer.name(PUBLICATION_LATITUDE_KEY).value(getLatitude());
            writer.name(PUBLICATION_LONGITUDE_KEY).value(getLongitude());
            writer.name(PUBLICATION_STARTING_DATE_KEY).value(getStartingDateUnixTime());
            writer.name(PUBLICATION_ENDING_DATE_KEY).value(getEndingDateUnixTime());
            writer.name(PUBLICATION_CONTACT_INFO_KEY).value(getContactInfo());
            writer.name(PUBLICATION_PHOTO_URL).value(getPhotoUrl());
            writer.name(PUBLICATION_COUNT_OF_REGISTER_USERS_KEY).value(getCountOfRegisteredUsers());
            writer.name(PUBLICATION_IS_ON_AIR_KEY).value(getIsOnAir());
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FCPublication GetPublicationFromArrayListByID(ArrayList<FCPublication> list, int id) {
        if (list == null || list.size() == 0)
            return null;
        for (FCPublication publication : list) {
            if (publication.getUniqueId() == id)
                return publication;
        }
        return null;
    }

    public static ArrayList<FCPublication> DeletePublicationFromCollectionByID(ArrayList<FCPublication> list, int id) {
        if (list == null || list.size() == 0)
            return null;
        FCPublication lookingFor = GetPublicationFromArrayListByID(list, id);
        if (lookingFor != null)
            list.remove(lookingFor);
        return list;
    }
/*

    PUBLICATION_UNIQUE_ID_KEY,
    PUBLICATION_VERSION_KEY,
    PUBLICATION_TITLE_KEY,
    PUBLICATION_SUBTITLE_KEY,
    PUBLICATION_ADDRESS_KEY,
    PUBLICATION_TYPE_OF_COLLECTION_KEY,
    PUBLICATION_LATITUDE_KEY,
    PUBLICATION_LONGITUDE_KEY,
    PUBLICATION_STARTING_DATE_KEY,
    PUBLICATION_ENDING_DATE_KEY,
    PUBLICATION_CONTACT_INFO_KEY,
    PUBLICATION_PHOTO_URL,
    PUBLICATION_COUNT_OF_REGISTER_USERS_KEY
*/

    //
//    public var  distanceFromUserLocation:Double {
//        let location = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
//        return location.distanceFromLocation(FCModel.sharedInstance.userLocation)
//    }
//    var reportsForPublication = [FCOnSpotPublicationReport]()
//
//    // True if the current user registered for this publication
//    var didRegisterForCurrentPublication:Bool = false {
//        didSet{
//            if didRegisterForCurrentPublication {
//                FCUserNotificationHandler.sharedInstance.removeLocationNotification(self)
//                FCUserNotificationHandler.sharedInstance.registerLocalNotification(self)
//            }
//            else if !didRegisterForCurrentPublication{
//                FCUserNotificationHandler.sharedInstance.removeLocationNotification(self)
//            }
//        }
//    }
//
//    //publication's registrations array holds only instances with a register message.
//    //when an unrigister push notification arrives the unregistered publication is taken out
//
//    var registrationsForPublication = [FCRegistrationForPublication]()
//
//    //the count of registered devices. is set in initial data download.
//    var countOfRegisteredUsers = 0


/*
    @Override
    public String toString() {
        return "id: " + getUniqueId() + " title: " + getTitle() + ";";
    }
*/
}


//package DataModel;
//
//import java.util.Date;
//
///**
// * Created by artyomshapet on 6/29/15.
// */
//public class FCPublication {
//
//    public static final String PUBLICATION_UNIQUE_ID_KEY = "id";
//    public static final String PUBLICATION_VERSION_KEY = "version";
//    public static final String PUBLICATION_TITLE_KEY = "title";
//    public static final String PUBLICATION_SUBTITLE_KEY = "subtitle";
//    public static final String PUBLICATION_ADDRESS_KEY = "address";
//    public static final String PUBLICATION_TYPE_OF_COLLECTION_KEY = "type_of_collecting";
//    public static final String PUBLICATION_LATITUDE_KEY = "latitude";
//    public static final String PUBLICATION_LONGITUDE_KEY = "longitude";
//    public static final String PUBLICATION_STARTING_DATE_KEY = "starting_date";
//    public static final String PUBLICATION_ENDING_DATE_KEY = "ending_date";
//    public static final String PUBLICATION_CONTACT_INFO_KEY = "contact_info";
//    public static final String PUBLICATION_PHOTO_URL = "photo_url";
//    public static final String PUBLICATION_IS_ON_AIR_KEY = "is_on_air";
//
//    public static final String DID_REGISTER_FOR_CURRENT_PUBLICATION = "did_Register_for_current_publication";
//    public static final String DID_MODIFY_COORDS = "did_modify_coords";
//    public static final String REPORST_MESSAGE_ARRAY = "reportsMessageArray";
//    public static final String PUBLICATION_COUNT_OF_REGISTER_USERS_KEY = "pulbicationCountOfRegisteredUsersKey";
//
//    private int uniqueId;
//
//    public int getUniqueId() {
//        return uniqueId;
//    }
//
//    public void setUniqueId(int uniqueId) {
//        this.uniqueId = uniqueId;
//    }
//
//    private int version;
//
//    public int getVersion() {
//        return version;
//    }
//
//    public void setVersion(int version) {
//        this.version = version;
//    }
//
//    private String title;
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    private String subtitle;// make it nullable
//
//    public String getSubtitle() {
//        return subtitle;
//    }
//
//    public void setSubtitle(String subtitle) {
//        this.subtitle = subtitle;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    private String address;
//
//    public enum FCTypeOfCollecting {
//        FreePickUp,//1
//        ContactPublisher//2
//    }
//
//    public FCTypeOfCollecting typeOfCollecting;
//
//    private Double latitude;
//
//    public Double getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(Double latitude) {
//        this.latitude = latitude;
//    }
//
//    private Double longitude;
//
//    public Double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(Double longitude) {
//        this.longitude = longitude;
//    }
//
//    private Date startingDate;
//
//    public Date getStartingDate() {
//        return startingDate;
//    }
//
//    public void setStartingDate(Date startingDate) {
//        this.startingDate = startingDate;
//    }
//
//    private Date endingDate;
//
//    public Date getEndingDate() {
//        return endingDate;
//    }
//
//    public void setEndingDate(Date endingDate) {
//        this.endingDate = endingDate;
//    }
//
//    private String contactInfo; // make it nullable
//
//    public String getContactInfo() {
//        return contactInfo;
//    }
//
//    public void setContactInfo(String contactInfo) {
//        this.contactInfo = contactInfo;
//    }
//
//    private Boolean isOnAir;
//
//    public Boolean getIsOnAir() {
//        return isOnAir;
//    }
//
//    public void setIsOnAir(Boolean isOnAir) {
//        this.isOnAir = isOnAir;
//    }
//
//    private Boolean didModifyCoords;
//
//    public Boolean getDidModifyCoords() {
//        return didModifyCoords;
//    }
//
//    public void setDidModifyCoords(Boolean didModifyCoords) {
//        this.didModifyCoords = didModifyCoords;
//    }
//
//
//    private String photoUrl;
//
//    public String getPhotoUrl() {
//        return photoUrl;
//    }
//
//    public void setPhotoUrl(String photoUrl) {
//        this.photoUrl = photoUrl;
//    }
//
//    private Double distanceFromUserLocation;
//
//    public Double getDistanceFromUserLocation() {
//        return distanceFromUserLocation;
//    }
//
//
//    private int countOfRegisteredUsers = 0;
//
//    public int getCountOfRegisteredUsers() {
//        return countOfRegisteredUsers;
//    }
//
//    //
////    public var  distanceFromUserLocation:Double {
////        let location = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
////        return location.distanceFromLocation(FCModel.sharedInstance.userLocation)
////    }
////    var reportsForPublication = [FCOnSpotPublicationReport]()
////
////    // True if the current user registered for this publication
////    var didRegisterForCurrentPublication:Bool = false {
////        didSet{
////            if didRegisterForCurrentPublication {
////                FCUserNotificationHandler.sharedInstance.removeLocationNotification(self)
////                FCUserNotificationHandler.sharedInstance.registerLocalNotification(self)
////            }
////            else if !didRegisterForCurrentPublication{
////                FCUserNotificationHandler.sharedInstance.removeLocationNotification(self)
////            }
////        }
////    }
////
////    //publication's registrations array holds only instances with a register message.
////    //when an unrigister push notification arrives the unregistered publication is taken out
////
////    var registrationsForPublication = [FCRegistrationForPublication]()
////
////    //the count of registered devices. is set in initial data download.
////    var countOfRegisteredUsers = 0
//
//}