package upp.foodonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import CommonUtilPackage.InternalRequest;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
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
public class RegisterUnregisterReportService
        extends IntentService
        implements IFooDoNetServerCallback, IFooDoNetSQLCallback {

    private static final String MY_TAG = "food_regUnregReport";

    private static final String ACTION_REGISTER_TO_PUBLICATION = "action.resiter.to.pub";
    private static final String ACTION_UNREGISTER_FROM_PUBLICATION = "action.unresiter.from.pub";
    private static final String ACTION_REPORT_TO_PUBLICATION = "action.report.to.pub";

    private static final String EXTRA_PARAM_REG_TO_PUB_DATA = "extra.resiter.to.pub";
    private static final String EXTRA_PARAM_UNREG_FROM_PUB_DATA = "extra.unregister.from.pub";
    private static final String EXTRA_PARAM_REPORT_TO_PUB_DATA = "extra.report.to.pub";

    RegisteredUserForPublication myRegistrationToPublication;

    public static void startActionRegisterToPub(Context context, RegisteredUserForPublication registrationData) {
        Intent intent = new Intent(context, RegisterUnregisterReportService.class);
        intent.setAction(ACTION_REGISTER_TO_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_REG_TO_PUB_DATA, registrationData);
        context.startService(intent);
    }

    public static void startActionUnRegisterFromPub(Context context, RegisteredUserForPublication registrationData) {
        Intent intent = new Intent(context, RegisterUnregisterReportService.class);
        intent.setAction(ACTION_UNREGISTER_FROM_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_REG_TO_PUB_DATA, registrationData);
        context.startService(intent);
    }

    public static void startActionReportForPublication(Context context, PublicationReport reportData) {
        Intent intent = new Intent(context, RegisterUnregisterReportService.class);
        intent.setAction(ACTION_REPORT_TO_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_REPORT_TO_PUB_DATA, reportData);
        context.startService(intent);
    }

    public RegisterUnregisterReportService() {
        super("RegisterUnregisterReportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action){
                case ACTION_REGISTER_TO_PUBLICATION:
                    myRegistrationToPublication
                            = (RegisteredUserForPublication) intent.getSerializableExtra(EXTRA_PARAM_REG_TO_PUB_DATA);
                    if(myRegistrationToPublication == null){
                        Log.e(MY_TAG, "no registeredUserForPublication found in extras");
                        ReportFail(action);
                        return;
                    }
                    handleActionRegisterToPublication(myRegistrationToPublication);
                    break;
                case ACTION_REPORT_TO_PUBLICATION:
                    break;
                case ACTION_UNREGISTER_FROM_PUBLICATION:
                    myRegistrationToPublication
                            = (RegisteredUserForPublication) intent.getSerializableExtra(EXTRA_PARAM_REG_TO_PUB_DATA);
                    if(myRegistrationToPublication == null){
                        Log.e(MY_TAG, "no registeredUserForPublication found in extras");
                        ReportFail(action);
                        return;
                    }
                    handleActionUnregisterFromPublication(myRegistrationToPublication);
                    break;
            }
        }
    }

    private void ReportFail(String action){
        Intent intentResponse = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
        switch (action){
            case ACTION_REGISTER_TO_PUBLICATION:
                Log.e(MY_TAG, "no registeredUserForPublication found in extras");
                intentResponse.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL);
                break;
        }
        sendBroadcast(intentResponse);
    }

    private void handleActionRegisterToPublication(RegisteredUserForPublication rufp) {
        String subPath = getResources().getString(R.string.server_post_register_to_publication);
        subPath = subPath.replace("{0}", String.valueOf(rufp.getPublication_id()));
        HttpServerConnectorAsync connector
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
        InternalRequest ir
                = new InternalRequest(InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION, subPath);
        ir.canWriteSelfToJSONWriterObject = rufp;
        ir.myRegisterToPublication = rufp;
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);

    }

    private void handleActionUnregisterFromPublication(RegisteredUserForPublication rufp){
        //if(true) return;
        String subPath = getResources().getString(R.string.server_post_unregister_from_publication);
        subPath = subPath.replace("{0}", String.valueOf(rufp.getPublication_id()));
        HttpServerConnectorAsync connector
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
        InternalRequest ir
                = new InternalRequest(InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION, subPath);
        ir.canWriteSelfToJSONWriterObject = rufp;
        ir.myRegisterToPublication = rufp;
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }


    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand){
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                if(response.Status != InternalRequest.STATUS_OK){
                    Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                            ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL);
                    sendBroadcast(intent);
                    return;
                }
                FooDoNetSQLExecuterAsync sqlExecutor = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB);
                ir.myRegisterToPublication = myRegistrationToPublication;
                sqlExecutor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                if(response.Status != InternalRequest.STATUS_OK){
                    Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                            ServicesBroadcastReceiver.ACTION_CODE_UNREGISTER_FROM_PUBLICATION_FAIL);
                    sendBroadcast(intent);
                    return;
                }
                FooDoNetSQLExecuterAsync sqlExecuter = new FooDoNetSQLExecuterAsync(this, getContentResolver());
                InternalRequest irUnreg = new InternalRequest(InternalRequest.ACTION_SQL_REMOVE_MYSELF_FROM_REGISTERED_TO_PUB);
                irUnreg.myRegisterToPublication = myRegistrationToPublication;
                sqlExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irUnreg);
                break;
            default:
                Log.e(MY_TAG, "unexpected callback received from server");
                break;
        }
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB:
                switch (request.Status){
                    case InternalRequest.STATUS_OK:
                        Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                        intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                                ServicesBroadcastReceiver.ACTION_CODE_ADD_MYSELF_TO_REGS_FOR_PUBLICATION);
                        sendBroadcast(intent);
                        break;
                    default:
                        break;
                }
                break;
            case InternalRequest.ACTION_SQL_REMOVE_MYSELF_FROM_REGISTERED_TO_PUB:
                if(request.Status == InternalRequest.STATUS_OK){
                    Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                            ServicesBroadcastReceiver.ACTION_CODE_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION);
                    sendBroadcast(intent);
                }
                break;
        }
    }
}
