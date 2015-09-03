package upp.foodonet;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.InternalRequest;
import FooDoNetServiceUtil.IFooDoNetCustomServiceBinder;
import FooDoNetServiceUtil.IFooDoNetServiceCallback;

public class FooDoNetService
        extends Service
        implements IFooDoNetServerCallback, IFooDoNetSQLCallback {
    IFooDoNetServiceCallback currentCallbackHandler;

    public final static int ACTION_START = 1;
    public final static int ACTION_WORK_DONE = 2;

    private final IBinder mBinder = new FooDoNetCustomServiceBinder();

    private boolean isAnyoneConnected;
    private boolean mustRun;
    private boolean isStarted;
    public int secondsToWait;

    private int count = 0;

    HttpServerConnectorAsync connecterToServer; //1
    FooDoNetSQLExecuterAsync sqlExecuter; //2
    IFooDoNetServiceCallback curretActivityForCallback; //3
    WaiterForScheduler waiter; //4
    final int taskServer = 1;
    final int taskSQL = 2;
    final int taskActivity = 3;
    final int taskWaiter = 4;

    int currentIndexInWorkPlan;
    int[] workPlan = new int[]{1,2,3,4};

    private final String MY_TAG = "food_SchedulerService";
    private String serverBaseUrl;

    //Handler callbackHandler;
    Messenger callbackMessenger;

    ArrayList<FCPublication> fetchedFromServer, loadedFromSQL;

    public FooDoNetService() {
    }

    @Override
    public void onCreate() {
        Log.i(MY_TAG, "creating service...");
        mustRun = true;
        secondsToWait = getResources().getInteger(R.integer.fetch_data_scheduler_repeat_time);
        serverBaseUrl = getResources().getString(R.string.server_base_url);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(MY_TAG, "destroing service");
        mustRun = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return 0;
    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_START:
                    callbackMessenger = msg.replyTo;
                    //callbackHandler = msg.getTarget();
                    StartScheduler();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(MY_TAG, "service binded");
        isAnyoneConnected = true;
        return mMessenger.getBinder();
        //return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(MY_TAG, "service unbinded");
        isAnyoneConnected = false;
        onDestroy();
        return super.onUnbind(intent);
    }

    private void DoNextTaskFromWorkPlan() {
        if(true)return;
        if(!mustRun) return;
        Log.i(MY_TAG, "DoingNextTaskFromPlan");
        switch (workPlan[currentIndexInWorkPlan]){
            case taskServer:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                connecterToServer = new HttpServerConnectorAsync(serverBaseUrl, this);
                connecterToServer.execute( new InternalRequest[]{
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS,
                                getResources().getString(R.string.server_get_publications_path)),
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                getResources().getString(R.string.server_get_registered_for_publications)),
                        new InternalRequest(InternalRequest.ACTION_GET_PUBLICATION_REPORTS,
                                getResources().getString(R.string.server_get_publication_report))});
                break;
            case taskSQL:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                sqlExecuter = new FooDoNetSQLExecuterAsync( this, getContentResolver());//fetchedFromServer,
                sqlExecuter.execute(
                        new InternalRequest(InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER, fetchedFromServer, null));
                break;
            case taskActivity:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                //currentCallbackHandler.LoadUpdatedListOfPublications(loadedFromSQL);
                Message m = Message.obtain(null, ACTION_WORK_DONE, loadedFromSQL);
                try {
                    callbackMessenger.send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //callbackHandler.dispatchMessage(m);
                currentIndexInWorkPlan = getNextIndex(currentIndexInWorkPlan, workPlan);
                DoNextTaskFromWorkPlan();
                return;
            case taskWaiter:
                Log.i(MY_TAG, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                waiter = new WaiterForScheduler(finishedSleepingHandler);
                waiter.execute(secondsToWait);
                break;
            default:
                throw new IllegalArgumentException("no such task: " + workPlan[currentIndexInWorkPlan]);
        }
        currentIndexInWorkPlan = getNextIndex(currentIndexInWorkPlan, workPlan);
    }

    private int getNextIndex(int current, int[] array){
        return (current + 1 == array.length? 0: ++current);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        Log.i(MY_TAG, "finished task server");
        fetchedFromServer = response.publications;
        DoNextTaskFromWorkPlan();
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        if(request.ActionCommand != InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER){
            Log.e(MY_TAG,"Unexpected action code!!");
            return;
        }
        Log.i(MY_TAG, "finished task sql");
        loadedFromSQL = request.publications;
        DoNextTaskFromWorkPlan();
    }

    public class FooDoNetCustomServiceBinder extends Binder implements IFooDoNetCustomServiceBinder {
        @Override
        public void AttachToService(IFooDoNetServiceCallback callback) {
            Log.i("food", "AttachToService called");
            isAnyoneConnected = true;
        }

        public FooDoNetService getService() {
            return FooDoNetService.this;
        }
    }

    public void StartScheduler(){//IFooDoNetServiceCallback callback) {
        //currentCallbackHandler = callback;
        currentIndexInWorkPlan = 0;
        DoNextTaskFromWorkPlan();
        Log.i("food", "starting scheduler...");
    }

    Handler finishedSleepingHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    DoNextTaskFromWorkPlan();
                    break;
                default:
                    Log.e(MY_TAG, "Handler got unexpected msg.what");
                    break;
            }
        }
    };

    private class WaiterForScheduler extends AsyncTask<Integer, Void, Void>{

        Handler callbackHandler;

        public WaiterForScheduler(Handler handler){
            callbackHandler = handler;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            if (params.length == 0 || params[0] <= 0)
                throw new IllegalArgumentException("service scheduler got no time param");
            final int secsToSleep = params[0];
                Log.i("food", "scheduler sleeps...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(secsToSleep);
                            callbackHandler.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("food", "finished sleeping");
            //callbackHandler.sendEmptyMessage(0);
            //DoNextTaskFromWorkPlan();
        }
    }
}
