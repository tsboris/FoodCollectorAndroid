package upp.foodonet;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import DataModel.FCPublication;

import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class AllPublicationsActivity extends FooDoNetCustomActivityConnectedToService {

    private final String MY_TAG = "AllPublicationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_publications);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.lv_all_publications_list);

        String[] from = new String[] {FCPublication.PUBLICATION_TITLE_KEY, FCPublication.PUBLICATION_SUBTITLE_KEY};
        int[] to = new int[] {R.id.tv_title_myPub_item, R.id.tv_subtitle_myPub_item};

        Cursor cursor = getCursor();
        adapter = new SimpleCursorAdapter(this, R.layout.my_fcpublication_item, cursor, from, to, 0);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

    protected Cursor getCursor()
    {
       return new Cursor() {
           @Override
           public int getCount() {
               return 0;
           }

           @Override
           public int getPosition() {
               return 0;
           }

           @Override
           public boolean move(int offset) {
               return false;
           }

           @Override
           public boolean moveToPosition(int position) {
               return false;
           }

           @Override
           public boolean moveToFirst() {
               return false;
           }

           @Override
           public boolean moveToLast() {
               return false;
           }

           @Override
           public boolean moveToNext() {
               return false;
           }

           @Override
           public boolean moveToPrevious() {
               return false;
           }

           @Override
           public boolean isFirst() {
               return false;
           }

           @Override
           public boolean isLast() {
               return false;
           }

           @Override
           public boolean isBeforeFirst() {
               return false;
           }

           @Override
           public boolean isAfterLast() {
               return false;
           }

           @Override
           public int getColumnIndex(String columnName) {
               return 0;
           }

           @Override
           public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
               return 0;
           }

           @Override
           public String getColumnName(int columnIndex) {
               return null;
           }

           @Override
           public String[] getColumnNames() {
               return new String[0];
           }

           @Override
           public int getColumnCount() {
               return 0;
           }

           @Override
           public byte[] getBlob(int columnIndex) {
               return new byte[0];
           }

           @Override
           public String getString(int columnIndex) {
               return null;
           }

           @Override
           public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {

           }

           @Override
           public short getShort(int columnIndex) {
               return 0;
           }

           @Override
           public int getInt(int columnIndex) {
               return 0;
           }

           @Override
           public long getLong(int columnIndex) {
               return 0;
           }

           @Override
           public float getFloat(int columnIndex) {
               return 0;
           }

           @Override
           public double getDouble(int columnIndex) {
               return 0;
           }

           @Override
           public int getType(int columnIndex) {
               return 0;
           }

           @Override
           public boolean isNull(int columnIndex) {
               return false;
           }

           @Override
           public void deactivate() {

           }

           @Override
           public boolean requery() {
               return false;
           }

           @Override
           public void close() {

           }

           @Override
           public boolean isClosed() {
               return false;
           }

           @Override
           public void registerContentObserver(ContentObserver observer) {

           }

           @Override
           public void unregisterContentObserver(ContentObserver observer) {

           }

           @Override
           public void registerDataSetObserver(DataSetObserver observer) {

           }

           @Override
           public void unregisterDataSetObserver(DataSetObserver observer) {

           }

           @Override
           public void setNotificationUri(ContentResolver cr, Uri uri) {

           }

           @Override
           public Uri getNotificationUri() {
               return null;
           }

           @Override
           public boolean getWantsAllOnMoveCalls() {
               return false;
           }

           @Override
           public Bundle getExtras() {
               return null;
           }

           @Override
           public Bundle respond(Bundle extras) {
               return null;
           }
       };
    }


    @Override
    public void OnNotifiedToFetchData() {
//TODO
    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {
//TODO
    }

    @Override
    public void OnGooglePlayServicesCheckError() {
        // TODO
    }

    @Override
    public void OnInternetNotConnected() {
        // TODO
    }

    private ListView listView;
    private SimpleCursorAdapter adapter;
}
