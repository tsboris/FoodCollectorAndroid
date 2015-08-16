package FooDoNetServerClasses;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;

/**
 * Created by Asher on 21-Jul-15.
 */
public class HttpServerConnecterAsync extends AsyncTask<InternalRequest, Void, String> {

    private final String MY_TAG = "food_httpConnecterAsync";

    private String baseUrl;
    private IFooDoNetServerCallback callbackListener;

    private String responseString;
    private JSONArray responseJSONArray;

    public HttpServerConnecterAsync(String baseUrl, IFooDoNetServerCallback callbackListener){
        this.baseUrl = baseUrl;
        this.callbackListener = callbackListener;
    }

    @Override
    protected String doInBackground(InternalRequest... params) {
        if(params.length == 0 || params[0] == null)
            return "";
        String server_sub_path = params[0].ServerSubPath;
        switch (params[0].ActionCommand){
            case InternalRequest.ACTION_GET_ALL_PUBLICATIONS:
                Get(server_sub_path);
                return "";
            case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:
                return "";
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                Post();
                return "";
            case InternalRequest.ACTION_POST_REGISTER:

                return "";
            default:
                return "";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
        Log.i(MY_TAG,"data loaded from http, calling callback");
        callbackListener.OnServerRespondedCallback(
                new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS,
                        FCPublication.GetArrayListOfPublicationsFromJSON(responseJSONArray), null));
    }

    private InternalRequest GetAllPublicationsWithRegisteredUsers(String server_sub_path, String server_sub_sub_path){
        Get(server_sub_path);
        ArrayList<FCPublication> fetchedPublications = FCPublication.GetArrayListOfPublicationsFromJSON(responseJSONArray);
        if(fetchedPublications == null || fetchedPublications.size() == 0){

        }
        return null;
    }

    private InternalRequest Post(){
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
}
