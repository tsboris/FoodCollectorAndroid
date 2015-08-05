package upp.foodonet;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;


public class SplashScreenActivity  extends FooDoNetCustomActivityConnectedToService implements LoaderManager.LoaderCallbacks<Cursor> {
    private int min_splash_screen_duration;
    ArrayList<FCPublication> publicationsFromDB, publicationsFromServer, publicationsUpdatedList;
    boolean flagWaitTaskFinished, flagSQLLoaderFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        min_splash_screen_duration = getResources().getInteger(R.integer.min_splash_screen_time);
        Point p = new Point(min_splash_screen_duration, 0);
        SplashScreenHolder ssh = new SplashScreenHolder();
        ssh.execute(p);
        /*HttpServerConnecterAsync connecter = new HttpServerConnecterAsync(getResources().getString(R.string.server_base_url), this);
        connecter.execute(new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS));*/
        getSupportLoaderManager().initLoader(0, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = FCPublication.GetColumnNamesArray();
        android.support.v4.content.CursorLoader cursorLoader
                = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null) {
            Log.e("myTag", "cursor null");
            return;
        }

        publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(data);

        flagSQLLoaderFinished = true;

        if(AllLoaded())
            StartNextActivity();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void OnNotifiedToFetchData() {
        Log.i("food", "mainAct callback called");
    }

    @Override
    public void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList) {
        Log.i("food", "LoadUpdatedListOfPublications mainActivity");
    }

    /*
    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        publicationsFromServer = new ArrayList<FCPublication>();
        publicationsFromServer.addAll(response.publications);
        flagHttpGetFinished = true;
        CompareAndUpdateLocalAndServer();
        //if(AllLoaded())
            //StartNextActivity();
    }

    @Override
    public void OnUpdateLocalDBComplete(ArrayList<FCPublication> publications) {
        publicationsUpdatedList = publications;
        flagSQLLoaderFinished = true;
        if(AllLoaded())
            StartNextActivity();
    }
    */

    private class SplashScreenHolder extends AsyncTask<Point, Void, Void> {


        @Override
        protected Void doInBackground(Point... params) {
            try {
                Thread.sleep(1000 * params[0].x, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            flagWaitTaskFinished = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(AllLoaded())
                StartNextActivity();
        }
    }

    private void StartNextActivity(){
/*
        Intent intent = new Intent(this, SqlLoaderResultActivity.class);
        //publicationsFromDB.addAll(publicationsFromServer);
        intent.putExtra("loaderResult", publicationsUpdatedList);
        this.startActivity(intent);
*/
    }



    private boolean AllLoaded(){
        return flagSQLLoaderFinished && flagWaitTaskFinished;
    }}
