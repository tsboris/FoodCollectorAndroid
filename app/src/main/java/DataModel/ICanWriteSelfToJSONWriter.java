package DataModel;

import android.util.JsonWriter;

import org.json.simple.JSONObject;

import java.util.Map;

/**
 * Created by Asher on 19.08.2015.
 */
public interface ICanWriteSelfToJSONWriter {
    JSONObject GetJsonObjectForPost();
}
