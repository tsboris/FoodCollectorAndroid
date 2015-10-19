package upp.foodonet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.InputStream;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class ShareToTwitterActivity extends Activity implements View.OnClickListener{

    private static final String MY_TAG = "food_twitter";

    /* Shared preference keys */
    private static final String PREF_NAME = "twitter_pref";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    private static final String PREF_USER_NAME = "twitter_user_name";

    public static final String  OAUTH_CALLBACK_SCHEME   = "x-oauthflow-twitter";
    public static final String  OAUTH_CALLBACK_HOST     = "callback";
    public static final String  CALLBACK_URL      = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 100;
    public static final String PUBLICATION_PARAM = "publication";

    private ProgressDialog pDialog;

    private static Twitter twitter;
    private static RequestToken requestToken;

    private static SharedPreferences mSharedPreferences;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;

    private View shareLayout;
    private EditText mShareEditText;
    private Button btnShare;

    private InputStream imageStreamForSharing;
    private ImageView imgForSharing;

    private FCPublication publication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to_twitter);

        Intent i = getIntent();
        this.publication = (FCPublication) i.getSerializableExtra(PUBLICATION_PARAM);
        if (publication == null) {
            Log.e(MY_TAG, "no publication found in extras!");
            finish();
        }

        int imageSize = getResources().getDimensionPixelSize(R.dimen.pub_details_image_size);
        Drawable image = CommonUtil.GetBitmapDrawableFromFile(
                publication.getUniqueId() + "." + publication.getVersion() + ".jpg",
                getString(R.string.image_folder_path), imageSize, imageSize);


        initTwitterConfigs();

        /* Enabling strict mode */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btnShare = (Button)findViewById(R.id.btn_share);
        btnShare.setOnClickListener(this);

        shareLayout = (LinearLayout) findViewById(R.id.share_layout);
        mShareEditText = (EditText) findViewById(R.id.share_text);
        imgForSharing = (ImageView)findViewById(R.id.imageToPost);

        imgForSharing.setImageDrawable(image);


        /* Initialize application preferences */
        mSharedPreferences = getSharedPreferences(PREF_NAME, 0);

        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        loginToTwitter();
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }

    private void loginToTwitter() {
        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                /**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  */
                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                Log.d(MY_TAG, e.getMessage());
                e.printStackTrace();
            }
        } else {
            shareLayout.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_to_twitter, menu);
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

    class updateTwitterStatus extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ShareToTwitterActivity.this);
            pDialog.setMessage("Posting to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected Void doInBackground(String... args) {

            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(consumerKey);
                builder.setOAuthConsumerSecret(consumerSecret);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                StatusUpdate statusUpdate = new StatusUpdate(status);
                imageStreamForSharing = CommonUtil.ConvertFileToInputStream(publication.getUniqueId() + "." +
                                                                            publication.getVersion() + ".jpg",
                                                                        getString(R.string.image_folder_path));
                statusUpdate.setMedia(publication.getTitle(), imageStreamForSharing);

                twitter4j.Status response = twitter.updateStatus(statusUpdate);

                Log.d("Status", response.getText());

            } catch (TwitterException e) {
                Log.d("Failed to post!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

/* Dismiss the progress dialog after sharing */
            pDialog.dismiss();

            Toast.makeText(ShareToTwitterActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT).show();

            // Clearing EditText field
            mShareEditText.setText("");

            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();

                saveTwitterInfo(accessToken);

                shareLayout.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Saving user information, after user is authenticated for the first time.
     * You don't need to show user to login, until user has a valid access toen
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userID = accessToken.getUserId();

        User user;
        try {
            user = twitter.showUser(userID);

            String username = user.getName();

            /* Storing oAuth tokens to shared preferences */
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_share:
                final String status = mShareEditText.getText().toString();

                if (status.trim().length() > 0) {
                    new updateTwitterStatus().execute(status);
                } else {
                    Toast.makeText(this, "Message is empty!!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
