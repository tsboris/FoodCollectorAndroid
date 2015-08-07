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
import android.widget.TextView;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.Locale;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class EntranceActivity extends FooDoNetCustomActivityConnectedToService {

    Button btn_give, btn_take;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        // Why do we need this?
        //TextView welcomeView = (TextView) findViewById(R.id.welcome);
        //welcomeView.setBackgroundColor(Color.BLUE);
        //=====

        btn_give = (Button)findViewById(R.id.btn_entrance_give);
        btn_take = (Button)findViewById(R.id.btn_entrance_take);

        Drawable img_give = getResources().getDrawable( R.drawable.first_screen_donate );
        Drawable img_take = getResources().getDrawable( R.drawable.first_screen_collect);
        img_give.setBounds( 0, 0, 60, 60 );
        img_take.setBounds( 0, 0, 60, 60 );
        btn_give.setCompoundDrawables(null, null, img_give, null); btn_give.setCompoundDrawablePadding(10);
        btn_take.setCompoundDrawables(img_take, null, null, null );

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
}
