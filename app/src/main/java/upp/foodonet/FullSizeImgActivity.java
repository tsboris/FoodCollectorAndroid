package upp.foodonet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

/**
 * Created by nikolaiVolodin on 19/10/2015.
 */
public class FullSizeImgActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);

        ImageView imgF = (ImageView)findViewById(R.id.iv_full_image_size);

        imgF.setImageDrawable(PublicationDetailsActivity.fullImage);

    }
}