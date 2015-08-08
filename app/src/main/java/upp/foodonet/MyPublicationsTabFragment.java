package upp.foodonet;

import android.app.Fragment;
import android.app.ListActivity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import DataModel.FCPublication;


public class MyPublicationsTabFragment extends android.support.v4.app.Fragment {

    private static final int ADD_TODO_ITEM_REQUEST = 0;
    FCPublicationListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_publications_tab, container, false);
/*
        adapter = new ListOfEventsAdapter(itemsList, context);
        lv_events_list = (ListView)view.findViewById(R.id.lv_list_of_events);
        lv_events_list.setAdapter(adapter);
*/
        return view;
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
