package upp.foodonet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

//import Adapters.FCPublicationListAdapter;
import java.util.List;

import DataModel.FCPublication;


public class MyPublicationsTabFragment
        extends android.support.v4.app.Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener{

    private static final int ADD_TODO_ITEM_REQUEST = 0;
    //FCPublicationListAdapter mAdapter;
    private Context context;
    SimpleCursorAdapter adapter;

    ListView lv_my_publications;
    //Button btn_new_publication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_publications_tab, container, false);

        lv_my_publications = (ListView)view.findViewById(R.id.lv_all_active_publications);
        //btn_new_publication = (Button)view.findViewById(R.id.btn_add_new_publication);
        //btn_new_publication.setOnClickListener(this);

        String[] from = new String[] {FCPublication.PUBLICATION_TITLE_KEY, FCPublication.PUBLICATION_SUBTITLE_KEY};
        int[] to = new int[] {R.id.tv_title_myPub_item, R.id.tv_subtitle_myPub_item};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(context, R.layout.my_fcpublication_item, null, from,
                to, 0);
        lv_my_publications.setAdapter(adapter);
/*
        adapter = new ListOfEventsAdapter(itemsList, context);
        lv_events_list = (ListView)view.findViewById(R.id.lv_list_of_events);
        lv_events_list.setAdapter(adapter);
*/
        return view;
    }

    public void SetContext(Context context){
        this.context = context;
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(context == null) return null;
        String[] projection = FCPublication.GetColumnNamesArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(context, FooDoNetSQLProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, AddNewFCPublicationActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        mAdapter = new FCPublicationListAdapter(getApplicationContext());

        getListView().setFooterDividersEnabled(true);

        Button headerView = (Button)getLayoutInflater().inflate(R.layout.header_view, null);

        // TODO - Add footerView to ListView
        getListView().addHeaderView(headerView);


        // TODO - Attach Listener to FooterView
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("FOODONET", "Entered submitButton.OnClickListener.onClick()");

                Intent intent = new Intent(MainActivity.this, AddNewFCPublicationActivity.class);
                startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);
            }
        });

        getListView().setAdapter(mAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("FOODONET", "Entered onActivityResult()");


        if (requestCode == ADD_TODO_ITEM_REQUEST) {
            if(resultCode == RESULT_OK){

                FCPublication item = new FCPublication(data);
                mAdapter.add(item);
            }
            if (resultCode == RESULT_CANCELED) {

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
}
