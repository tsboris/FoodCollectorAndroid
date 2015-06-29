package DataModel;

import java.util.Date;

/**
 * Created by artyomshapet on 6/29/15.
 */
public class FCPublication {

    public static final String PUBLICATION_UNIQUE_ID_KEY = "id";
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
    public static final String PUBLICATION_IS_ON_AIR_KEY = "is_on_air";

    public static final String DID_REGISTER_FOR_CURRENT_PUBLICATION = "did_Register_for_current_publication";
    public static final String DID_MODIFY_COORDS = "did_modify_coords";
    public static final String REPORST_MESSAGE_ARRAY = "reportsMessageArray";
    public static final String PUBLICATION_COUNT_OF_REGISTER_USERS_KEY = "pulbicationCountOfRegisteredUsersKey";

    private int uniqueId;

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
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
        return title;
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

    public enum FCTypeOfCollecting {
        FreePickUp,//1
        ContactPublisher//2
    }

    public FCTypeOfCollecting typeOfCollecting;

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

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    private Date endingDate;

    public Date getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(Date endingDate) {
        this.endingDate = endingDate;
    }

    private String contactInfo; // make it nullable

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    private Boolean isOnAir;

    public Boolean getIsOnAir() {
        return isOnAir;
    }

    public void setIsOnAir(Boolean isOnAir) {
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

}