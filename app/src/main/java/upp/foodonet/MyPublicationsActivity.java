package upp.foodonet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class MyPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService implements View.OnClickListener  {
    
    SearchView src_all_pub_listView;
    Button btn_add_new_publication ,btn_navigate_share ,btn_navigate_take,btn_active_pub,btn_not_active_pub,btn_ending_pub;
    Animation animZoomIn;
/*    ToggleButton tgl_btn_navigate_share;
      ToggleButton tgl_btn_navigate_take;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publications);

        src_all_pub_listView = (SearchView)findViewById(R.id.searchView1);
        btn_ending_pub = (Button)findViewById(R.id.btn_publication_ending);
        btn_ending_pub.setOnClickListener(this);
        btn_not_active_pub = (Button)findViewById(R.id.btn_publication_notActive);
        btn_not_active_pub.setOnClickListener(this);
        btn_active_pub = (Button)findViewById(R.id.btn_publication_active);
        btn_active_pub.setOnClickListener(this);
        btn_add_new_publication = (Button)findViewById(R.id.btn_add_new_myPubsLst);
        btn_add_new_publication.setOnClickListener(this);
        btn_navigate_share = (Button)findViewById(R.id.btn_share_mypubs);
        btn_navigate_take = (Button)findViewById(R.id.btn_take_mypubs);
        //tgl_btn_navigate_share.setOnClickListener(this);
        btn_navigate_take.setOnClickListener(this);
        animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in);

        Drawable navigate_share = getResources().getDrawable( R.drawable.donate_v62x_60x60 );
        Drawable navigate_take = getResources().getDrawable( R.drawable.collect_v6_60x60);
        navigate_share.setBounds(0, 0, 60, 60);
        navigate_take.setBounds(0, 0, 60, 60);
        btn_navigate_share.setCompoundDrawables(null, navigate_share, null, null);
        btn_navigate_share.setCompoundDrawablePadding(10);
        btn_navigate_take.setCompoundDrawables(null, navigate_take, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // btn_navigate_take.setChecked(false);
        btn_navigate_take.setEnabled(true);
        btn_navigate_share.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_publications, menu);
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

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_new_myPubsLst:
                Intent addNewPubIntent = new Intent(this, AddNewFCPublicationActivity.class);
                startActivityForResult(addNewPubIntent, 1);
                break;
            //case R.id.tgl_btn_share_mypubs:
            //    break;
            case R.id.btn_take_mypubs:
                Intent intent = new Intent(this, MapAndListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.btn_publication_ending:
                btn_not_active_pub.setAnimation(null);
                btn_active_pub.setAnimation(null);
                btn_ending_pub.startAnimation(animZoomIn);

                break;
            case R.id.btn_publication_active:
                btn_not_active_pub.setAnimation(null);
                btn_ending_pub.setAnimation(null);
                btn_active_pub.startAnimation(animZoomIn);

                break;
            case R.id.btn_publication_notActive:
                btn_active_pub.setAnimation(null);
                btn_ending_pub.setAnimation(null);
                btn_not_active_pub.startAnimation(animZoomIn);
                break;
        }
    }
}
