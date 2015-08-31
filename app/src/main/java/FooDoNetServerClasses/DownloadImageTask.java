package FooDoNetServerClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by ah on 31/08/15.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private static final String MY_TAG = "DownloadImageTask";

    public DownloadImageTask(IDownloadImageCallBack callBack) {
        this.callback = callBack;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        callback.OnImageDownloaded(result);
    }

    IDownloadImageCallBack callback;
}