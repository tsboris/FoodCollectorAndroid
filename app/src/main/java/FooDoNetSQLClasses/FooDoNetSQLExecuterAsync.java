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
                cursor.close();
                cursor = null;
                cursor = contentResolver.query(FooDoNetSQLProvider.URI_GET_ALL_REGS, RegisteredUserForPublication.GetColumnNamesArray(), null, null, null);
                ArrayList<RegisteredUserForPublication> regs = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(cursor);
                resultPublications = new ArrayList<FCPublication>();

                for (FCPublication publicationFromServer : publicationsFromServer) {
                    resultPublications.add(publicationFromServer);
                    FCPublication pubFromDB = FCPublication.GetPublicationFromArrayListByID(publicationsFromDB, publicationFromServer.getUniqueId());
                    if (pubFromDB == null) {
                        InsertPublicationToDB(contentResolver, publicationFromServer);
                    } else {
                        if (pubFromDB.getVersion() < publicationFromServer.getVersion()) {
                            DeletePublicationFromDB(contentResolver, pubFromDB);
                            InsertPublicationToDB(contentResolver, publicationFromServer);
                        }
                        publicationsFromDB.remove(pubFromDB);
                    }
                }
                ArrayList<Integer> toRemoveFromDB = new ArrayList<Integer>();
                for (FCPublication publicationFromDB : publicationsFromDB) {
                    if (publicationFromDB.getUniqueId() == 0) {

                    } else {
                        toRemoveFromDB.add(publicationFromDB.getUniqueId());
                        DeletePublicationFromDB(contentResolver, publicationFromDB);
                    }
                }
                for (Integer i : toRemoveFromDB) {
                    FCPublication.DeletePublicationFromCollectionByID(publicationsFromDB, i);
                }
                return null;
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:
                Cursor publicationsForListCursor
                        = contentResolver.query(FooDoNetSQLProvider.URI_GET_ALL_PUBS_FOR_LIST_ID_DESC,
                        FCPublication.GetColumnNamesForListArray(), null, null, null);
                publicationsForList = FCPublication.GetArrayListOfPublicationsFromCursor(publicationsForListCursor, true);
                break;
        }
        return null;
    }

    private void InsertPublicationToDB(ContentResolver contentResolver, FCPublication publication){
        contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publication.GetContentValuesRow());
        for(RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
    }

    private void DeletePublicationFromDB(ContentResolver contentResolver, FCPublication publication){
        for(RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_FOR_PUBLICATION + "/" + reg.getId()), null, null);
        contentResolver.delete(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + publication.getUniqueId()), null, null);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switch (incomingRequest.ActionCommand) {
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                callbackHandler.OnUpdateLocalDBComplete(resultPublications);
                return;
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:

        }
    }
}
