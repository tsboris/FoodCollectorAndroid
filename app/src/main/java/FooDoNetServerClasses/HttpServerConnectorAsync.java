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
                    try {
                        regedUsers = RegisteredUserForPublication.
                                GetArrayListOfRegisteredForPublicationsFromJSON(
                                        new JSONArray(responseString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        responseString = "";
                    }
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
                    if (regedUsers.size() > 0) {
                        pub.setRegisteredForThisPublication(regedUsers);
                    }
                    if (pubReports.size() > 0) {
                        pub.setPublicationReports(pubReports);
                    }
                    regedUsers.clear();
                    pubReports.clear();
                    responseString = "";
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
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, registrationToPublicationToPost, false);
                return "";
            //endregion
            //region case report for publication
            case InternalRequest.ACTION_REPORT_FOR_PUBLICATION:
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
            connection.setConnectTimeout(30000);
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
                    //connection.setDoInput(true);
                    connection.setDoOutput(true);
/*
                    String str = "{\"active_device\":{\"last_location_longitude\":34.85003149, \"dev_uuid\":\"353784052343615\", \"last_location_latitude\":32.11102827, \"is_ios\":\"false\", \"remote_notification_token\":\"1234\"}}";
                    Log.e(MY_TAG, str);
                    bytes = str.getBytes("UTF-8");
                    connection.setRequestProperty("Content-length", String.valueOf(bytes.length));
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(bytes);//jo.toString().getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();
*/
                    break;
            }
            //connection.connect();
            if (writableObject != null) {

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                String s = writableObject.GetJsonObjectForPost().toString();
                String out = null;
                try {
                    out = new String(writableObject.GetJsonObjectForPost().toString().getBytes("UTF-8"), "ISO-8859-1");
                } catch (java.io.UnsupportedEncodingException e) {

                }

                wr.writeBytes(out);

                //wr.writeBytes(writableObject.GetJsonObjectForPost().toString());
                wr.flush();
                wr.close();
/*                 DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                JSONObject jo = new JSONObject(writableObject.GetJsonMapStringObject());
                Log.i(MY_TAG, jo.toString());
                dos.write(jo.toString().getBytes("UTF-8"));
                dos.flush();
                dos.close();

                 OutputStream outputStream = connection.getOutputStream();
                //Map<String, Object> regData = writableObject.GetJsonMapStringObject();
                //JSONObject jo = new JSONObject(regData);
                //OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                //Log.wtf(MY_TAG, jo.toString());
                //outputStream.write(bytes);//jo.toString().getBytes("UTF-8"));
                //outputStream.flush();
                //outputStream.close();
                StringWriter sw = new StringWriter();

                JsonWriter writer = new JsonWriter(sw);//new OutputStreamWriter(outputStream, "UTF-8"));
                writableObject.WriteSelfToJSONWriter(writer);
                String jsonS = sw.toString();
                Log.i(MY_TAG, jsonS);

                JsonWriter writerToStream = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writableObject.WriteSelfToJSONWriter(writerToStream);
                //writer.flush();
                writer.close();
                //outputStream.flush();
                outputStream.close();


              //

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String answer = sb.toString();
                Log.i(MY_TAG, answer);

                //*/
            }
            //Log.i(MY_TAG, "sending: " + connection.toString());
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
        } finally {
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
                Log.i(MY_TAG, "data loaded from http, calling callback");
                callbackListener.OnServerRespondedCallback(
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS, resultPublications, null));
                break;
            case InternalRequest.ACTION_POST_REGISTER:
                Log.i(MY_TAG, "successfully registered user on server");
                //callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, true));
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
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                Log.i(MY_TAG, "register to pub: " + (isSuccess? "ok":"fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
        }
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
}
