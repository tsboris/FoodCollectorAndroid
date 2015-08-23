package upp.foodonet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.Locale;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class EntranceActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener {
    private static final String MY_TAG = "food_EntanceActivity";
    LinearLayout ll_btn_share,ll_btn_pick;
    Button btn_give;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        // Why do we need this?
        //TextView welcomeView = (TextView) findViewById(R.id.welcome);
        //welcomeView.setBackgroundColor(Color.BLUE);
        //=====

        ll_btn_share = (LinearLayout)findViewById(R.id.ll_btn_pick_mainScreen);
        ll_btn_pick = (LinearLayout)findViewById(R.id.ll_btn_share_mainScreen);
        btn_give = (Button)findViewById(R.id.btn_entrance_give);
     /*   Drawable img_give = getResources().getDrawable( R.drawable.donate_v6_3x );
        Drawable img_take = getResources().getDrawable( R.drawable.collect_v6_3x);
        Drawable img_ask = getResources().getDrawable( R.drawable.collect_v6_3x);
        img_give.setBounds(0, 0, 300, 300);
        img_take.setBounds(0, 0, 300, 300);
        btn_give.setCompoundDrawables(null, null, img_give, null); btn_give.setCompoundDrawablePadding(10);
        btn_take.setCompoundDrawables(null, null, img_take, null);
        btn_ask.setCompoundDrawables(null, null, img_ask, null);*/

        Bidi bidi = new Bidi(btn_give.getText().toString(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        Log.i("food", "bidi.getBaseLevel() = " + bidi.getBaseLevel());



/*
        TextView publishTextView = (TextView) findViewById(R.id.publishText);
        publishTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EntranceActivity.this, MainActivity.class);
                startActivity(intent);
            }
            });

        RoundedImageView publishImageView = (RoundedImageView) findViewById(R.id.publishImage);
        //publishImageView.setBackgroundColor(Color.BLUE);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.first_screen_collect);
        publishImageView.setImageBitmap(icon);

        publishImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EntranceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        */



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entrance, menu);
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
    public void OnNotifiedToFetchData() {

    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {

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
            case R.id.ll_btn_pick_mainScreen:
                Intent mapIntent = new Intent(this, MapAndListActivity.class);
                startActivity(mapIntent);
                break;
          /*  case R.id.ll_btn_share_mainScreen:
                Intent myPublicationList = new Intent(this, MyPublicationsListActivity.class);
                startActivity(myPublicationList);
                break;*/
            /*case R.id.btn_entrance_ask:
                Intent  = new Intent(this, MapAndListActivity.class);
                startActivity();
                break;*/
        }
    }
}
