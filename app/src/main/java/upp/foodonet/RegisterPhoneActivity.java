package upp.foodonet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import org.w3c.dom.Text;

import FooDoNetServerClasses.DownloadImageTask;
import UIUtil.RoundedImageView;



public class RegisterPhoneActivity extends Activity implements View.OnClickListener{

    private EditText et_additional_info;
    private Button btn_register;
    private TextView tv_profile_name;
    public static final String PHONE_KEY = "phone_number";
    public static final String AVATAR_KEY = "avatar";
    public static final String USER_NAME = "user_name";
    public static final String NETWORKTYPE_KEY = "networktype";
    public static final String FACEBOOK_KEY = "facebook";
    public static final String GOOGLE_KEY = "google";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);

        et_additional_info = (EditText) findViewById(R.id.et_additional_info_add_edit_pub);
        et_additional_info.setInputType(InputType.TYPE_CLASS_NUMBER);

        btn_register = (Button) findViewById(R.id.btn_register_phone);
        btn_register.setOnClickListener(this);

        tv_profile_name = (TextView)findViewById(R.id.user_name);

        Intent i = this.getIntent();
        String photoURL = i.getStringExtra(AVATAR_KEY);
        if(photoURL != null){
            String socialNetworkType = i.getStringExtra(NETWORKTYPE_KEY);
            String baseUrl = "";
            if(socialNetworkType.equals(FACEBOOK_KEY))
                baseUrl = "https://graph.facebook.com/";
            ImageView userProfileImage = (ImageView) findViewById(R.id.iv_user_profile_img);
            DownloadImageTask imageTask = new DownloadImageTask(baseUrl + photoURL + "?type=large",
                    1024, getString(R.string.image_folder_path), userProfileImage);
            imageTask.execute();
        }

        tv_profile_name.setText(i.getStringExtra(USER_NAME));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void OnImageDownloaded(Bitmap result) {
//        Bitmap roundBmp = RoundedImageView.getRoundedCroppedBitmap(result, 200);
//        ImageView avatarImg = (ImageView)findViewById(R.id.iv_user_profile_img);
//        avatarImg.setImageBitmap(roundBmp);
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register_phone:
                Intent phoneIntent = new Intent();
                phoneIntent.putExtra(PHONE_KEY, et_additional_info.getText());
                // return data Intent and finish
                setResult(RESULT_OK, phoneIntent);
                finish();
        }
    }
}
