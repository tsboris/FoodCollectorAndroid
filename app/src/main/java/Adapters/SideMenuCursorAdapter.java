package Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import DataModel.FCPublication;
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
        // Extract properties from cursor
        String title = cursor.getString(cursor.getColumnIndexOrThrow(FCPublication.PUBLICATION_TITLE_KEY));
        int score = cursor.getInt(cursor.getColumnIndexOrThrow(FCPublication.PUBLICATION_NUMBER_OF_REGISTERED));
        // Populate fields with extracted properties
        tv_title.setText(title);
        tv_score.setText(String.valueOf(score));

        //this can be used to programmaticaly change score background
        View score_sircle = (View) view.findViewById(R.id.v_oval_score_background);
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