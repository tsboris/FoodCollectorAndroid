package FooDoNetSQLClasses;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.ArrayList;

import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;
import FooDoNetServerClasses.InternalRequest;
import upp.foodonet.FooDoNetSQLProvider;

/**
 * Created by Asher on 24-Jul-15.
 */
public class FooDoNetSQLExecuterAsync extends AsyncTask<InternalRequest, Void, Void> {

    ArrayList<FCPublication> publicationsFromServer;
    ArrayList<FCPublication> publicationsFromDB;
    ArrayList<FCPublication> resultPublications;
    ArrayList<RegisteredUserForPublication> regUsersFromServer;
    ArrayList<RegisteredUserForPublication> regUsersFromDB;
    ArrayList<RegisteredUserForPublication> resultRegUsers;
    IFooDoNetSQLCallback callbackHandler;
    ContentResolver contentResolver;

    public FooDoNetSQLExecuterAsync(IFooDoNetSQLCallback callback, ContentResolver content){
    //ArrayList<FCPublication> fromServer removed from parameters
        //publicationsFromServer = fromServer;
        callbackHandler = callback;
        contentResolver = content;
    }


    @Override
    protected Void doInBackground(InternalRequest... params) {
        if(params.length == 0) return null;
        publicationsFromServer = params[0].publications;
        regUsersFromServer = params[0].registeredUsers;
        if(publicationsFromServer == null || publicationsFromServer.size() == 0
                || contentResolver == null || callbackHandler == null)
            return null;
        Cursor cursor = contentResolver.query(FooDoNetSQLProvider.CONTENT_URI, FCPublication.GetColumnNamesArray(), null, null, null);
        publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(cursor);
        resultPublications = new ArrayList<FCPublication>();

        for(FCPublication publicationFromServer : publicationsFromServer){
            resultPublications.add(publicationFromServer);
            FCPublication pubFromDB = FCPublication.GetPublicationFromArrayListByID(publicationsFromDB, publicationFromServer.getUniqueId());
            if(pubFromDB == null) {
                contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publicationFromServer.GetContentValuesRow());
            }
            else {
                if (pubFromDB.getVersion() < publicationFromServer.getVersion()) {
                    contentResolver.delete(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + pubFromDB.getUniqueId()), null, null);
                    contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publicationFromServer.GetContentValuesRow());
                }
                publicationsFromDB.remove(pubFromDB);
            }
        }
        ArrayList<Integer> toRemoveFromDB = new ArrayList<Integer>();
        for(FCPublication publicationFromDB : publicationsFromDB){
            if(publicationFromDB.getUniqueId() == 0) {

            }
            else {
                contentResolver.delete(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + publicationFromDB.getUniqueId()), null, null);
                toRemoveFromDB.add(publicationFromDB.getUniqueId());
            }
        }
        for(Integer i : toRemoveFromDB){
            FCPublication.DeletePublicationFromCollectionByID(publicationsFromDB, i);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callbackHandler.OnUpdateLocalDBComplete(resultPublications);
    }
}
