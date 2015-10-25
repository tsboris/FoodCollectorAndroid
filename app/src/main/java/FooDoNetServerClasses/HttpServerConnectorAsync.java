package FooDoNetServerClasses;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.JsonWriter;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.ICanWriteSelfToJSONWriter;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import DataModel.UserRegisterData;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

/**
 * Created by Asher on 21-Jul-15.
 */
public class HttpServerConnectorAsync extends AsyncTask<InternalRequest, Void, String> {

    private static final String MY_TAG = "food_httpConnecterAsync";

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String REQUEST_METHOD_GET = "GET";
    private static final String REQUEST_METHOD_DELETE = "DELETE";
    private static final String REQUEST_METHOD_PUT = "PUT";

    private String baseUrl;
    private IFooDoNetServerCallback callbackListener;
    private Context context;

    private String responseString;
    private JSONArray responseJSONArray;
    private JSONObject responseJSONObject;
    private InternalRequest internalResponse;
    private ArrayList<FCPublication> resultPublications;
    private FCPublication publicationForSaving;
    private RegisteredUserForPublication registrationToPublicationToPost;
    private PublicationReport publicationReport;

    private int Action_Command_ID;

    private boolean isSuccess = false;

    public HttpServerConnectorAsync(String baseUrl, IFooDoNetServerCallback callbackListener) {
        this.baseUrl = baseUrl;
        this.callbackListener = callbackListener;
    }

    public HttpServerConnectorAsync(String baseUrl, Context context) {
        this.baseUrl = baseUrl;
        this.context = context;
    }

    public void setContextForBroadcasting(Context context) {
        this.context = context;
    }

    public void executeSync(InternalRequest... params) {
        switch (params[0].ActionCommand) {
            case InternalRequest.ACTION_POST_REGISTER:
                break;
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                doInBackground(params);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        isSuccess ? ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_SUCCESS
                                : ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL);
                context.sendBroadcast(intent);
                break;
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                break;
        }
    }

    @Override
    protected String doInBackground(InternalRequest... params) {
        if (params.length == 0 || params[0] == null)
            return "";
        Action_Command_ID = params[0].ActionCommand;
        String server_sub_path = params[0].ServerSubPath;
        switch (Action_Command_ID) {
            //region case get all pubs
            case InternalRequest.ACTION_GET_ALL_PUBLICATIONS:
                MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, null, true);
                if(!isSuccess)
                    return "";
                isSuccess = false;
                ArrayList<FCPublication> publications = new ArrayList<>();
                try {
                    publications = FCPublication.GetArrayListOfPublicationsFromJSON(new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseString = "";
                ArrayList<RegisteredUserForPublication> regedUsers = new ArrayList<>();
                ArrayList<PublicationReport> pubReports = new ArrayList<>();
                for (FCPublication pub : publications) {
                    MakeServerRequest(REQUEST_METHOD_GET,
                            params[1].ServerSubPath.replace("{0}",
                                    String.valueOf(pub.getUniqueId())), null, true);
                    if(!isSuccess)
                        return "";
                    isSuccess = false;
                    try {
                        regedUsers = RegisteredUserForPublication.
                                GetArrayListOfRegisteredForPublicationsFromJSON(
                                        new JSONArray(responseString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        responseString = "";
                    }

/*
                    MakeServerRequest(REQUEST_METHOD_GET,
                            params[2].ServerSubPath.replace("{0}",
                                    String.valueOf(pub.getUniqueId())), null, true);
                    try {
                        pubReports = PublicationReport.
                                GetArrayListOfPublicationReportsFromJSON(
                                        new JSONArray(responseString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
*/

                    if (regedUsers.size() > 0) {
                        pub.setRegisteredForThisPublication(regedUsers);
                    }
/*
                    if (pubReports.size() > 0) {
                        pub.setPublicationReports(pubReports);
                    }
*/
                    regedUsers.clear();
                    //pubReports.clear();
                    responseString = "";
                }

                MakeServerRequest(REQUEST_METHOD_GET,
                        params[2].ServerSubPath.replace("{0}",
                                String.valueOf(0)), null, true);
                if(!isSuccess)
                    return "";
                try {
                    pubReports = PublicationReport.
                            GetArrayListOfPublicationReportsFromJSON(
                                    new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (pubReports.size() > 0) {
                    publications = tmpConnectReportsToPublications(publications, pubReports);
                }


                if (publications.size() > 0) {
                    if (resultPublications == null)
                        resultPublications = new ArrayList<>();
                    resultPublications.addAll(publications);
                }
                return "";
            //endregion
            //region case post new publication
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                //MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                //Get(server_sub_path, (FCPublication) params[0].canWriteSelfToJSONWriterObject);
                if (params[0].publicationForSaving == null) {
                    Log.e(MY_TAG, "got null pubForSaving!");
                    return "";
                }
                publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                try {
                    AbstractMap.SimpleEntry<Integer, Integer> responsePair = FCPublication.ParseServerResponseToNewPublication(new JSONObject(responseString));
                    if (responsePair != null) {
                        publicationForSaving.setNewIdFromServer(responsePair.getKey());
                        publicationForSaving.setVersionFromServer(responsePair.getValue());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "";
            //endregion
            //region edit publication
            case InternalRequest.ACTION_PUT_EDIT_PUBLICATION:
                if (params[0].publicationForSaving == null) {
                    Log.e(MY_TAG, "got null pubForSaving!");
                    return "";
                }
                publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseString = "";
                if (jsonObject == null) {
                    Log.e(MY_TAG, "cant transform edit pub response to json");
                    isSuccess = false;
                    return "";
                }
                try {
                    publicationForSaving.setVersion(jsonObject.getInt(FCPublication.PUBLICATION_VERSION_KEY));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "";
            //endregion
            //region case register user to system
            case InternalRequest.ACTION_POST_REGISTER:
                publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, params[0].canWriteSelfToJSONWriterObject, false);
                return "";
            //endregion
            //region case register to publication
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                registrationToPublicationToPost = params[0].myRegisterToPublication;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, registrationToPublicationToPost, false);
                return "";
            //endregion
            //region case unregister from publication
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                registrationToPublicationToPost = params[0].myRegisterToPublication;
                MakeServerRequest(REQUEST_METHOD_DELETE, server_sub_path, registrationToPublicationToPost, false);
//                TestDeleteWithBody(registrationToPublicationToPost, baseUrl + server_sub_path);

                return "";
            //endregion
            //region case report for publication
            case InternalRequest.ACTION_POST_REPORT_FOR_PUBLICATION:
                publicationReport = params[0].publicationReport;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, publicationReport, true);
                return "";
            //endregion
            //region take pub off air
            case InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR:
                publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, publicationForSaving, false);
                return "";
            //endregion
            //region delete publication
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                MakeServerRequest(REQUEST_METHOD_DELETE, server_sub_path, null, false);
                return "";
            //endregion
            default:
                return "";
        }
    }

    private void MakeServerRequest(String requestMethod, String server_sub_path, ICanWriteSelfToJSONWriter writableObject, boolean isForResult) {
        String post_url = baseUrl + server_sub_path;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(post_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(15000);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            //connection.setRequestMethod("GET");
            byte[] bytes = null;
            switch (requestMethod) {
                case REQUEST_METHOD_GET:
                    connection.setRequestProperty("Content-length", "0");
                    connection.setAllowUserInteraction(false);
                    break;
                case REQUEST_METHOD_POST:
                case REQUEST_METHOD_PUT:
                    //connection.setDoInput(true);
                    connection.setDoOutput(true);
                    break;
                case REQUEST_METHOD_DELETE:
                    break;
            }
            //connection.connect();
            if (writableObject != null && requestMethod != REQUEST_METHOD_DELETE) {

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                String s = writableObject.GetJsonObjectForPost().toString();
                String out = null;
                try {
                    out = new String(writableObject.GetJsonObjectForPost().toString().getBytes("UTF-8"), "ISO-8859-1");
                } catch (java.io.UnsupportedEncodingException e) {

                }

                wr.writeBytes(out);
                wr.flush();
                wr.close();
            }
            int connectionResponse = connection.getResponseCode();

            switch (connectionResponse) {
                case HttpURLConnection.HTTP_OK:
                    isSuccess = true;
                    if (isForResult) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        //Log.i(MY_TAG, this.hashCode() + sb.toString());
                        responseString = sb.toString();
                    }
                    break;
                default:
                    isSuccess = false;
                    break;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                    connection = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
        switch (Action_Command_ID) {
            case InternalRequest.ACTION_GET_ALL_PUBLICATIONS:
                InternalRequest irLoadData;
                if(isSuccess) {
                    Log.i(MY_TAG, "data loaded from http, calling callback");
                    irLoadData = new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS, resultPublications, null);
                    irLoadData.Status = InternalRequest.STATUS_OK;
                } else {
                    Log.i(MY_TAG, "failed to load data from server");
                    irLoadData = new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS, false);
                }
                callbackListener.OnServerRespondedCallback(irLoadData);
                break;
            case InternalRequest.ACTION_POST_REGISTER:
                Log.i(MY_TAG, "successfully registered user on server");
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        isSuccess ? ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_SUCCESS
                                : ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_FAIL);
                context.sendBroadcast(intent);
                break;
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                Log.i(MY_TAG, "successfully posted new publication to server");
                callbackListener.OnServerRespondedCallback(
                        new InternalRequest(Action_Command_ID, publicationForSaving));
                break;
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                Log.i(MY_TAG, "register to pub: " + (isSuccess ? "ok" : "fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                Log.i(MY_TAG, "unregister from pub: " + (isSuccess ? "ok" : "fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
            case InternalRequest.ACTION_POST_REPORT_FOR_PUBLICATION:
                Log.i(MY_TAG, "report for publication: " + (isSuccess ? "ok" : "fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
            case InternalRequest.ACTION_PUT_EDIT_PUBLICATION:
                Log.i(MY_TAG, "save edited publication on server: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irEdit = new InternalRequest(Action_Command_ID, isSuccess);
                irEdit.publicationForSaving = publicationForSaving;
                callbackListener.OnServerRespondedCallback(irEdit);
                break;
            case InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR:
                Log.i(MY_TAG, "take publication off air: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irOffAir = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irOffAir);
                break;
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                Log.i(MY_TAG, "delete publication: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irDelete = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irDelete);
                break;
        }
    }

    private ArrayList<FCPublication> tmpConnectReportsToPublications(ArrayList<FCPublication> publications, ArrayList<PublicationReport> reports) {
        //ArrayList<Integer> validReports = new ArrayList<>();
        for (PublicationReport report : reports) {
            int pubId = report.getPublication_id();
            int pubVersion = report.getPublication_version();
            FCPublication pub = FCPublication.GetPublicationFromArrayListByID(publications, pubId);
            if (pub == null) continue;
            if (pub.getVersion() == pubVersion) {
                if (pub.getPublicationReports() == null)
                    pub.setPublicationReports(new ArrayList<PublicationReport>());
                pub.getPublicationReports().add(report);
            }
        }
        return publications;
    }

/*
    private InternalRequest Get(String server_sub_path, FCPublication publication) {
        String get_publications_url = baseUrl + "/" + server_sub_path;
        Log.i(MY_TAG, "Get: " + get_publications_url);
        try {
            HttpGet httpGet = new HttpGet(get_publications_url);
            HttpClient client = new DefaultHttpClient();
            HttpGetWithEntity myGet = new HttpGetWithEntity(get_publications_url);
            myGet.setEntity(new StringEntity(publication.GetJSONObject().toString(), "UTF8"));
            HttpResponse response = client.execute(myGet);
            int resonseStatus = response.getStatusLine().getStatusCode();
            Log.i(MY_TAG, response.toString());
            if (resonseStatus == 200) {
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                responseJSONArray = new JSONArray(data);
                responseString = responseJSONArray.toString();
                Log.i("myTag", responseString);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {

        public HttpGetWithEntity() {
            super();
        }

        public HttpGetWithEntity(URI uri) {
            super();
            setURI(uri);
        }

        public HttpGetWithEntity(String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return HttpGet.METHOD_NAME;
        }
    }
*/
/*

    private InternalRequest GetAllPublicationsWithRegisteredUsers(String server_sub_path, String server_sub_sub_path){
        Get(server_sub_path);
        ArrayList<FCPublication> fetchedPublications = FCPublication.GetArrayListOfPublicationsFromJSON(responseJSONArray);
        if(fetchedPublications == null || fetchedPublications.size() == 0){

        }
        return null;
    }


    private InternalRequest Post(String server_sub_path){
        String post_url = baseUrl + server_sub_path;
        try {
            URL url = new URL(post_url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InternalRequest PostRegisterUser(String server_sub_path, UserRegisterData registerData) {
        String post_url = baseUrl + server_sub_path;
        try {
            URL url = new URL(post_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            registerData.WriteSelfToJSONWriter(writer);
            writer.close();
            outputStream.close();
            int connectionResponse = connection.getResponseCode();
            switch (connectionResponse){
                case HttpURLConnection.HTTP_OK:
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InternalRequest Get(String server_sub_path){
        String get_publications_url = baseUrl + "/" + server_sub_path;
        Log.i(MY_TAG, "Get: " + get_publications_url);
        try {
            HttpGet httpGet = new HttpGet(get_publications_url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpGet);
            int resonseStatus = response.getStatusLine().getStatusCode();
            Log.i(MY_TAG, response.toString());
            if(resonseStatus == 200){
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                responseJSONArray = new JSONArray(data);
                responseString = responseJSONArray.toString();
                Log.i("myTag", responseString);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;    }
        */

//    private void TestDeleteWithBody(ICanWriteSelfToJSONWriter object, String connString) {
//        Map<String, Object> registrationData = new HashMap<String, Object>();


//        registrationData.put("publication_id", 384);
//        registrationData.put("active_device_dev_uuid", "DD42331F-3E58-43E2-979C-6A0AC0E5A5C0");
//        registrationData.put("date_of_registration", 11243423); //this is for future use.
//        //if you don't save the registration date, you can pass any number here (or just use this one)
//        registrationData.put("publication_version", 1);
//
//        // make hash map
//        Map<String, Object> dataToSend = new HashMap<String, Object>();
//        dataToSend.put("registered_user_for_publication", registrationData);


        // convert dataToSend to a valid json object
//        org.json.simple.JSONObject json = new org.json.simple.JSONObject();
//        json.putAll(dataToSend);
//        System.out.println(json);

//        try {
//
//            // 1. URL
//            URL url = new URL(connString);
//
//            // 2. Open connection
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            // 3. Specify DELETE method
//            conn.setRequestMethod("DELETE");
//
//            // 4. Set the headers
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Accept", "application/json");
//
//            conn.setDoOutput(true);
//
//            // 5. Add JSON data into POST request body
//
//            // 5.1 Get connection output stream
//            // 5.2 write the json to bytes
//
//            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//
//            org.json.simple.JSONObject json = object.GetJsonObjectForPost();
//            String tstStr = json.toString();
//
//            wr.writeBytes(json.toString());
//
//            // 5.3 Send the request
//            wr.flush();
//
//            // 5.5 close
//            wr.close();
//
//            // 6. Get the response
//            int responseCode = conn.getResponseCode();
//            System.out.println("\nSending 'DELETE' request to URL : " + url);
//            System.out.println("Response Code : " + responseCode);
//
//            // 7. if the service returns something, this is how you read it back
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            // 7. Print result
//            System.out.println(response.toString());
//
//        } catch (MalformedURLException e) {
//
//            e.printStackTrace();
//
//        } catch (IOException e) {
//
//            e.printStackTrace();
//
//        }
//    }
}
