package FooDoNetServerClasses;

import android.os.AsyncTask;
import android.util.JsonWriter;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import DataModel.FCPublication;
import DataModel.ICanWriteSelfToJSONWriter;
import DataModel.RegisteredUserForPublication;
import DataModel.UserRegisterData;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;

/**
 * Created by Asher on 21-Jul-15.
 */
public class HttpServerConnectorAsync extends AsyncTask<InternalRequest, Void, String> {

    private static final String MY_TAG = "food_httpConnecterAsync";

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String REQUEST_METHOD_GET = "GET";

    private String baseUrl;
    private IFooDoNetServerCallback callbackListener;

    private String responseString;
    private JSONArray responseJSONArray;
    private JSONObject responseJSONObject;
    private InternalRequest internalResponse;
    private ArrayList<FCPublication> resultPublications;

    private int Action_Command_ID;

    public HttpServerConnectorAsync(String baseUrl, IFooDoNetServerCallback callbackListener) {
        this.baseUrl = baseUrl;
        this.callbackListener = callbackListener;
    }

    @Override
    protected String doInBackground(InternalRequest... params) {
        if (params.length == 0 || params[0] == null)
            return "";
        Action_Command_ID = params[0].ActionCommand;
        String server_sub_path = params[0].ServerSubPath;
        switch (Action_Command_ID) {
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
                    }
                    if (regedUsers.size() > 0) {
                        pub.setRegisteredForThisPublication(regedUsers);
                    }
                    regedUsers.clear();
                    responseString = "";
                }
                if (publications.size() > 0) {
                    if (resultPublications == null)
                        resultPublications = new ArrayList<>();
                    resultPublications.addAll(publications);
                }
                return "";
            case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:

                return "";
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                //MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                Get(server_sub_path, (FCPublication)params[0].canWriteSelfToJSONWriterObject);
                return "";
            case InternalRequest.ACTION_POST_REGISTER:
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, params[0].canWriteSelfToJSONWriterObject, false);
                return "";
            default:
                return "";
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
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, true));
                break;
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                Log.i(MY_TAG, "successfully posted new publication to server");
                callbackListener.OnServerRespondedCallback(internalResponse);
        }
    }

    private void MakeServerRequest(String requestMethod, String server_sub_path, ICanWriteSelfToJSONWriter writableObject, boolean isForResult) {
        String post_url = baseUrl + server_sub_path;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(post_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            //connection.setRequestMethod(requestMethod);
            connection.setRequestMethod("GET");
            switch (requestMethod) {
                case REQUEST_METHOD_GET:
                    //connection.setRequestProperty("Content-length", "0");
                    connection.setUseCaches(false);
                    connection.setAllowUserInteraction(false);
                    if (writableObject != null) {
                        //connection.setDoInput(true);
                        connection.setDoOutput(true);
                    }
                    break;
                case REQUEST_METHOD_POST:
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    break;
            }
            if (writableObject != null) {
                OutputStream outputStream = connection.getOutputStream();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writableObject.WriteSelfToJSONWriter(writer);
                writer.flush();
                writer.close();
                outputStream.close();
/*                //

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
            Log.i(MY_TAG, "sending: " + connection.toString());
            connection.connect();
            int connectionResponse = connection.getResponseCode();
            switch (connectionResponse) {
                case HttpURLConnection.HTTP_OK:
                    if (isForResult) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        Log.i(MY_TAG, this.hashCode() + sb.toString());
                        responseString = sb.toString();
                    }
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
