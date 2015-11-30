package FooDoNetServerClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import CommonUtilPackage.CommonUtil;

/**
 * Created by ah on 31/08/15.
 */
public class DownloadImageTask extends AsyncTask<Void, Void, Void> {

    private static final String MY_TAG = "DownloadImageTask";

    IDownloadImageCallBack callback;
    String baseUrl;

    private int maxImageWidthHeight;
    private String imageFolderPath;

    Map<Integer, Integer> request;
    Map<Integer, byte[]> resultImages;

    public DownloadImageTask(IDownloadImageCallBack callBack, String baseUrlImages, int maxImageWidthHeight, String imageFolderPath) {
        this.callback = callBack;
        baseUrl = baseUrlImages;
        resultImages = null;
        this.maxImageWidthHeight = maxImageWidthHeight;
        this.imageFolderPath = imageFolderPath;
    }

    public synchronized void setRequestHashMap(Map<Integer, Integer> urls){
        if(urls == null || urls.size() == 0)
            return;
        if(request == null)
            request = new HashMap<>();
        Set<Integer> set = urls.keySet();
        for(int i : set)
            request.put(i, urls.get(i));
    }

    protected Void doInBackground(Void... params) {
        resultImages = new HashMap<>();
        if(request == null || request.size() == 0)
            return null;
        Set<Integer> pubIDs = request.keySet();
        for (int id : pubIDs) {
            String fileName = String.valueOf(id)
                    + "." + String.valueOf(request.get(id)) + ".jpg";
            String url = baseUrl + "/" + fileName;
            InputStream is = null;
            try {

                HttpURLConnection connection = null;
                URL urlurl = new URL(url);
                connection = (HttpURLConnection) urlurl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(1000);
                connection.setUseCaches(false);
                is = connection.getInputStream();

                //is = new java.net.URL(url).openStream();
                byte[] result = IOUtils.toByteArray(is);
                result = CommonUtil.CompressImageByteArrayByMaxSize(result, maxImageWidthHeight);
                Log.i(MY_TAG, "Compressed image to " + (int)Math.round(result.length/1024) + " kb");

                File photo=new File(Environment.getExternalStorageDirectory() + imageFolderPath, fileName);

                if (photo.exists()) {
                    photo.delete();
                }

                try {
                    FileOutputStream fos=new FileOutputStream(photo.getPath());

                    fos.write(result);
                    fos.close();
                }
                catch (java.io.IOException e) {
                    Log.e(MY_TAG, "cant save image");
                }
                //resultImages.put(id, result);
                Log.i(MY_TAG, "succeeded load image " + photo.getPath());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(MY_TAG, "cant load image for: " + fileName);
            } finally {
                if (is != null) try {
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
        callback.OnImageDownloaded(null);
    }
}