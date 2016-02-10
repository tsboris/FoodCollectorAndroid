package upp.foodonet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import FooDoNetServerClasses.ImageDownloader;

/**
 * Created by Asher on 08.02.2016.
 */
public class TestActivity extends Activity {

    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);
        iv = (ImageView)findViewById(R.id.iv_test_async_image_load);
        ImageDownloader downloader = new ImageDownloader(this, null);
        downloader.Download(0, 0, iv);
    }
}
