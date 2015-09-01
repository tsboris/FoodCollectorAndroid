package upp.foodonet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import DataModel.FCPublication;
import DataModel.FCTypeOfCollecting;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.InternalRequest;
import DataModel.RegisteredUserForPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class EntranceActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, IFooDoNetSQLCallback, IFooDoNetServerCallback{



    private static final String MY_TAG = "food_EntanceActivity";
    Button btn_pick, btn_share, btn_ask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        startMyPublicationDetailsActivity();

        // Why do we need this?
        //TextView welcomeView = (TextView) findViewById(R.id.welcome);
        //welcomeView.setBackgroundColor(Color.BLUE);
        //=====

        btn_share = (Button)findViewById(R.id.btn_share_welcomeScreen);
        btn_pick = (Button)findViewById(R.id.btn_pick_welcomeScreen);
        btn_ask = (Button)findViewById(R.id.btn_ask_welcomeScreen);
        btn_pick.setOnClickListener(this);
        btn_share.setOnClickListener(this);

        btn_ask.setOnClickListener(this);

        Drawable img_give = getResources().getDrawable( R.drawable.donate_v6_3x );
        Drawable img_take = getResources().getDrawable( R.drawable.collect_v6_3x);
        Drawable img_ask = getResources().getDrawable( R.drawable.collect_v6_3x);
        img_give.setBounds(0, 0, 90, 90);
        img_take.setBounds(0, 0, 90, 90);
        img_ask.setBounds(0, 0, 90, 90);
        btn_share.setCompoundDrawables(null, null, img_give, null);
            btn_share.setCompoundDrawablePadding(10);
        btn_pick.setCompoundDrawables(null, null, img_take, null);
        btn_ask.setCompoundDrawables(null, null, img_ask, null);

        //Bidi bidi = new Bidi(btn_give.getText().toString(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        //Log.i("food", "bidi.getBaseLevel() = " + bidi.getBaseLevel());



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

    private void startMyPublicationDetailsActivity()
    {
        FCPublication samplePub = new FCPublication();
        samplePub.setTitle("pomodori");
        samplePub.setSubtitle("pomodori in boxes a 3 kg");
        samplePub.setPhotoUrl("http://static.adzerk.net/Advertisers/90bd75b000054d73ad21a6f72a11fc14.jpg");
        samplePub.setAddress("Ramat Gan, HaRoeh 257");

        ArrayList<RegisteredUserForPublication> regUsers = new ArrayList<RegisteredUserForPublication>();
        RegisteredUserForPublication u1 = new RegisteredUserForPublication();
        u1.setId(11111);
        regUsers.add(u1);
        RegisteredUserForPublication u2 = new RegisteredUserForPublication();
        u2.setId(2222);
        regUsers.add(u2);
        RegisteredUserForPublication u3 = new RegisteredUserForPublication();
        u1.setId(3333);
        regUsers.add(u3);
        samplePub.setRegisteredForThisPublication(regUsers);

        Intent intent = new Intent(this, MyPublicationDetailsActivity.class);
        intent.putExtra(MyPublicationDetailsActivity.PUBLICATION_PARAM, samplePub);

        startActivity(intent);
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
            case R.id.btn_pick_welcomeScreen:
                Intent mapListIntent = new Intent(this, MapAndListActivity.class);
                startActivity(mapListIntent);
                break;
            case R.id.btn_share_welcomeScreen:
                Intent myPubsIntent = new Intent(this, MyPublicationsActivity.class);
                startActivity(myPubsIntent);
                break;
            case R.id.btn_ask_welcomeScreen:


                //FooDoNetSQLExecuterAsync saveNewTask = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                //saveNewTask.execute(new InternalRequest(InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION, newPublication));

/*
                HttpServerConnectorAsync postNewPubTask
                        = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), this);
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_PUBLICATION, newPublication);
                ir.ServerSubPath = getResources().getString(R.string.server_add_new_publication_path);
                postNewPubTask.execute(ir);
*/


                break;
        }
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        if(request.publicationForSaving == null){
            Log.e(MY_TAG, "got null request.publicationForSaving");
            return;
        }
        HttpServerConnectorAsync postNewPubTask
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), this);
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_PUBLICATION, request.publicationForSaving);
        ir.ServerSubPath = getResources().getString(R.string.server_add_new_publication_path);
        postNewPubTask.execute(ir);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {

    }
}
