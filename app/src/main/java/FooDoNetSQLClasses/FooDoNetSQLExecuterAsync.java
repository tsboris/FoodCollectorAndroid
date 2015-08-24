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
    ArrayList<FCPublication> publicationsForList;
    ArrayList<RegisteredUserForPublication> regUsersFromServer;
    ArrayList<RegisteredUserForPublication> regUsersFromDB;
    ArrayList<RegisteredUserForPublication> resultRegUsers;
    IFooDoNetSQLCallback callbackHandler;
    ContentResolver contentResolver;
    InternalRequest incomingRequest;

    public FooDoNetSQLExecuterAsync(IFooDoNetSQLCallback callback, ContentResolver content) {
        //ArrayList<FCPublication> fromServer removed from parameters
        //publicationsFromServer = fromServer;
        callbackHandler = callback;
        contentResolver = content;
    }


    @Override
    protected Void doInBackground(InternalRequest... params) {
        if (params.length == 0) return null;
        incomingRequest = params[0];
        switch (incomingRequest.ActionCommand) {
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                publicationsFromServer = params[0].publications;
                regUsersFromServer = params[0].registeredUsers;
                if (publicationsFromServer == null || publicationsFromServer.size() == 0
                        || contentResolver == null || callbackHandler == null)
                    return null;
                Cursor cursor = contentResolver.query(FooDoNetSQLProvider.CONTENT_URI, FCPublication.GetColumnNamesArray(), null, null, null);
                publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(cursor, false);
                resultPublications = new ArrayList<FCPublication>();

                for (FCPublication publicationFromServer : publicationsFromServer) {
                    resultPublications.add(publicationFromServer);
                    FCPublication pubFromDB = FCPublication.GetPublicationFromArrayListByID(publicationsFromDB, publicationFromServer.getUniqueId());
                    if (pubFromDB == null) {
                        contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publicationFromServer.GetContentValuesRow());
                    } else {
                        if (pubFromDB.getVersion() < publicationFromServer.getVersion()) {
                            contentResolver.delete(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + pubFromDB.getUniqueId()), null, null);
                            contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publicationFromServer.GetContentValuesRow());
                        }
                        publicationsFromDB.remove(pubFromDB);
                    }
                }
                ArrayList<Integer> toRemoveFromDB = new ArrayList<Integer>();
                for (FCPublication publicationFromDB : publicationsFromDB) {
                    if (publicationFromDB.getUniqueId() == 0) {

                    } else {
                        contentResolver.delete(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + publicationFromDB.getUniqueId()), null, null);
                        toRemoveFromDB.add(publicationFromDB.getUniqueId());
                    }
                }
                for (Integer i : toRemoveFromDB) {
                    FCPublication.DeletePublicationFromCollectionByID(publicationsFromDB, i);
                }
                return null;
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:
                Cursor publicationsForListCursor
                        = contentResolver.query(FooDoNetSQLProvider.URI_GET_ALL_PUBS_FOR_LIST_ID_DESC,
                                                FCPublication.GetColumnNamesForListArray(), null,null,null);
                publicationsForList = FCPublication.GetArrayListOfPublicationsFromCursor(publicationsForListCursor, true);
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switch (incomingRequest.ActionCommand){
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                callbackHandler.OnUpdateLocalDBComplete(resultPublications);
                return;
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:

        }
    }
}
