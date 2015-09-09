package upp.foodonet;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;
import FooDoNetServerClasses.DownloadImageTask;
import FooDoNetServerClasses.IDownloadImageCallBack;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class PublicationDetailsActivity extends FooDoNetCustomActivityConnectedToService
        implements IDownloadImageCallBack
{
    public static final String PUBLICATION_PARAM = "publication";
    public static final String IS_OWN_PUBLICATION_PARAM = "is_own";

    private static final int PHOTO_RADIUS = 200;

    private static final String MY_TAG = "food_PubDetails";

    private FCPublication publication;

    private TextView subtitleTextView;
    private TextView interestedPersonsCountTextView;
    private TextView postAddressTextView;
    private TextView publicationDescriptionTextView;
    private ListView interestedsListView;
    private ArrayAdapter<String> interestedsAdapter;
    private Button cancelPublicationButton;

    Button btnCall, btnRishum, btnSms, btnNavigate;

    private AlertDialog cancelPublicationDialog;

    private ImageButton photoButton;
    private Bitmap photoBmp;

    private boolean isOwnPublication;

    //region Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent i = getIntent();
            this.publication = (FCPublication) i.getSerializableExtra(PUBLICATION_PARAM);
            this.isOwnPublication = publication.getPublisherUID() == CommonUtil.GetIMEI(this);
        }
        catch (Exception ex) {
            Log.e(MY_TAG, "error deserializing passed parameters: " + ex.getMessage());
            return;
        }

        if (this.isOwnPublication){
            setContentView(R.layout.activity_my_publication_details);
        }
        else{
            setContentView(R.layout.activity_foreign_publication_details);
        }

        if (publication.getTitle() != null) {
            this.setTitle(publication.getTitle());
            subtitleTextView = (TextView) findViewById(R.id.tv_subtitle);
            subtitleTextView.setText(publication.getTitle());
        }

        postAddressTextView =            (TextView) findViewById(R.id.post_address);
        publicationDescriptionTextView = (TextView) findViewById(R.id.publication_description);

        postAddressTextView.setText(publication.getAddress());
        publicationDescriptionTextView.setText(publication.getSubtitle());

        LoadPhoto(this.publication.getPhotoUrl());

        if (!isOwnPublication)
        {
            makeBlueButtons();
        }

        makeInterestedsList();

        if (isOwnPublication)
        {
            makeTheCancelDialog();

            makeCancelButton();
        }
    }

    //region methods making parts of activity (to call in OnCreate)
    private void makeBlueButtons()
    {
        btnCall = (Button)findViewById(R.id.btn_call);
        Drawable img_call = getResources().getDrawable( R.drawable.call_2x);
        img_call.setBounds(0, 0, 30, 30);
        btnCall.setCompoundDrawables(null, null, img_call, null);

        btnRishum = (Button)findViewById(R.id.btn_rishum);
        Drawable img_rishum = getResources().getDrawable( R.drawable.rishum_2x);
        img_rishum.setBounds(0, 0, 30, 30);
        btnRishum.setCompoundDrawables(null, null, img_rishum, null);

        btnSms = (Button)findViewById(R.id.btn_sms);
        Drawable img_sms = getResources().getDrawable( R.drawable.sms_2x);
        img_sms.setBounds(0, 0, 30, 30);
        btnSms.setCompoundDrawables(null, null, img_sms, null);

        btnNavigate = (Button)findViewById(R.id.btn_navigate);
        Drawable img_navigate = getResources().getDrawable( R.drawable.navigate_2x);
        img_navigate.setBounds(0, 0, 30, 30);
        btnNavigate.setCompoundDrawables(null, null, img_navigate, null);
    }


    private void makeCancelButton() {
        cancelPublicationButton        = (Button) findViewById(R.id.btn_cancel_publication);
        cancelPublicationButton.setText(getString(R.string.cancel_publication));
        cancelPublicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPublicationDialog.show();
            }
        });
    }

    private void makeTheCancelDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.cancel_publication);
        builder.setMessage(R.string.cancel_publication_question);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                PublicationDetailsActivity.this.finishWithCancelPublicationResult();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        cancelPublicationDialog = builder.create();
    }

    private void finishWithCancelPublicationResult()
    {
        Intent intent= new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra("userAction", "cancelPublication");
        finish();
    }

    private void makeInterestedsList() {
        interestedPersonsCountTextView = (TextView) findViewById(R.id.interested_persons_count);
        interestedPersonsCountTextView.setText(getString(R.string.going_to_collect) + "  "
                + Integer.toString(publication.getRegisteredForThisPublication().size()));

        interestedsListView = (ListView)findViewById(R.id.lst_interested_persons_list);
        ArrayList<String> interestedPersonsInfoList = mapRegisteredPersonsToStringArray(
                publication.getRegisteredForThisPublication()
        );
        interestedsAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                interestedPersonsInfoList);
        interestedsListView.setAdapter(interestedsAdapter);
    }

    private ArrayList<String> mapRegisteredPersonsToStringArray(ArrayList<RegisteredUserForPublication> arg)
    {
        ArrayList<String> l = new ArrayList<String>();
        for (RegisteredUserForPublication u: arg) {
            l.add( Integer.toString( u.getId()) );
        }
        return l;
    }
    //endregion

    private void LoadPhoto(String photoUrlString)
    {
        if (photoUrlString==null || photoUrlString.isEmpty()){
            Log.i(MY_TAG, "photo url was null or empty");
            return;
        }

        /*
        URL url = null;
        try {
            url = new URL(photoUrlString);
            photoBmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            Bitmap roundBmp = RoundedImageView.getRoundedCroppedBitmap(photoBmp, PHOTO_RADIUS);
            photoButton = (ImageButton)findViewById(R.id.photoButton);
            photoButton.setImageBitmap(roundBmp);
        } catch (MalformedURLException e) {
            Log.e(MY_TAG, "malformed photo url: " + e.getMessage());
        } catch (IOException e) {
            Log.e(MY_TAG, "error opening internet connection to photo url: " + e.getMessage());
        }

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " photo clicked!", Toast.LENGTH_LONG).show();
            }
        }); */

        new DownloadImageTask(this).execute(photoUrlString);
    }

    @Override
    public void OnImageDownloaded(Bitmap result) {
        this.photoBmp = result;
        Bitmap roundBmp = RoundedImageView.getRoundedCroppedBitmap(photoBmp, PHOTO_RADIUS);
        photoButton = (ImageButton)findViewById(R.id.btn_photoButton);
        photoButton.setImageBitmap(roundBmp);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        MY_TAG + " photo clicked!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_publication_details, menu);
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
    //endregion

    //region FooDoNetCustomActivityConnectedToService implementation
/*
    @Override
    public void OnNotifiedToFetchData() {
        //TODO
    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {
        //TODO
    }
*/

    @Override
    public void OnGooglePlayServicesCheckError() {
        //TODO
    }

    @Override
    public void OnInternetNotConnected() {
        //TODO
    }
    //endregion

}
