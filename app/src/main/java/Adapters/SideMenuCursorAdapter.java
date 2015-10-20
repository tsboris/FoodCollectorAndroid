package Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import UIUtil.RoundedImageView;
import upp.foodonet.R;

/**
 * Created by Asher on 13.09.2015.
 */
public class SideMenuCursorAdapter extends CursorAdapter {

    Context context;

    public SideMenuCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
        this.context = context;
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.side_menu_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tv_title = (TextView) view.findViewById(R.id.tv_side_menu_item_title);
        TextView tv_score = (TextView) view.findViewById(R.id.tv_side_menu_item_score);

        final RoundedImageView riv_image = (RoundedImageView) view.findViewById(R.id.riv_side_menu_pub_icon);

        // Extract properties from cursor
        String title = cursor.getString(cursor.getColumnIndexOrThrow(FCPublication.PUBLICATION_TITLE_KEY));
        int score = cursor.getInt(cursor.getColumnIndexOrThrow(FCPublication.PUBLICATION_NUMBER_OF_REGISTERED));
        // Populate fields with extracted properties
        tv_title.setText(title);
        tv_score.setText(String.valueOf(score));

        //this can be used to programmaticaly change score background
        View score_sircle = (View) view.findViewById(R.id.v_oval_score_background);


        riv_image.setImageDrawable(context.getResources().getDrawable(R.drawable.default_publication_icon_side_menu));
        final int id = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY));
        final int version = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_VERSION_KEY));
        File photo = new File(Environment.getExternalStorageDirectory()
                + context.getString(R.string.image_folder_path), id + "." + version + ".jpg");
        if (!photo.exists()) return;
        try {
            FileInputStream fis = new FileInputStream(photo.getPath());
            byte[] imageBytes = IOUtils.toByteArray(fis);
            Bitmap bImage = CommonUtil.decodeScaledBitmapFromByteArray(imageBytes, 42, 42);
            Drawable image = new BitmapDrawable(bImage);
            riv_image.setImageDrawable(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

/*
        byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(FCPublication.PUBLICATION_IMAGE_BYTEARRAY_KEY));
        if(imageBytes != null && imageBytes.length > 0){
            Bitmap bImage = CommonUtil.decodeScaledBitmapFromByteArray(imageBytes, 42, 42);
            Drawable image = new BitmapDrawable(bImage);//BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length)
            riv_image.setImageDrawable(image);
        } else {
            riv_image.setImageDrawable(context.getResources().getDrawable(R.drawable.default_publication_icon_side_menu));
        }
*/

/*
        OvalShape ovalShape = new OvalShape();
        float size = context.getResources().getDimension(R.dimen.side_menu_item_score_circle_size);
        ovalShape.resize(size, size);
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint()
                .setColor(context.getResources()
                        .getColor(R.color.side_menu_score_circle_red));
        tv_score.setBackground(shapeDrawable);
*/
    }
}