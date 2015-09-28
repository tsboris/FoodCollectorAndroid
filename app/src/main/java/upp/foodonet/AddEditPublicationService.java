package upp.foodonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AddEditPublicationService extends IntentService implements IFooDoNetSQLCallback, IFooDoNetServerCallback {

    private static final String MY_TAG = "food_serviceAddEditPub";

    private static final String ACTION_SAVE_NEW_PUBLICATION = "upp.foodonet.action.save.new.pub";
    private static final String ACTION_SAVE_EDITED_PUBLICATION = "upp.foodonet.action.save.edited.pub";


    private static final String EXTRA_PARAM_PUBLICATION = "upp.foodonet.new.pub.param";

    private CognitoCachingCredentialsProvider credentialsProvider;

    private FCPublication editedPublicationForSaving;

    public static void StartSaveNewPublication(Context context, FCPublication pubToSave) {
        Intent intent = new Intent(context, AddEditPublicationService.class);
        intent.setAction(ACTION_SAVE_NEW_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_PUBLICATION, pubToSave);
        context.startService(intent);
    }

    public static void StartSaveEditedPublication(Context context, FCPublication pubToSave) {
        Intent intent = new Intent(context, AddEditPublicationService.class);
        intent.setAction(ACTION_SAVE_EDITED_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_PUBLICATION, pubToSave);
        context.startService(intent);
    }

    public AddEditPublicationService() {
        super("SaveNewPublicationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_SAVE_NEW_PUBLICATION:
                    FCPublication publicationToSave = (FCPublication) intent.getSerializableExtra(EXTRA_PARAM_PUBLICATION);
                    if (publicationToSave == null) {
                        Log.e(MY_TAG, "got no publication to save (onHandleIntent)");
                        return;
                    }
                    handleActionSaveNewPublication(publicationToSave);
                    break;
                case ACTION_SAVE_EDITED_PUBLICATION:
                    FCPublication editedPubToSave = (FCPublication) intent.getSerializableExtra(EXTRA_PARAM_PUBLICATION);
                    if (editedPubToSave == null) {
                        Log.e(MY_TAG, "got no publication to save (onHandleIntent)");
                        return;
                    }
                    editedPublicationForSaving = editedPubToSave;
                    handleActionSaveEditedPublication(editedPubToSave);
                    break;
            }
        }
    }

    private void handleActionSaveEditedPublication(FCPublication publication) {
        HttpServerConnectorAsync connector1
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
        String subPath = getString(R.string.server_edit_publication_path);
        subPath = subPath.replace("{0}", String.valueOf(publication.getUniqueId()));
        InternalRequest ir1
                = new InternalRequest(InternalRequest.ACTION_PUT_EDIT_PUBLICATION,
                subPath, publication);
        ir1.publicationForSaving = publication;
        connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir1);
/*
        FooDoNetSQLExecuterAsync saveExecuter
                = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        saveExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new InternalRequest(InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION, publication));
*/
    }

    private void handleActionSaveNewPublication(FCPublication publication) {
        if (publication.getUniqueId() == 0) {
            int newNegativeID = 0;
            Cursor negIdCursor = getContentResolver()
                    .query(FooDoNetSQLProvider.URI_GET_NEW_NEGATIVE_ID,
                            new String[]{FCPublication.PUBLICATION_NEW_NEGATIVE_ID}, null, null, null);
            if (negIdCursor != null && negIdCursor.moveToFirst()) {
                newNegativeID = negIdCursor.getInt(
                        negIdCursor.getColumnIndex(FCPublication.PUBLICATION_NEW_NEGATIVE_ID));
                newNegativeID = newNegativeID >= 0 ? -1 : newNegativeID;
                publication.setUniqueId(newNegativeID);
            }
        }
        FooDoNetSQLExecuterAsync saveExecuter
                = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        saveExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new InternalRequest(
                        InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION, publication));
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                if (request.Status == InternalRequest.STATUS_FAIL) {
                    Log.i(MY_TAG, "cant save new pub in sql");
                    return;
                }
                if(TextUtils.isEmpty(request.publicationForSaving.getPhotoUrl())){
                    File imageToSave = new File(request.publicationForSaving.getPhotoUrl());
                    File imageToSaveOnDevice=new File(Environment.getExternalStorageDirectory(),
                            request.publicationForSaving.getUniqueId() + "." + request.publicationForSaving.getVersion() + ".jpg");
                    if (imageToSaveOnDevice.exists()) {
                        imageToSaveOnDevice.delete();
                    }
                    try {
                        copy(imageToSave, imageToSaveOnDevice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                Log.i(MY_TAG, "new pub successfully saved in db, sending to server");
                NotifyToBListenerAboutPubSavedInDB(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_SQL_SUCCESS);
                HttpServerConnectorAsync connector
                        = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                InternalRequest ir
                        = new InternalRequest(InternalRequest.ACTION_POST_NEW_PUBLICATION,
                        getResources().getString(R.string.server_add_new_publication_path),
                        request.publicationForSaving);
                ir.publicationForSaving = request.publicationForSaving;
                connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
                if (request.Status == InternalRequest.STATUS_FAIL) {
                    Log.i(MY_TAG, "cant update new pub's id in sql");
                    return;
                }
                if (request.publicationForSaving.getPhotoUrl() != null
                        && !TextUtils.isEmpty(request.publicationForSaving.getPhotoUrl()))
                    UploadImageToAmazon(request.publicationForSaving);
                //NotifyToBListenerAboutPubSavedInDB(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_COMPLETE);
                break;
            case InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION:
                if (request.publicationForSaving.getPhotoUrl() != null
                        && !TextUtils.isEmpty(request.publicationForSaving.getPhotoUrl()))
                    UploadImageToAmazon(request.publicationForSaving);
/*
                if(request.Status == InternalRequest.STATUS_FAIL){
                    Log.i(MY_TAG, "cant update edited pub in sql");
                    return;
                }
                HttpServerConnectorAsync connector1
                        = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                String subPath = getString(R.string.server_edit_publication_path);
                subPath = subPath.replace("{0}", String.valueOf(request.publicationForSaving.getUniqueId()));
                InternalRequest ir1
                        = new InternalRequest(InternalRequest.ACTION_PUT_EDIT_PUBLICATION,
                        subPath, request.publicationForSaving);
                ir1.publicationForSaving = request.publicationForSaving;
                connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir1);
*/
                break;
        }
    }

    private void NotifyToBListenerAboutPubSavedInDB(int eventCode) {
        Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
        intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, eventCode);
        sendBroadcast(intent);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand) {
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                Log.i(MY_TAG, "succeeded saving pub to server, new id: "
                        + response.publicationForSaving.getNewIdFromServer());
                FooDoNetSQLExecuterAsync executerAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                executerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new InternalRequest(
                        InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER,
                        response.publicationForSaving));
                break;
            case InternalRequest.ACTION_PUT_EDIT_PUBLICATION:
                FooDoNetSQLExecuterAsync saveExecuter
                        = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                saveExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        new InternalRequest(InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION, response.publicationForSaving));
                break;
        }
    }

    private void UploadImageToAmazon(FCPublication publication) {
        RegisterAWSS3();
        File imageToSave = new File(publication.getPhotoUrl());
        //Environment.getExternalStorageDirectory(), publication.getPhotoUrl());

        String imageName = String.valueOf(publication.getUniqueId()) + "."
                + String.valueOf(publication.getVersion()) + ".jpg";
        uploadPhotoForPublication(imageToSave, imageName);
    }

    public void RegisterAWSS3() {
        // initialize a credentials provider object with your Activity’s context and
        // the values from your identity pool
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // get the context for the current activity
                "458352772906", // your AWS Account id
                "us-east-1:ec4b269f-88a9-471d-b548-7886e1f9f2d7", // your identity pool id
                "arn:aws:iam::458352772906:role/Cognito_food_collector_poolUnauth_DefaultRole", // an unauthenticated role ARN
                "arn:aws:iam::458352772906:role/Cognito_food_collector_poolAuth_DefaultRole",// an authenticated role ARN
                Regions.US_EAST_1 //Region
        );
        Log.i(MY_TAG, "succesfully registered to amazon");
    }

    private void uploadPhotoForPublication(File sourceFile, String fileName) {
        // Create an S3 client
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));

        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        TransferObserver observer = transferUtility.upload(
                "foodcollectordev",     /* The bucket to upload to */
                fileName,          /* The key for the uploaded object */
                sourceFile        /* The file where the data to upload exists */
        );

        observer.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                // do something
                //Log.d(MY_TAG,"AMAZON UPLOAD STATE IS ---> " + state);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                //Display percentage transfered to user
                //Log.d(MY_TAG,"AMAZON PROGRESS OF UPLOAD PICTURE IS ---> " + percentage);
                if (percentage >= 99)
                    NotifyToBListenerAboutPubSavedInDB(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_COMPLETE);
            }

            @Override
            public void onError(int id, Exception ex) {
                // do something
                Log.d(MY_TAG, "OOOOPPPSSSS - UPLOAD DIDN'T WORK WELL ---> " + ex.getMessage());
            }

        });

    }


}
