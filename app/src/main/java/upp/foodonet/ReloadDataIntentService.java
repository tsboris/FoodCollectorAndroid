package upp.foodonet;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.DownloadImageTask;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IDownloadImageCallBack;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

public class ReloadDataIntentService
        extends IntentService
        implements IFooDoNetServerCallback, IFooDoNetSQLCallback, IDownloadImageCallBack {

    private final static String MY_TAG = "food_reloadIntService";

    private final static String ACTION_RELOAD = "action_reload";

    final int taskServer = 1;
    final int taskSQL = 2;
    final int taskImages = 3;
    final int taskActivity = 4;

    int currentIndexInWorkPlan;
    int[] workPlan = new int[]{taskServer, taskSQL, taskImages, taskActivity};

    public int secondsToWait;
    private String serverBaseUrl;

    ArrayList<FCPublication> loadedFromSQL, fetchedFromServer;
    Map<Integer, Integer> needToLoadImages;

    HttpServerConnectorAsync connecterToServer;
    FooDoNetSQLExecuterAsync sqlExecuter;

    private int maxImageWidthHeight = 0;

    @Override
    public void onCreate() {
        Log.i(MY_TAG, "creating reload service...");
        serverBaseUrl = getResources().getString(R.string.server_base_url);
        super.onCreate();
    }

    public static void startActionReloadData(Context context) {
        Intent intent = new Intent(context, ReloadDataIntentService.class);
        intent.setAction(ACTION_RELOAD);
        context.startService(intent);
    }

    public ReloadDataIntentService() {
        super("ReloadDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action){
                case ACTION_RELOAD:
                    handleActionReload();
                    break;
            }
        }
    }

    private void handleActionReload() {
        currentIndexInWorkPlan = 0;
        DoNextTaskFromWorkPlan();
    }

    private void DoNextTaskFromWorkPlan() {
        //if(true)return; //todo: this temporary turns of scheduler
        Log.i(MY_TAG, "DoingNextTaskFromPlan");
        switch (workPlan[currentIndexInWorkPlan]) {
            case taskServer:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                connecterToServer = new HttpServerConnectorAsync(serverBaseUrl, (IFooDoNetServerCallback)this);
                connecterToServer.execute(new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS,
                                            getResources().getString(R.string.server_get_publications_path)),
                                          new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                            getResources().getString(R.string.server_get_registered_for_publications)),
                                          new InternalRequest(InternalRequest.ACTION_GET_PUBLICATION_REPORTS,
                                            getResources().getString(R.string.server_get_publication_report)));
                break;
            case taskSQL:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                sqlExecuter = new FooDoNetSQLExecuterAsync(this, getContentResolver());//fetchedFromServer,
                sqlExecuter.execute(
                        new InternalRequest(InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER, fetchedFromServer, null));
                sqlExecuter = null;
                break;
            case taskImages:
                maxImageWidthHeight = getResources().getInteger(R.integer.max_image_width_height);
                DownloadImageTask imageTask
                        = new DownloadImageTask(this,
                        getResources().getString(R.string.amazon_base_url_for_images), maxImageWidthHeight,
                        getResources().getString(R.string.image_folder_path));
                imageTask.execute(needToLoadImages);
                break;
            case taskActivity:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                moveIndexToNextTaskFromPlan();
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        ServicesBroadcastReceiver.ACTION_CODE_RELOAD_DATA_SUCCESS);
                sendBroadcast(intent);
                return;
            default:
                throw new IllegalArgumentException("no such task: " + workPlan[currentIndexInWorkPlan]);
        }
        moveIndexToNextTaskFromPlan();
    }

    private void moveIndexToNextTaskFromPlan(){
        currentIndexInWorkPlan = (currentIndexInWorkPlan + 1 >= workPlan.length? 0 : ++currentIndexInWorkPlan);
    }


    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        Log.i(MY_TAG, "finished task server");
        fetchedFromServer = response.publications;
        DoNextTaskFromWorkPlan();
    }

    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                Log.i(MY_TAG, "finished task sql");
                loadedFromSQL = request.publications;
                if(request.listOfPubsToFetchImageFor == null || request.listOfPubsToFetchImageFor.size() == 0){
                    moveIndexToNextTaskFromPlan();
                } else {
                    needToLoadImages = request.listOfPubsToFetchImageFor;
                 }
                DoNextTaskFromWorkPlan();
                break;
            case InternalRequest.ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS:
                DoNextTaskFromWorkPlan();
                break;
            default:
                Log.e(MY_TAG, "Unexpected action code!!");
                return;
        }
    }

    @Override
    public void OnImageDownloaded(Map<Integer, byte[]> imagesMap) {
        Log.i(MY_TAG, "downloaded images");
        DoNextTaskFromWorkPlan();
//        FooDoNetSQLExecuterAsync sqlExecuter = new FooDoNetSQLExecuterAsync(this, getContentResolver());
//        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS);
//        ir.publicationImageMap = imagesMap;
//        sqlExecuter.execute(ir);
    }
}
