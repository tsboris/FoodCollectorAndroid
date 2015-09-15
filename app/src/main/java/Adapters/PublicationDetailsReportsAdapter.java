package Adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import DataModel.PublicationReport;
import upp.foodonet.R;

/**
 * Created by Asher on 16.09.2015.
 */
public class PublicationDetailsReportsAdapter extends ArrayAdapter<PublicationReport> {
    List<PublicationReport> itemSourse;
    Context context;

    public PublicationDetailsReportsAdapter(Context context, int resource, List<PublicationReport> objects) {
        super(context, resource, objects);
        itemSourse = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        ImageView iv_repImage = (ImageView)convertView.findViewById(R.id.iv_pub_det_report_icon);
        TextView tv_repText = (TextView)convertView.findViewById(R.id.tv_pub_det_report_text);
        TextView tv_repTime = (TextView)convertView.findViewById(R.id.tv_pub_det_report_time);

        // tmp switch, todo implement - need spec
        switch (itemSourse.get(position).getId() % 3){
            case 0:
                iv_repImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_few));
                break;
            case 1:
                iv_repImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_half));
                break;
            case 2:
            default:
                iv_repImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_whole));
                break;
        }

        tv_repText.setText(itemSourse.get(position).getReport());
        tv_repTime.setText(itemSourse.get(position).getDate_reported().getHours()
                + ":" + itemSourse.get(position).getDate_reported().getMinutes());

        return convertView;
    }
}
