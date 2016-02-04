
package upp.foodonet;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import java.util.Arrays;

import CommonUtilPackage.CommonUtil;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, FacebookCallback<LoginResult> {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    ImageButton signInGoogle;
    public Profile profile;
    ImageButton faceLogIn;
    TextView continueWithoutRegistration;
    ProfileTracker profileTracker;
    AccessTokenTracker accessTokenTracker;
    public CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private boolean isFacebookTrackingStarted;


    public FacebookCallback<LoginResult> facebookCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        InitGoogleLogin();

        faceLogIn = (ImageButton) findViewById(R.id.sing_in_btn_facebook);
        faceLogIn.setOnClickListener(this);
        //faceLogIn.registerCallback(callbackManager, facebookCallback);

        signInGoogle = (ImageButton) findViewById(R.id.sing_in_btn_google);
        signInGoogle.setOnClickListener(this);
        //signInGoogle.setSize(SignInButton.SIZE_STANDARD);
        //signInGoogle.setScopes(gso.getScopeArray());

        continueWithoutRegistration = (TextView) findViewById(R.id.sign_in_tv_continue);
        continueWithoutRegistration.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();

        if (profile != null) {
            Toast.makeText(this, profile.getId(), Toast.LENGTH_LONG).show();
        }
    }

    private void InitGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFacebookTrackingStarted) {
            accessTokenTracker.stopTracking();
            profileTracker.stopTracking();
            isFacebookTrackingStarted = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sing_in_btn_facebook:
                //Toast.makeText(this,"face",Toast.LENGTH_LONG).show();
                facebookCallback = InitFacebookCallback();
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));

                break;
            case R.id.sing_in_btn_google:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                //Toast.makeText(this,"google",Toast.LENGTH_LONG).show();

                break;
            case R.id.sign_in_tv_continue:
                // Toast.makeText(this,"continue",Toast.LENGTH_LONG).show();
                setResult(0);
                finish();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        setResult(0);
        finish();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            CommonUtil.PutCommonPreferencesIsRegisteredGoogleFacebook(this, account);
            setResult(1);
            finish();
            //Toast.makeText(this, acct.getDisplayName(), Toast.LENGTH_LONG).show();
            /*mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);*/
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
            Toast.makeText(this, "Login error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private FacebookCallback<LoginResult> InitFacebookCallback() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().registerCallback(callbackManager, this);
//                new FacebookCallback<LoginResult>() {
//
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        profile = Profile.getCurrentProfile();
//
//                        Toast.makeText(getApplicationContext(), profile.getFirstName() + " is logged to Facebook", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(FacebookException exception) {
//                        Toast.makeText(getApplicationContext(), "LogIn error", Toast.LENGTH_SHORT).show();
//                    }
//                });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                //profile changes following
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
        isFacebookTrackingStarted = true;

        return new FacebookCallback<LoginResult>() {
            private ContentResolver contentResolver;

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                try {
                                    String s = object.getString("locale");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();

                profile = Profile.getCurrentProfile();

                Toast.makeText(getApplicationContext(), profile.getFirstName() + " is logged to Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        profile = Profile.getCurrentProfile();
        CommonUtil.PutCommonPreferencesIsRegisteredGoogleFacebook(this, profile);
        setResult(1);
        finish();
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }
}

