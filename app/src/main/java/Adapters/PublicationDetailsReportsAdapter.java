package Adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

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
    int v;

    public PublicationDetailsReportsAdapter(Context context, int resource, List<PublicationReport> objects,int v) {
        super(context, resource, objects);
        itemSourse = objects;
        this.context = context;
        this.v = v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(v, parent, false);
        } else {
            result = convertView;
        }
        //return super.getView(position, convertView, parent);
        ImageView iv_repImage = (ImageView)result.findViewById(R.id.iv_pub_det_report_icon);
        TextView tv_repText = (TextView)result.findViewById(R.id.tv_pub_det_report_text);
        TextView tv_repTime = (TextView)result.findViewById(R.id.tv_pub_det_report_time);

        // tmp switch, todo implement - need spec
        switch (itemSourse.get(position).getReport()){
            case 5:
                iv_repImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_few));
                break;
            case 3:
                iv_repImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_half));
                break;
            case 1:
            default:
                iv_repImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_whole));
                break;
        }

        String reportString = "";
        switch (itemSourse.get(position).getReport()){
            case 1:
                reportString = context.getString(R.string.report_dialog_collected_part_btn);
                break;
            case 3:
                reportString = context.getString(R.string.report_dialog_collected_all_btn);
                break;
            case 5:
                reportString = context.getString(R.string.report_dialog_found_nothing_btn);
                break;
        }

        tv_repText.setText(reportString);
        tv_repTime.setText((itemSourse.get(position).getDate_reported().getHours()<10?"0":"")
                + itemSourse.get(position).getDate_reported().getHours()
                + ":"
                + (itemSourse.get(position).getDate_reported().getMinutes()<10?"0":"")
                + itemSourse.get(position).getDate_reported().getMinutes());

        return result;
    }
}
