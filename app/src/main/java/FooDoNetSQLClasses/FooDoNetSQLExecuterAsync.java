package FooDoNetSQLClasses;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;

import DataModel.FCPublication;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetServerClasses.InternalRequest;
import upp.foodonet.FooDoNetSQLProvider;

/**
 * Created by Asher on 24-Jul-15.
 */
public class FooDoNetSQLExecuterAsync extends AsyncTask<InternalRequest, Void, Void> {

    private static final String MY_TAG = "food_sqlAsyncTask";

    ArrayList<FCPublication> publicationsFromServer;
    ArrayList<FCPublication> publicationsFromDB;
    ArrayList<FCPublication> resultPublications;
    ArrayList<FCPublication> publicationsForList;
    ArrayList<RegisteredUserForPublication> regUsersFromServer;
    ArrayList<RegisteredUserForPublication> regUsersFromDB;
    ArrayList<RegisteredUserForPublication> resultRegUsers;
    FCPublication newPublicationForSaving;
    IFooDoNetSQLCallback callbackHandler;
    ContentResolver contentResolver;
    InternalRequest incomingRequest;
    int newNegativeID;

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
                cursor.close();
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
                publicationsForListCursor.close();
                break;
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                if(params[0].publicationForSaving == null){
                    Log.e(MY_TAG, "got null publication for saving");
                    return null;
                }
                newPublicationForSaving = params[0].publicationForSaving;
                if(params[0].publicationForSaving.getUniqueId() == 0){
                    Cursor newNegativeIDCursor = contentResolver.query(FooDoNetSQLProvider.URI_GET_NEW_NEGATIVE_ID,
                            new String[] { FCPublication.PUBLICATION_NEW_NEGATIVE_ID }, null, null, null);
                    if(newNegativeIDCursor.moveToFirst()){
                        newNegativeID
                                = newNegativeIDCursor.getInt(
                                        newNegativeIDCursor.getColumnIndex(FCPublication.PUBLICATION_NEW_NEGATIVE_ID));
                        if(newNegativeID>0)
                            newNegativeID = 0;
                        newPublicationForSaving.setUniqueId(newNegativeID);
                    } else {
                        Log.e(MY_TAG, "cant get data from cursor, moveToFirst() == false");
                    }
                    newNegativeIDCursor.close();
                }
                Uri newRowURLForLog
                        = contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, newPublicationForSaving.GetContentValuesRow());
                Log.i(MY_TAG, "insert succeeded! id: " + newPublicationForSaving.getUniqueId()
                                + "; new row url: " + newRowURLForLog );
                break;
        }
        return null;
    }

    private void InsertPublicationToDB(ContentResolver contentResolver, FCPublication publication){
        contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publication.GetContentValuesRow());
        for(RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
        for(PublicationReport pr : publication.getPublicationReports()){
            contentResolver.insert(FooDoNetSQLProvider.URI_GET_ALL_REPORTS, pr.GetContentValuesRow());
        }
    }

    private void DeletePublicationFromDB(ContentResolver contentResolver, FCPublication publication){
        for(RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_FOR_PUBLICATION + "/" + reg.getId()), null, null);
        for(PublicationReport pr : publication.getPublicationReports())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS + "/" + pr.getId()), null, null);
        Uri deleteUri = publication.getUniqueId() < 0 ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE:FooDoNetSQLProvider.CONTENT_URI;
        int idToDelete = publication.getUniqueId() < 0 ? publication.getUniqueId() * -1 : publication.getUniqueId();
        contentResolver.delete(Uri.parse(deleteUri + "/" + idToDelete), null, null);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switch (incomingRequest.ActionCommand) {
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, resultPublications));
                break;
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, newPublicationForSaving));
                break;
/*  not needed
            case InternalRequest.ACTION_SQL_GET_NEW_NEGATIVE_ID:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, newNegativeID));
                break;
*/
        }
    }
}
