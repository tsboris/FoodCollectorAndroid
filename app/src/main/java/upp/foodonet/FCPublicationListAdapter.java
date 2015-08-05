package upp.foodonet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import DataModel.FCPublication;

/**
 * Created by artyomshapet on 7/18/15.
 */
class FCPublicationListAdapter extends BaseAdapter {

    public final List<FCPublication> mItems = new ArrayList<FCPublication>();
    private final Context mContext;

    public FCPublicationListAdapter(Context context) {

        mContext = context;
    }

    // Add a MyFCPublicationItem to the adapter
    // Notify observers that the data set has changed
    public void add(FCPublication item) {

        mItems.add(item);
        notifyDataSetChanged();

    }

    // Clears the list adapter of all items.
    public void clear() {

        mItems.clear();
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final FCPublication newItem = (FCPublication)getItem(position);


        if( convertView == null ){
            //We must create a View:
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_fcpublication_item, null);
        }


        TextView name = (TextView) convertView.findViewById(R.id.nameView);
        name.setText(newItem.getTitle());


        name.setTextColor(Color.WHITE);
        //name.setText(items.get(position));
        name.setBackgroundColor(Color.RED);
        int color = Color.argb( 200, 255, 64, 64 );
        name.setBackgroundColor( color );


        ImageView imgViewItem = (ImageView) convertView.findViewById(R.id.imageView);
        imgViewItem.setImageURI(newItem.getPhotoUrlAsUri());



        return convertView;
    }
}
