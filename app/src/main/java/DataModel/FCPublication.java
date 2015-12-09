package DataModel;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import upp.foodonet.PublicationDetailsActivity;

/**
 * Created by artyomshapet on 6/29/15.
 */
public class FCPublication implements Serializable, ICanWriteSelfToJSONWriter {

    private static final String MY_TAG = "food_fcPublication";

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

    public static final String PUBLICATION_TRIED_TO_LOAD_IMAGE = "tried_load_image";

    //public static final String PUBLICATION_IMAGE_BYTEARRAY_KEY = "image_bytes";

    public static final String PUBLICATION_JSON_ITEM_KEY = "publication";

    public static final String PUBLICATION_NUMBER_OF_REGISTERED = "num_of_regs";
    public static final String PUBLICATION_NEW_NEGATIVE_ID = "new_neg_id";

    public static final String DID_REGISTER_FOR_CURRENT_PUBLICATION = "did_Register_for_current_publication";
    public static final String DID_MODIFY_COORDS = "did_modify_coords";
    public static final String REPORST_MESSAGE_ARRAY = "reportsMessageArray";

    public FCPublication() {
        setVersion(0);
        setCountOfRegisteredUsers(0);
        setIfTriedToGetPictureBefore(false);
        setTypeOfCollecting(FCTypeOfCollecting.FreePickUp);
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

    public FCPublication(FCPublication publication){
        this();
        if(publication == null) return;
        setUniqueId(publication.getUniqueId());
        setVersion(publication.getVersion());
        setTitle(publication.getTitle());
        setSubtitle(publication.getSubtitle());
        setPublisherUID(publication.getPublisherUID());
        setAddress(publication.getAddress());
        setStartingDate(publication.getStartingDate());
        setEndingDate(publication.getEndingDate());
        setTypeOfCollecting(publication.getTypeOfCollecting());
        setLatitude(publication.getLatitude());
        setLongitude(publication.getLongitude());
        setContactInfo(publication.getContactInfo());
        setIsOnAir(publication.getIsOnAir());
        setPhotoUrl(publication.getPhotoUrl());
    }

    public boolean IsEqualTo(FCPublication otherPublication){
        if(getUniqueId() != otherPublication.getUniqueId()) return false;
        if(getVersion() != otherPublication.getVersion()) return false;
        if(CheckIfStringsDiffer(getTitle(), otherPublication.getTitle())) return false;
        if(CheckIfStringsDiffer(getSubtitle(), otherPublication.getSubtitle())) return false;
        if(CheckIfStringsDiffer(getPublisherUID(), otherPublication.getPublisherUID()))return false;
        if(CheckIfStringsDiffer(getAddress(), otherPublication.getAddress())) return false;
        if(getStartingDateUnixTime() != otherPublication.getStartingDateUnixTime()) return false;
        if (getEndingDateUnixTime() != otherPublication.getEndingDateUnixTime()) return false;
        if(getTypeOfCollecting() != otherPublication.getTypeOfCollecting()) return false;
        if(getLatitude() != otherPublication.getLatitude()) return false;
        if(getLongitude() != otherPublication.getLongitude()) return false;
        if(CheckIfStringsDiffer(getContactInfo(), otherPublication.getContactInfo())) return false;
        if(getIsOnAir() != otherPublication.getIsOnAir()) return false;
        if(CheckIfStringsDiffer(getPhotoUrl(), otherPublication.getPhotoUrl())) return false;
        if(!TextUtils.isEmpty(otherPublication.getPhotoUrl())) return false;
        return true;
    }

    private boolean CheckIfStringsDiffer(String string1, String string2){
        if(string1 == null && string2 != null) return true;
        if(string1 != null && string2 == null) return true;
        if(string1 == null && string2 == null) return false;
        return (string1.compareTo(string2) != 0);
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

    private int newIdFromServer;

    public void setNewIdFromServer(int id){
        this.newIdFromServer = id;
    }

    public int getNewIdFromServer(){
        return newIdFromServer;
    }

    private String publisherUID;

    public void setPublisherUID(String publisherUID) {
        this.publisherUID = publisherUID;
    }

    public String getPublisherUID() {
        return publisherUID;
    }

    public boolean isOwnPublication;

    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    private int versionFromServer;

    public int getVersionFromServer(){ return versionFromServer; }

    public void setVersionFromServer(int ver){ versionFromServer = ver; }

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
        return startingDate.getTime()/1000;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public void setStartingDateUnixTime(long startingDate) {
        this.startingDate = new Date(startingDate * 1000);
    }

    private Date endingDate;

    public Date getEndingDate() {
        return endingDate;
    }

    public long getEndingDateUnixTime() {
        return endingDate.getTime()/1000;
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

    private int numberOfRegistered;

    public int getNumberOfRegistered(){
        return numberOfRegistered;
    }

    public void setNumberOfRegistered(int num){
        numberOfRegistered = num;
    }

    private int countOfRegisteredUsers = 0;

    public int getCountOfRegisteredUsers() {
        return countOfRegisteredUsers;
    }

    public void setCountOfRegisteredUsers(int value) {
        countOfRegisteredUsers = value;
    }

    private boolean ifTriedToGetPictureBefore;

    public boolean getIfTriedToGetPictureBefore() { return ifTriedToGetPictureBefore; }

    public void setIfTriedToGetPictureBefore(boolean value) { ifTriedToGetPictureBefore = value; }

    public void setIfTriedToGetPictureBefore(int value) { ifTriedToGetPictureBefore = value != 0; }

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

    private ArrayList<PublicationReport> publicationReports;

    public void setPublicationReports(ArrayList<PublicationReport> reports){
        if(publicationReports == null)
            publicationReports = new ArrayList<>();
        publicationReports.addAll(reports);
    }

    public ArrayList<PublicationReport> getPublicationReports(){
        if(publicationReports == null)
            publicationReports = new ArrayList<>();
        return publicationReports;
    }

    public boolean pictureWasChangedDuringEditing = false;

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
                        PUBLICATION_TRIED_TO_LOAD_IMAGE
                        //PUBLICATION_IMAGE_BYTEARRAY_KEY
                };
    }

    public static String[] GetColumnNamesForListArray() {
        return new String[]{
                PUBLICATION_UNIQUE_ID_KEY,
                PUBLICATION_VERSION_KEY,
                PUBLICATION_TITLE_KEY,
                PUBLICATION_LATITUDE_KEY,
                PUBLICATION_LONGITUDE_KEY,
                PUBLICATION_ADDRESS_KEY,
                PUBLICATION_ENDING_DATE_KEY,
                PUBLICATION_IS_ON_AIR_KEY,
                PUBLICATION_NUMBER_OF_REGISTERED,
                PUBLICATION_PHOTO_URL
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
        cv.put(PUBLICATION_TRIED_TO_LOAD_IMAGE, getIfTriedToGetPictureBefore());
        //cv.put(PUBLICATION_IMAGE_BYTEARRAY_KEY, getImageByteArray());
        return cv;
    }

    public static ArrayList<FCPublication> GetArrayListOfPublicationsFromCursor(Cursor cursor, boolean isForList) {
        ArrayList<FCPublication> result = new ArrayList<FCPublication>();
        if(cursor != null && cursor.moveToFirst()) {
            do {
                FCPublication publication = new FCPublication();
                publication.setUniqueId(cursor.getInt(cursor.getColumnIndex(PUBLICATION_UNIQUE_ID_KEY)));
                publication.setTitle(cursor.getString(cursor.getColumnIndex(PUBLICATION_TITLE_KEY)));
                publication.setAddress(cursor.getString(cursor.getColumnIndex(PUBLICATION_ADDRESS_KEY)));
                publication.setLatitude(cursor.getDouble(cursor.getColumnIndex(PUBLICATION_LATITUDE_KEY)));
                publication.setLongitude(cursor.getDouble(cursor.getColumnIndex(PUBLICATION_LONGITUDE_KEY)));
                if(isForList){
                    publication.setNumberOfRegistered(cursor.getInt(cursor.getColumnIndex(PUBLICATION_NUMBER_OF_REGISTERED)));
                } else {
                    publication.setPhotoUrl(cursor.getString(cursor.getColumnIndex(PUBLICATION_PHOTO_URL)));
                    publication.setVersion(cursor.getInt(cursor.getColumnIndex(PUBLICATION_VERSION_KEY)));
                    publication.setPublisherUID(cursor.getString(cursor.getColumnIndex(PUBLICATION_PUBLISHER_UUID_KEY)));
                    publication.setSubtitle(cursor.getString(cursor.getColumnIndex(PUBLICATION_SUBTITLE_KEY)));
                    publication.setTypeOfCollecting(cursor.getInt(cursor.getColumnIndex(PUBLICATION_TYPE_OF_COLLECTION_KEY)));
                    publication.setStartingDateUnixTime(cursor.getLong(cursor.getColumnIndex(PUBLICATION_STARTING_DATE_KEY)));
                    publication.setEndingDate(cursor.getLong(cursor.getColumnIndex(PUBLICATION_ENDING_DATE_KEY)));
                    publication.setContactInfo(cursor.getString(cursor.getColumnIndex(PUBLICATION_CONTACT_INFO_KEY)));
                    publication.setIsOnAir(cursor.getInt(cursor.getColumnIndex(PUBLICATION_IS_ON_AIR_KEY)) == 1);
                    publication.setIfTriedToGetPictureBefore(cursor.getInt(cursor.getColumnIndex(PUBLICATION_TRIED_TO_LOAD_IMAGE)));
                }
                result.add(publication);
            } while (cursor.moveToNext());
        }
        //cursor.close();
        return result;
    }

    public static ArrayList<FCPublication> GetArrayListOfPublicationsForMapFromCursor(Cursor cursor) {
        ArrayList<FCPublication> result = new ArrayList<FCPublication>();
        if(cursor != null && cursor.moveToFirst()) {
            do {
                FCPublication publication = new FCPublication();
                publication.setUniqueId(cursor.getInt(cursor.getColumnIndex(PUBLICATION_UNIQUE_ID_KEY)));
                publication.setVersion(cursor.getInt(cursor.getColumnIndex(PUBLICATION_VERSION_KEY)));
                publication.setTitle(cursor.getString(cursor.getColumnIndex(PUBLICATION_TITLE_KEY)));
                publication.setLatitude(cursor.getDouble(cursor.getColumnIndex(PUBLICATION_LATITUDE_KEY)));
                publication.setLongitude(cursor.getDouble(cursor.getColumnIndex(PUBLICATION_LONGITUDE_KEY)));
                publication.setNumberOfRegistered(cursor.getInt(cursor.getColumnIndex(PUBLICATION_NUMBER_OF_REGISTERED)));
                result.add(publication);
            } while (cursor.moveToNext());
        }
        return result;
    }

    public static ArrayList<FCPublication> GetArrayListOfPublicationsFromJSON(JSONArray ja) {
        ArrayList<FCPublication> result = new ArrayList<FCPublication>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                //Log.i(MY_TAG, ja.getJSONObject(i).toString());
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
            publication.setStartingDateUnixTime(jo.getLong(PUBLICATION_STARTING_DATE_KEY));
            publication.setEndingDate(jo.getLong(PUBLICATION_ENDING_DATE_KEY));
            publication.setContactInfo(jo.getString(PUBLICATION_CONTACT_INFO_KEY));
            publication.setPhotoUrl(jo.getString(PUBLICATION_PHOTO_URL));
            //publication.setCountOfRegisteredUsers(jo.getInt(PUBLICATION_COUNT_OF_REGISTER_USERS_KEY));
            publication.setIsOnAir(jo.getBoolean(PUBLICATION_IS_ON_AIR_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return publication;
    }

    public static AbstractMap.SimpleEntry<Integer,Integer> ParseServerResponseToNewPublication(JSONObject jo){
        if(jo == null) return null;
        int idValue, versionValue;
        try {
            idValue = jo.getInt(PUBLICATION_UNIQUE_ID_KEY_JSON);
            versionValue = jo.getInt(PUBLICATION_VERSION_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return new AbstractMap.SimpleEntry<Integer, Integer>(idValue, versionValue);
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

    @Override
    public Map<String, Object> GetJsonMapStringObject() {
        return null;
    }

    @Override
    public org.json.simple.JSONObject GetJsonObjectForPost() {
        Map<String, Object> publicationData = new HashMap<String, Object>();
        publicationData.put(PUBLICATION_PUBLISHER_UUID_KEY, getPublisherUID());
        publicationData.put(PUBLICATION_TITLE_KEY, getTitle());
        publicationData.put(PUBLICATION_SUBTITLE_KEY, TextUtils.isEmpty(getSubtitle())? "": getSubtitle());
        publicationData.put(PUBLICATION_ADDRESS_KEY, getAddress());
        publicationData.put(PUBLICATION_LATITUDE_KEY, getLatitude());
        publicationData.put(PUBLICATION_LONGITUDE_KEY, getLongitude());
        publicationData.put(PUBLICATION_STARTING_DATE_KEY, getStartingDateUnixTime());
        publicationData.put(PUBLICATION_ENDING_DATE_KEY, getEndingDateUnixTime());
        publicationData.put(PUBLICATION_TYPE_OF_COLLECTION_KEY, getTypeOfCollecting() + 1);
        publicationData.put(PUBLICATION_CONTACT_INFO_KEY, TextUtils.isEmpty(getContactInfo())? "":getContactInfo());
        publicationData.put(PUBLICATION_PHOTO_URL, "");//getPhotoUrl());
        publicationData.put(PUBLICATION_IS_ON_AIR_KEY, true);//getIsOnAir());
        //publicationData.put(PUBLICATION_UNIQUE_ID_KEY, 0);
        //publicationData.put(PUBLICATION_VERSION_KEY, getVersion());
        //publicationData.put(PUBLICATION_COUNT_OF_REGISTER_USERS_KEY, getCountOfRegisteredUsers());


        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put(PUBLICATION_JSON_ITEM_KEY, publicationData);
        org.json.simple.JSONObject json = new org.json.simple.JSONObject();
        json.putAll(dataToSend);
        return json;

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


    //region images

    private byte[] ImageByteArray;

    public void setImageByteArray(byte[] bytes){
        ImageByteArray = bytes;
    }

    public byte[] getImageByteArray(){
        return ImageByteArray;
    }

    public void setImageByteArrayFromBitmap(Bitmap bitmap){
        setImageByteArray(BitmapToBytes(bitmap));
    }

    private byte[] BitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //todo: error null pointer could be thrown here, check if reproducable
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public String GetImageFileName(){
        return String.valueOf(getUniqueId()) + "." + String.valueOf(getVersion()) + ".jpg";
    }
    //endregion
}

