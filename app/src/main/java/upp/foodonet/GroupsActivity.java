package upp.foodonet;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.Group;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener, IFooDoNetServerCallback {

    EditText et_group_name;
    Button btn_create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        et_group_name = (EditText)findViewById(R.id.et_group_name);
        btn_create = (Button)findViewById(R.id.btn_group_create);
        btn_create.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_group_create:
                if(et_group_name.getText().toString().isEmpty())
                    Toast.makeText(this, "name empty", Toast.LENGTH_SHORT).show();
                HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                Group g = new Group(et_group_name.getText().toString(), CommonUtil.GetMyUserID(this));
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_GROUP, getString(R.string.server_post_new_group), g);
                connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
        }
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        Toast.makeText(this, "saving group " + (response.Status == InternalRequest.STATUS_OK ? "succeeded" : "failed"), Toast.LENGTH_SHORT).show();
        if(response.Status == InternalRequest.STATUS_OK && response.group != null){
            getContentResolver().insert(FooDoNetSQLProvider.URI_GROUP, response.group.GetContentValuesRow());
        }
    }
}
