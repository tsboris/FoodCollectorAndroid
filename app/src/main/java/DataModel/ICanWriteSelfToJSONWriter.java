package DataModel;

import android.util.JsonWriter;

import java.util.Map;

/**
 * Created by Asher on 19.08.2015.
 */
public interface ICanWriteSelfToJSONWriter {
    void WriteSelfToJSONWriter(JsonWriter writer);
    Map<String, Object> GetJsonMapStringObject();
}
