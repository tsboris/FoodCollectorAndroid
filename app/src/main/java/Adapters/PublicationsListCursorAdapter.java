package Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public PublicationsListCursorAdapter(Context context, Cursor c, int flags, LatLng mLocation) {
        super(context, c, flags);
        this.context = context;
        if(mLocation != null){
            myLocation = mLocation;
        }
/*
        if(context!=null)
            amazonBaseAddress = context.getResources().getString(R.string.amazon_base_url_for_images);
*/
    }

/*
    public void UpdateCurrentLocation(float lat, float lon){
        myLatitude = lat;
        myLongitude = lon;
    }
*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.my_fcpublication_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // images must be dinamically set to appropriate
        RoundedImageView publicationImage = (RoundedImageView)view.findViewById(R.id.iv_pub_list_item_img);
        ImageView publicationIcon = (ImageView)view.findViewById(R.id.iv_pub_list_item_icon);

        TextView publicationTitle = (TextView)view.findViewById(R.id.tv_pub_list_item_title);
        TextView publicationAddress = (TextView)view.findViewById(R.id.tv_pub_list_item_address);
        TextView publicationDistance = (TextView)view.findViewById(R.id.tv_pub_list_item_distance);

/*        String imageAmazonAddress
                = amazonBaseAddress + "/"
                + String.valueOf(cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY)))
                + "." + String.valueOf(cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_VERSION_KEY)))
                + ".jpg";
        //Log.i(MY_TAG, "loading image from " + imageAmazonAddress);

        try {
            Bitmap publicationImageDrawable = bitmap_from_url(imageAmazonAddress);
            publicationImage.setImageBitmap(publicationImageDrawable);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NetworkOnMainThreadException e){
            Log.e(MY_TAG, "no image found for pub" + cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_TITLE_KEY)));
        }
        if(publicationImage != null)
            drawableManager.fetchDrawableOnThread(imageAmazonAddress, publicationImage);
        */

        byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(FCPublication.PUBLICATION_IMAGE_BYTEARRAY_KEY));
        if(imageBytes != null && imageBytes.length > 0){
            Bitmap bImage = CommonUtil.decodeScaledBitmapFromByteArray(imageBytes, 100, 100);
            Drawable image = new BitmapDrawable(bImage);//BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length)
            publicationImage.setImageDrawable(image);
        } else {
            publicationImage.setImageDrawable(context.getResources().getDrawable(R.drawable.foodonet_logo_200_200));
        }

        // tmp switch, todo implement - need spec
        switch (cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY)) % 3){
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

        if(myLocation != null){
            float lat = cursor.getFloat(cursor.getColumnIndex(FCPublication.PUBLICATION_LATITUDE_KEY));
            float lon = cursor.getFloat(cursor.getColumnIndex(FCPublication.PUBLICATION_LONGITUDE_KEY));
            publicationDistance.setText(CommonUtil.GetDistanceString(new LatLng(lat, lon), myLocation, context));
        } else {
            publicationDistance.setText(context.getResources().getString(R.string.pub_det_cant_get_distance));
        }
    }

/*
    Bitmap bitmap_from_url(String url) throws
            java.net.MalformedURLException, java.io.IOException
    {
        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        return bitmap;
        return Drawable.createFromStream(((java.io.InputStream)
                new java.net.URL(url).getContent()), null);
    }
*/

/*
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.foodonet_logo_200_200);
                        imageView.setImageDrawable(placeholder);
                    }
                }
            }
        }
    }

    private Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            urlConnection.disconnect();
            Log.w("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
*/
}
