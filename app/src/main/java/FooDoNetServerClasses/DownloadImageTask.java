package FooDoNetServerClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ah on 31/08/15.
 */
public class DownloadImageTask extends AsyncTask<Map<Integer,Integer>, Void, Void> {

    private static final String MY_TAG = "DownloadImageTask";

    IDownloadImageCallBack callback;
    String baseUrl;

    Map<Integer, byte[]> resultImages;

    public DownloadImageTask(IDownloadImageCallBack callBack, String baseUrlImages) {
        this.callback = callBack;
        baseUrl = baseUrlImages;
        resultImages = null;
    }

    protected Void doInBackground(Map<Integer, Integer>... urls) {
        resultImages = new HashMap<>();
        Set<Integer> pubIDs = urls[0].keySet();
        for(int id : pubIDs){
            String url = baseUrl + "/" + String.valueOf(id)
                    + "." + String.valueOf(urls[0].get(id)) + ".jpg";
            InputStream is = null;
            try {
                is = new java.net.URL(url).openStream();
                byte[] result = IOUtils.toByteArray(is);
                resultImages.put(id, result);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(is != null) try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callback.OnImageDownloaded(resultImages);
    }
}