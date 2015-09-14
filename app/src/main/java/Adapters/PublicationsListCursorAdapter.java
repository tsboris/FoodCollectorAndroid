package Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import DataModel.FCPublication;
import upp.foodonet.R;

/**
 * Created by Asher on 14.09.2015.
 */
public class PublicationsListCursorAdapter extends CursorAdapter {

    Context context;
    float myLatitude, myLongitude;

    public PublicationsListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
        UpdateCurrentLocation(32.11102827f, 34.85003149f);
    }

    public void UpdateCurrentLocation(float lat, float lon){
        myLatitude = lat;
        myLongitude = lon;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.my_fcpublication_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // images must be dinamically set to appropriate
        ImageView publicationImage = (ImageView)view.findViewById(R.id.iv_pub_list_item_img);
        ImageView publicationIcon = (ImageView)view.findViewById(R.id.iv_pub_list_item_icon);

        TextView publicationTitle = (TextView)view.findViewById(R.id.tv_pub_list_item_title);
        TextView publicationAddress = (TextView)view.findViewById(R.id.tv_pub_list_item_address);
        TextView publicationDistance = (TextView)view.findViewById(R.id.tv_pub_list_item_distance);

        String title = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_TITLE_KEY));
        publicationTitle.setText(title);
        String address = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_ADDRESS_KEY));
        publicationAddress.setText(address);

        float lat = cursor.getFloat(cursor.getColumnIndex(FCPublication.PUBLICATION_LATITUDE_KEY));
        float lon = cursor.getFloat(cursor.getColumnIndex(FCPublication.PUBLICATION_LONGITUDE_KEY));
        float latDelta = lat - myLatitude;
        float lonDelta = lon - myLongitude;
        latDelta *= latDelta;
        lonDelta *= lonDelta;
        double distanceInDegrees = Math.sqrt(latDelta + lonDelta);
        double distanceInKilometers = Math.round(distanceInDegrees / 0.09);
        int distanceInKilometersRounded = (int)distanceInKilometers;
        publicationDistance.setText(String.valueOf(distanceInKilometersRounded));
    }
}
