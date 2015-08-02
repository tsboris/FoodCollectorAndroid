package upp.foodonet;

import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by artyomshapet on 7/19/15.
 */
public class MyFCPublicationItem {
    public final static String NAME = "name";
    public final static String DATE = "date";

    public final static SimpleDateFormat FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.US);

    private String mName = new String();
    private Date mDate = new Date();

    MyFCPublicationItem(String name) {
        this.mName = name;
    }

    // Create a new Item from data packaged in an Intent
    MyFCPublicationItem(Intent intent) {

        mName = intent.getStringExtra(MyFCPublicationItem.NAME);
    }

    public static void packageIntent(Intent intent, String name) {

        intent.putExtra(MyFCPublicationItem.NAME, name);
    }

    public String getName() {
        return mName;
    }
}
