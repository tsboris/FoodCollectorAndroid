package upp.foodonet;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.Group;
import DataModel.GroupMember;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener, IFooDoNetServerCallback {

    EditText et_group_name;
    Button btn_create;
    Button btn_get_groups;
    TextView tv_get_groups_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        et_group_name = (EditText)findViewById(R.id.et_group_name);
        btn_create = (Button)findViewById(R.id.btn_group_create);
        btn_create.setOnClickListener(this);
        btn_get_groups = (Button)findViewById(R.id.btn_get_groups_by_user);
        btn_get_groups.setOnClickListener(this);
        tv_get_groups_result = (TextView)findViewById(R.id.tv_groups_by_user_data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_group_create:
                if(et_group_name.getText().toString().isEmpty())
                    Toast.makeText(this, "name empty", Toast.LENGTH_SHORT).show();
                HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                Group g = new Group(et_group_name.getText().toString(), CommonUtil.GetMyUserID(this));
                GroupMember owner = new GroupMember(0, CommonUtil.GetMyUserID(this), 0, true,
                                                    CommonUtil.GetMyPhoneNumberFromPreferences(this),
                                                    CommonUtil.GetSocialAccountNameFromPreferences(this));
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_GROUP, getString(R.string.server_post_new_group), g);
                ir.groupOwner = owner;
                ir.MembersServerSubPath = getString(R.string.server_post_add_members_to_group);
                connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
            case R.id.btn_get_groups_by_user:
                HttpServerConnectorAsync connectorAsync = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                InternalRequest irGetGroups = new InternalRequest(InternalRequest.ACTION_GET_GROUPS_BY_USER);
                irGetGroups.newUserID = CommonUtil.GetMyUserID(this);
                irGetGroups.ServerSubPath = getString(R.string.server_get_groups_by_user).replace("{0}", String.valueOf(CommonUtil.GetMyUserID(this)));
                connectorAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irGetGroups);
                break;
        }
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand){
            case InternalRequest.ACTION_POST_NEW_GROUP:
                Toast.makeText(this, "saving group " + (response.Status == InternalRequest.STATUS_OK ? "succeeded" : "failed"), Toast.LENGTH_SHORT).show();
                if(response.Status == InternalRequest.STATUS_OK && response.group != null){
                    getContentResolver().insert(FooDoNetSQLProvider.URI_GROUP, response.group.GetContentValuesRow());
                }
                break;
            case InternalRequest.ACTION_GET_GROUPS_BY_USER:
                Toast.makeText(this, "get groups by user " + (response.Status == InternalRequest.STATUS_OK ? "succeeded" : "failed"), Toast.LENGTH_SHORT).show();
                tv_get_groups_result.setText(response.resultString);
                break;
        }
    }
}
