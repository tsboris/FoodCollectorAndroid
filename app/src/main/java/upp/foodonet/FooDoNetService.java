package upp.foodonet;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnecterAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.InternalRequest;
import FooDoNetServiceUtil.IFooDoNetCustomServiceBinder;
import FooDoNetServiceUtil.IFooDoNetServiceCallback;

public class FooDoNetService
        extends Service
        implements IFooDoNetServerCallback, IFooDoNetSQLCallback {
    IFooDoNetServiceCallback currentCallbackHandler;

    private final IBinder mBinder = new FooDoNetCustomServiceBinder();

    private boolean isAnyoneConnected;
    private boolean mustRun;
    private boolean isStarted;
    public int secondsToWait;

    private int count = 0;

    HttpServerConnecterAsync connecterToServer; //1
    FooDoNetSQLExecuterAsync sqlExecuter; //2
    IFooDoNetServiceCallback curretActivityForCallback; //3
    WaiterForScheduler waiter; //4
    final int taskServer = 1;
    final int taskSQL = 2;
    final int taskActivity = 3;
    final int taskWaiter = 4;

    int currentIndexInWorkPlan;
    int[] workPlan = new int[]{1,2,3,4};

    private final String myTag = "foodService";
    private String serverBaseUrl;

    ArrayList<FCPublication> fetchedFromServer, loadedFromSQL;

    public FooDoNetService() {
    }

    @Override
    public void onCreate() {
        Log.i("food", "creating service...");
        mustRun = true;
        secondsToWait = getResources().getInteger(R.integer.fetch_data_scheduler_repeat_time);
        serverBaseUrl = getResources().getString(R.string.server_base_url);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("food", "destroing service");
        mustRun = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("food", "service binded");
        isAnyoneConnected = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("food", "service unbinded");
        isAnyoneConnected = false;
        return super.onUnbind(intent);
    }

    private void DoNextTaskFromWorkPlan(){
        Log.i(myTag, "DoingNextTaskFromPlan");
        switch (workPlan[currentIndexInWorkPlan]){
            case taskServer:
                Log.i(myTag, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                connecterToServer = new HttpServerConnecterAsync(serverBaseUrl, this);
                connecterToServer.execute(
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS,
                                getResources().getString(R.string.server_get_publications_path)));
                break;
            case taskSQL:
                Log.i(myTag, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                sqlExecuter = new FooDoNetSQLExecuterAsync(fetchedFromServer, this, getContentResolver());
                sqlExecuter.execute();
                break;
            case taskActivity:
                Log.i(myTag, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                currentCallbackHandler.LoadUpdatedListOfPublications(loadedFromSQL);
                currentIndexInWorkPlan = getNextIndex(currentIndexInWorkPlan, workPlan);
                DoNextTaskFromWorkPlan();
                return;
            case taskWaiter:
                Log.i(myTag, "Perfoming task " + workPlan[currentIndexInWorkPlan]);
                waiter = new WaiterForScheduler();
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
        Log.i(myTag, "finished task server");
        fetchedFromServer = response.publications;
        DoNextTaskFromWorkPlan();
    }

    @Override
    public void OnUpdateLocalDBComplete(ArrayList<FCPublication> publications) {
        Log.i(myTag, "finished task sql");
        loadedFromSQL = publications;
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

    public void StartScheduler(IFooDoNetServiceCallback callback) {
        currentCallbackHandler = callback;
        currentIndexInWorkPlan = 0;
        DoNextTaskFromWorkPlan();
        Log.i("food", "starting scheduler...");
    }

    private class WaiterForScheduler extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... params) {
            if (params.length == 0 || params[0] <= 0)
                throw new IllegalArgumentException("service scheduler got no time param");
            try {
                Log.i("food", "scheduler sleeps...");
                Thread.sleep(params[0] * 1000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("food", "finished sleeping");
            DoNextTaskFromWorkPlan();
        }
    }
}
