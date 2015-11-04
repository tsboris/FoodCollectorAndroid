package upp.foodonet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.drive.realtime.internal.BeginCompoundOperationRequest;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.simple.JSONObject;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.PushObject;

import java.util.Map;

// Service listenining for  push notifications
public class FooDoNetGcmListenerService extends GcmListenerService implements IFooDoNetServerCallback, IFooDoNetSQLCallback {
    private static final String TAG = "food_gcmListener";
    private PushObject pushObject;

    public static final String PUBLICATION_NUMBER = "pubnumber";
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //String message = data.getString("message");
        //Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Message: " + message);

        if(true) return; // turn off notifications

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }
        //region structure of notifications
//        message = "{\"type\":\"new_publication\",\"data\":{ id:579}}";
//        message = "{\"type\":\"deleted_publication\",\"data\":{ id:561}}";
//        message = "{\"type\":\"publication_report\",\"data\":{ publication_id : 542, publication_version : 1, date_of_report :11111112, report :5}}";
//        message = "{\"type\":\"registeration_for_publication\",\"data\":{ id:574}}";
//        PushObject pushObject = new PushObject();
//        pushObject.PushObjectType = "registeration_for_publication";
//        pushObject.ID = 574;
        //end region

        pushObject = PushObject.DecodePushObject(data);
        HandleMessage(pushObject);
        //sendNotification(message);
    }

    private void HandleMessage(PushObject pushObject) {
        if(pushObject == null || pushObject.PushObjectType == null || TextUtils.isEmpty(pushObject.PushObjectType))
            return;
        switch (pushObject.PushObjectType) {
            case PushObject.PUSH_OBJECT_VALUE_NEW:
                String basePath = getString(R.string.server_base_url);
                String subPath = getString(R.string.server_edit_publication_path).replace("{0}", String.valueOf(pushObject.ID));
                HttpServerConnectorAsync connectorAsync = new HttpServerConnectorAsync(basePath, (IFooDoNetServerCallback) this);
                connectorAsync.execute(new InternalRequest(InternalRequest.ACTION_PUSH_NEW_PUB, subPath),
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                getResources().getString(R.string.server_get_registered_for_publications)),
                        new InternalRequest(InternalRequest.ACTION_GET_PUBLICATION_REPORTS,
                                getResources().getString(R.string.server_get_publication_report)));
                break;
            case PushObject.PUSH_OBJECT_VALUE_DELETE:
                FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                InternalRequest irPubIdToDelete = new InternalRequest(InternalRequest.ACTION_PUSH_PUB_DELETED);
                irPubIdToDelete.PublicationID = pushObject.ID;
                sqlExecuterAsync.execute(irPubIdToDelete);
                break;
            case PushObject.PUSH_OBJECT_VALUE_REPORT:
                PublicationReport publicationReport
                        = new PublicationReport(pushObject.ID,
                                                pushObject.PublicationID,
                                                pushObject.PublicationVersion,
                                                pushObject.Report,
                                                pushObject.DateOfReport, "");//// TODO: 31.10.2015 also need publisher uuid
                InternalRequest irReportFromPush = new InternalRequest(InternalRequest.ACTION_PUSH_REPORT_FOR_PUB, publicationReport);
                irReportFromPush.PublicationID = pushObject.PublicationID;
                FooDoNetSQLExecuterAsync sqlExecuterAsync1 = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                sqlExecuterAsync1.execute(irReportFromPush);
                break;
            case PushObject.PUSH_OBJECT_VALUE_REG:
                String basePath1 = getString(R.string.server_base_url);
                String subPath1 = getString(R.string.server_edit_publication_path).replace("{0}", String.valueOf(pushObject.ID));
                long publicationID = (long)pushObject.ID;
                HttpServerConnectorAsync connectorAsync1 = new HttpServerConnectorAsync(basePath1, (IFooDoNetServerCallback) this);
                connectorAsync1.execute(new InternalRequest(InternalRequest.ACTION_PUSH_REG, subPath1),
                                        new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                            getResources().getString(R.string.server_get_registered_for_publications)),
                                        new InternalRequest(InternalRequest.ACTION_PUSH_REG, publicationID));
                break;
        }
    }


    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                if(response.Status == InternalRequest.STATUS_OK){
                    FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                    sqlExecuterAsync.execute(response);
                }
                break;
            case InternalRequest.ACTION_PUSH_REG:
                if(response.Status == InternalRequest.STATUS_OK){
                    FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                    sqlExecuterAsync.execute(response);
                }
                break;
        }
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                if(request.Status == InternalRequest.STATUS_OK)
                    SendNotification(request.publicationForSaving, request.ActionCommand);
                    //PublishNotificationNewPublication(request.publicationForSaving);
                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                if(request.Status == InternalRequest.STATUS_OK)
                    SendNotification(request.publicationForSaving, request.ActionCommand);
                    //PublishNotificationPublicationDeleted(request.publicationForSaving.getTitle());
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                if(request.Status == InternalRequest.STATUS_OK)
                    SendNotification(request.publicationForSaving, request.ActionCommand);
                    //PublishNotificationForNewReport(request.publicationForSaving.getTitle(), pushObject.Report);
                break;
            case InternalRequest.ACTION_PUSH_REG:
                if(request.Status == InternalRequest.STATUS_OK)
                    SendNotification(request.publicationForSaving, request.ActionCommand);
                //PublishNot
                break;
        }
    }

    private void SendNotification(FCPublication publication, int action) {
        Intent intent = new Intent();
        String title = publication.getTitle();
        String message = "";

        switch (action){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                message = "New publication near you";

                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                intent = new Intent(this, SplashScreenActivity.class);
                message = "Publication was deleted";
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                intent.putExtra(PUBLICATION_NUMBER, pushObject.Report);
                message = "Report for publication";
            case InternalRequest.ACTION_PUSH_REG:
                intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                message = "New user was registered to publication";

                break;
        }

        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.side_menu_collect_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

//    private void PublishNotificationNewPublication(FCPublication publication)
//    {
//        sendNotification(publication.getUniqueId(), publication.getTitle(), "New publication near you");
//    }
//
//    private void PublishNotificationPublicationDeleted(String pubTitle)
//    {
//        sendNotification(-1, pubTitle, "Publication was deleted");
//    }
//
//    private void PublishNotificationForNewReport(String pubTitle, int reportTypeID)
//    {
//        sendNotification(-1, pubTitle, reportTypeID, "Report for publication");
//    }
}
