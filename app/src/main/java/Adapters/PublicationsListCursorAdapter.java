package Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.util.IOUtils;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import UIUtil.RoundedImageView;
import upp.foodonet.R;

/**
 * Created by Asher on 14.09.2015.
 */
public class PublicationsListCursorAdapter extends CursorAdapter {

    private static final String MY_TAG = "food_adapterForList";

    Context context;
    //float myLatitude, myLongitude;
    String amazonBaseAddress;
    LatLng myLocation;
    boolean isForMyPublicationFlag;

    private Map<Integer, Drawable> imageDictionary;

    public PublicationsListCursorAdapter(Context context, Cursor c, int flags, LatLng mLocation, boolean isForMyPublications) {
        super(context, c, flags);
        this.context = context;
        if (mLocation != null) {
            Log.i(MY_TAG, "PublicationsListCursorAdapter got location: "
                    + mLocation.latitude + "-" + mLocation.longitude);
            myLocation = mLocation;
        }
        imageDictionary = new HashMap<>();
        isForMyPublicationFlag = isForMyPublications;
    }

    public void SetMyLocation(LatLng myLocation) {
        this.myLocation = myLocation;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(isForMyPublicationFlag
                        ? R.layout.my_fcpublication_item
                        : R.layout.others_fcpublication_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (isForMyPublicationFlag)
            bindViewForMyPublication(view, cursor);
        else
            bindViewForOthersPublication(view, cursor);
    }

    public void ClearImagesDictionary() {
        imageDictionary.clear();
    }

    private void bindViewForMyPublication(View view, Cursor cursor) {
        RoundedImageView publicationImage = (RoundedImageView) view.findViewById(R.id.iv_my_pub_list_item_img);
        View dotIsActive = (View) view.findViewById(R.id.v_dot_my_pubs_list);
        TextView publicationTitle = (TextView) view.findViewById(R.id.tv_my_pub_list_item_title);
        TextView publicationEndDate = (TextView) view.findViewById(R.id.tv_end_date_my_pubs_list);
        TextView publicationWarning = (TextView) view.findViewById(R.id.tv_warning_my_pub_list_item);
        publicationWarning.setVisibility(View.GONE);

        String title = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_TITLE_KEY));
        publicationTitle.setText(title);
        String endDateBase = context.getString(R.string.my_pubs_list_end_date_base_string);
        long endDateLong = 0;
        if(cursor.getColumnIndex(FCPublication.PUBLICATION_ENDING_DATE_KEY) != -1)
            endDateLong = cursor.getLong(cursor.getColumnIndex(FCPublication.PUBLICATION_ENDING_DATE_KEY));
        else Log.i(MY_TAG, "check if unneeded column " + FCPublication.PUBLICATION_LATITUDE_KEY + "present: " + cursor.getColumnIndex(FCPublication.PUBLICATION_LATITUDE_KEY));
        Calendar endDateCalendar = Calendar.getInstance();
        endDateCalendar.setTime(new Date(endDateLong * 1000));
        endDateBase = endDateBase.replace("{0}", CommonUtil.GetDateTimeStringFromCalendar(endDateCalendar));
        publicationEndDate.setText(endDateBase);

        SetPublicationImage(cursor, publicationImage);

        boolean isActive = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_IS_ON_AIR_KEY)) == 1
                && cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY)) > 0;

        if(isActive)
            dotIsActive.setBackground(context.getResources().getDrawable(R.drawable.green_rounded_dot_my_pubs));
        else
            dotIsActive.setBackground(context.getResources().getDrawable(R.drawable.red_rounded_dot_my_pubs));

        if(cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY)) < 0){
            publicationWarning.setVisibility(View.VISIBLE);
            publicationWarning.setText(context.getString(R.string.progress_saving_on_server));
        }

//        int isActiveInt = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_IS_ON_AIR_KEY));
//        switch (isActiveInt) {
//            case 1:
//                dotIsActive.setBackground(context.getResources().getDrawable(R.drawable.green_rounded_dot_my_pubs));
//                break;
//            default:
//                dotIsActive.setBackground(context.getResources().getDrawable(R.drawable.red_rounded_dot_my_pubs));
//                break;
//        }
    }

    private void bindViewForOthersPublication(View view, Cursor cursor) {
        RoundedImageView publicationImage = (RoundedImageView) view.findViewById(R.id.iv_pub_list_item_img);
        ImageView publicationIcon = (ImageView) view.findViewById(R.id.iv_pub_list_item_icon);

        TextView publicationTitle = (TextView) view.findViewById(R.id.tv_pub_list_item_title);
        TextView publicationAddress = (TextView) view.findViewById(R.id.tv_pub_list_item_address);
        TextView publicationDistance = (TextView) view.findViewById(R.id.tv_pub_list_item_distance);

        SetPublicationImage(cursor, publicationImage);

        // tmp switch, todo implement - need spec
        switch (cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY)) % 3) {
            case 0:
                publicationIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_few));
                break;
            case 1:
                publicationIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_half));
                break;
            case 2:
            default:
                publicationIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_whole));
                break;
        }

        String title = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_TITLE_KEY));
        publicationTitle.setText(title);
        String address = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_ADDRESS_KEY));
        publicationAddress.setText(address);

        if (myLocation != null) {
            float lat = cursor.getFloat(cursor.getColumnIndex(FCPublication.PUBLICATION_LATITUDE_KEY));
            float lon = cursor.getFloat(cursor.getColumnIndex(FCPublication.PUBLICATION_LONGITUDE_KEY));
            publicationDistance.setText(CommonUtil.GetDistanceString(new LatLng(lat, lon), myLocation, context));
        } else {
            publicationDistance.setText(context.getResources().getString(R.string.pub_det_cant_get_distance));
        }
    }

    private void SetPublicationImage(Cursor cursor, ImageView publicationImage) {
        int imageSize = isForMyPublicationFlag
                ? context.getResources().getInteger(R.integer.pub_list_item_image_size_my)
                : context.getResources().getInteger(R.integer.pub_list_item_image_size_others);
        final int id = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY));
        final int version = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_VERSION_KEY));
        Drawable imageDrawable;
        if (imageDictionary.containsKey(id))
            publicationImage.setImageDrawable(imageDictionary.get(id));
        else {
            imageDrawable = CommonUtil.GetImageFromFileForPublicationCursor(context, cursor, imageSize);
/*
            if (id <= 0) {
                Log.i(MY_TAG, "negative id");
                imageDrawable = CommonUtil.GetBitmapDrawableFromFile("n" + (id * -1) + "." + version + ".jpg",
                        context.getString(R.string.image_folder_path), imageSize, imageSize);
            } else
                imageDrawable = CommonUtil.GetBitmapDrawableFromFile(id + "." + version + ".jpg",
                        context.getString(R.string.image_folder_path), imageSize, imageSize);
            if(imageDrawable == null && cursor.getColumnIndex(FCPublication.PUBLICATION_PHOTO_URL) != -1){
                String imagePath = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_PHOTO_URL));
                imageDrawable = CommonUtil.GetBitmapDrawableFromFile(imagePath, "", imageSize, imageSize);
            }
*/
            if (imageDrawable != null) {
                publicationImage.setImageDrawable(imageDrawable);
                imageDictionary.put(id, imageDrawable);
            } else
                publicationImage.setImageDrawable(context.getResources().getDrawable(R.drawable.foodonet_logo_200_200));
        }
    }
}
