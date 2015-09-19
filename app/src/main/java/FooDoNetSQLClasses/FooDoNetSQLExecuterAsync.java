package FooDoNetSQLClasses;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import DataModel.FCPublication;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import CommonUtilPackage.InternalRequest;
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
    FCPublication publicationAfterUpdate;
    FCPublication publicationDetailsByID;
    IFooDoNetSQLCallback callbackHandler;
    ContentResolver contentResolver;
    InternalRequest incomingRequest;
    int newNegativeID;
    Map<Integer, Integer> needToLoadPicturesFor;

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
                needToLoadPicturesFor = new HashMap<>();
                if (publicationsFromServer == null || publicationsFromServer.size() == 0
                        || contentResolver == null || callbackHandler == null)
                    return null;
                Cursor cursor = contentResolver.query(FooDoNetSQLProvider.CONTENT_URI,
                        FCPublication.GetColumnNamesArray(), null, null, null);
                publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(cursor, false);
                cursor.close();
                cursor = null;
                cursor = contentResolver.query(FooDoNetSQLProvider.URI_GET_ALL_REGS,
                        RegisteredUserForPublication.GetColumnNamesArray(), null, null, null);
                ArrayList<RegisteredUserForPublication> regs
                        = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(cursor);
                cursor.close();
                resultPublications = new ArrayList<FCPublication>();
                for (FCPublication publicationFromServer : publicationsFromServer) {
                    resultPublications.add(publicationFromServer);
                    FCPublication pubFromDB
                            = FCPublication.GetPublicationFromArrayListByID(publicationsFromDB, publicationFromServer.getUniqueId());
                    if (pubFromDB == null) {
                        InsertPublicationToDB(contentResolver, publicationFromServer);
                        needToLoadPicturesFor.put(publicationFromServer.getUniqueId(), publicationFromServer.getVersion());
                    } else {
                        if (pubFromDB.getVersion() < publicationFromServer.getVersion()) {
                            DeletePublicationFromDB(contentResolver, pubFromDB);
                            InsertPublicationToDB(contentResolver, publicationFromServer);
                            needToLoadPicturesFor.put(publicationFromServer.getUniqueId(), publicationFromServer.getVersion());
                        } else {
                            UpdateRegsAndReports(contentResolver, publicationFromServer);
                        }
                        publicationsFromDB.remove(pubFromDB);
                    }
                }
                ArrayList<Integer> toRemoveFromDB = new ArrayList<Integer>();
                for (FCPublication publicationFromDB : publicationsFromDB) {
                    if (publicationFromDB.getUniqueId() <= 0) {

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
                if (params[0].publicationForSaving == null) {
                    Log.e(MY_TAG, "got null publication for saving");
                    return null;
                }
                newPublicationForSaving = params[0].publicationForSaving;
                if (params[0].publicationForSaving.getUniqueId() == 0) {
                    Cursor newNegativeIDCursor = contentResolver.query(FooDoNetSQLProvider.URI_GET_NEW_NEGATIVE_ID,
                            new String[]{FCPublication.PUBLICATION_NEW_NEGATIVE_ID}, null, null, null);
                    if (newNegativeIDCursor.moveToFirst()) {
                        newNegativeID
                                = newNegativeIDCursor.getInt(
                                newNegativeIDCursor.getColumnIndex(FCPublication.PUBLICATION_NEW_NEGATIVE_ID));
                        if (newNegativeID > 0)
                            newNegativeID = 0;
                        newPublicationForSaving.setUniqueId(newNegativeID);
                    } else {
                        Log.e(MY_TAG, "cant get data from cursor, moveToFirst() == false");
                    }
                    newNegativeIDCursor.close();
                }
                Uri newRowURLForLog
                        = contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, newPublicationForSaving.GetContentValuesRow());
                newPublicationForSaving.setUniqueId(Integer.parseInt(newRowURLForLog.getLastPathSegment()));
                Log.i(MY_TAG, "insert succeeded! id: " + newPublicationForSaving.getUniqueId()
                        + "; new row url: " + newRowURLForLog);
                break;
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                Uri getPubUri = params[0].PublicationID < 0
                        ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE
                        : FooDoNetSQLProvider.CONTENT_URI;
                long pubID = params[0].PublicationID < 0 ? params[0].PublicationID * -1 : params[0].PublicationID;
                Cursor cPub = contentResolver.query(Uri.parse(getPubUri + "/" + pubID),
                        FCPublication.GetColumnNamesArray(), null, null, null);
                ArrayList<FCPublication> pubs = FCPublication.GetArrayListOfPublicationsFromCursor(cPub, false);
                cPub.close();
                if (pubs == null || pubs.size() == 0) {
                    Log.e(MY_TAG, "can't get publication from sql by id: " + params[0].PublicationID);
                    return null;
                }
                FCPublication resultPublication = pubs.get(0);

                Uri getRegUri = params[0].PublicationID < 0
                        ? FooDoNetSQLProvider.URI_GET_REGISTERED_BY_PUBLICATION_NEG_ID
                        : FooDoNetSQLProvider.URI_GET_REGISTERED_BY_PUBLICATION_ID;
                Cursor cRegs = contentResolver.query(Uri.parse(getRegUri + "/" + pubID),
                        RegisteredUserForPublication.GetColumnNamesArray(), null, null, null);
                ArrayList<RegisteredUserForPublication> regsById
                        = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(cRegs);
                cRegs.close();
                if (regsById != null && regsById.size() > 0)
                    resultPublication.setRegisteredForThisPublication(regsById);

                Uri getRepUri = params[0].PublicationID < 0
                        ? FooDoNetSQLProvider.URI_GET_ALL_REPORTS_BY_PUB_ID
                        : FooDoNetSQLProvider.URI_GET_ALL_REPORTS_BY_PUB_NEG_ID;
                Cursor cReports = contentResolver.query(Uri.parse(getRepUri + "/" + pubID),
                        PublicationReport.GetColumnNamesArray(), null, null, null);
                ArrayList<PublicationReport> reports
                        = PublicationReport.GetArrayListOfPublicationReportsFromCursor(cReports);
                cReports.close();
                if (reports != null && reports.size() > 0)
                    resultPublication.setPublicationReports(reports);
                publicationDetailsByID = resultPublication;
                break;
            case InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
                Uri getUpdateUri = params[0].publicationForSaving.getUniqueId() < 0
                        ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE
                        : FooDoNetSQLProvider.CONTENT_URI;
                int tmpIdToDelete = params[0].publicationForSaving.getUniqueId();
                long pubIdToUpdate = params[0].publicationForSaving.getUniqueId() < 0
                        ? params[0].publicationForSaving.getUniqueId() * -1
                        : params[0].publicationForSaving.getUniqueId();
                FCPublication publication = params[0].publicationForSaving;
                publication.setUniqueId(publication.getNewIdFromServer());
                publication.setVersion(publication.getVersionFromServer());
                int rowsUpdated = contentResolver.update(Uri.parse(getUpdateUri + "/" + pubIdToUpdate), publication.GetContentValuesRow(), null, null);
                if (rowsUpdated > 0)
                    Log.i(MY_TAG, "successfully updated new id and version");
                rowsUpdated = 0;
                publicationAfterUpdate = publication;
/*  No need to delete - update works fine
                Uri deleteUri = tmpIdToDelete < 0 ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE:FooDoNetSQLProvider.CONTENT_URI;
                int idToDelete = tmpIdToDelete < 0 ? tmpIdToDelete * -1 : tmpIdToDelete;
                rowsUpdated = contentResolver.delete(Uri.parse(deleteUri + "/" + idToDelete), null, null);
                if(rowsUpdated > 0)
                    Log.i(MY_TAG, "successfully deleted rows: " + rowsUpdated);
*/
                //throw new UnsupportedOperationException("yet implemented updating id");
                break;
            case InternalRequest.ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS:
                Map<Integer, byte[]> imgToPub = params[0].publicationImageMap;
                ContentValues cv = new ContentValues();
                for (int pubId : imgToPub.keySet()) {
                    cv.put(String.valueOf(pubId), imgToPub.get(pubId));
                }
                int rowsAffected = contentResolver.update(FooDoNetSQLProvider.URI_UPDATE_IMAGES, cv, null, null);
                break;
            case InternalRequest.ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB:
                if(params[0].myRegisterToPublication == null){
                    Log.e(MY_TAG, "no data in myRegisterToPublication");
                    break;
                }
                RegisteredUserForPublication myRegistrationToPublication = params[0].myRegisterToPublication;
                int newNegativeID = 0;
                Cursor negIdCursor = contentResolver
                        .query(FooDoNetSQLProvider.URI_GET_REG_FOR_PUB_NEW_NEG_ID,
                                new String[]{RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID},
                                    null, null, null);
                if (negIdCursor.moveToFirst()) {
                    newNegativeID = negIdCursor.getInt(
                            negIdCursor.getColumnIndex(RegisteredUserForPublication
                                    .REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID));
                    newNegativeID = newNegativeID >= 0 ? -1 : newNegativeID;
                    myRegistrationToPublication.setId(newNegativeID);
                } else {
                    Log.e(MY_TAG, "got no cursor for new pub reg new neg id");
                    return null;
                }
                contentResolver.insert(
                        FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION,
                            myRegistrationToPublication.GetContentValuesRow());
                break;
            case InternalRequest.ACTION_SQL_REMOVE_MYSELF_FROM_REGISTERED_TO_PUB:
                if(params[0].myRegisterToPublication == null){
                    Log.e(MY_TAG, "no data in myRegisterToPublication");
                    break;
                }
                RegisteredUserForPublication myRegistrationToCancel = params[0].myRegisterToPublication;

                break;
        }
        return null;
    }

    private void InsertPublicationToDB(ContentResolver contentResolver, FCPublication publication) {
        contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publication.GetContentValuesRow());
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
        for (PublicationReport pr : publication.getPublicationReports()) {
            contentResolver.insert(FooDoNetSQLProvider.URI_GET_ALL_REPORTS, pr.GetContentValuesRow());
        }
    }

    private void DeletePublicationFromDB(ContentResolver contentResolver, FCPublication publication) {
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_FOR_PUBLICATION + "/" + reg.getId()), null, null);
        for (PublicationReport pr : publication.getPublicationReports())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS + "/" + pr.getId()), null, null);
        Uri deleteUri = publication.getUniqueId() < 0 ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE : FooDoNetSQLProvider.CONTENT_URI;
        int idToDelete = publication.getUniqueId() < 0 ? publication.getUniqueId() * -1 : publication.getUniqueId();
        contentResolver.delete(Uri.parse(deleteUri + "/" + idToDelete), null, null);
    }

    private void UpdateRegsAndReports(ContentResolver contentResolver, FCPublication publication) {
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_FOR_PUBLICATION + "/" + reg.getId()), null, null);
        for (PublicationReport pr : publication.getPublicationReports())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS + "/" + pr.getId()), null, null);
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
        for (PublicationReport pr : publication.getPublicationReports()) {
            contentResolver.insert(FooDoNetSQLProvider.URI_GET_ALL_REPORTS, pr.GetContentValuesRow());
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        InternalRequest ir;
        switch (incomingRequest.ActionCommand) {
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                InternalRequest respond = new InternalRequest(incomingRequest.ActionCommand, resultPublications);
                respond.listOfPubsToFetchImageFor = needToLoadPicturesFor;
                callbackHandler.OnSQLTaskComplete(respond);
                break;
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, resultPublications));
                break;
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                ir = new InternalRequest(incomingRequest.ActionCommand, newPublicationForSaving);
                ir.Status = InternalRequest.STATUS_OK;
                callbackHandler.OnSQLTaskComplete(ir);
                break;
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                ir = new InternalRequest(incomingRequest.ActionCommand);
                ir.publicationForDetails = publicationDetailsByID;
                callbackHandler.OnSQLTaskComplete(ir);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
                InternalRequest response = new InternalRequest(incomingRequest.ActionCommand);
                response.Status = InternalRequest.STATUS_OK;
                response.publicationForSaving = publicationAfterUpdate;
                callbackHandler.OnSQLTaskComplete(response);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS:
                InternalRequest statusResponse = new InternalRequest(incomingRequest.ActionCommand);
                statusResponse.Status = InternalRequest.STATUS_OK;
                callbackHandler.OnSQLTaskComplete(statusResponse);
                break;
            case InternalRequest.ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, true));
                break;
/*  not needed
            case InternalRequest.ACTION_SQL_GET_NEW_NEGATIVE_ID:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, newNegativeID));
                break;
*/
        }
    }
}
