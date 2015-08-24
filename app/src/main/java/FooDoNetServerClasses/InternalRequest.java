package FooDoNetServerClasses;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import DataModel.FCPublication;
import DataModel.ICanWriteSelfToJSONWriter;
import DataModel.RegisteredUserForPublication;
import DataModel.UserRegisterData;

/**
 * Created by Asher on 23-Jul-15.
 */
public class InternalRequest {

    public static final int ACTION_GET_ALL_PUBLICATIONS = 0;
    public static final int ACTION_POST_NEW_PUBLICATION = 1;
    public static final int ACTION_POST_REGISTER = 2;
    public static final int ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION = 3;
    public static final int ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC = 4;
    public static final int ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER = 5;

    public static final int STATUS_OK = 1;
    public static final int STATUS_FAIL = 0;

    public int ActionCommand;
    public String ServerSubPath;
    public JSONObject jsonObject;
    public JSONArray jsonArray;
    public ArrayList<FCPublication> publications;
    public ArrayList<RegisteredUserForPublication> registeredUsers;
    public ICanWriteSelfToJSONWriter canWriteSelfToJSONWriterObject;
    public int Status;

    public InternalRequest(int actionCommand, JSONObject obj, String sub_path) {
        ActionCommand = actionCommand;
        jsonObject = obj;
        ServerSubPath = sub_path;
    }

    public InternalRequest(int com, JSONArray arr, String sub_path) {
        ActionCommand = com;
        jsonArray = arr;
        ServerSubPath = sub_path;
    }

/*
    public InternalRequest(int com, ArrayList<FCPublication> pubs, String sub_path) {
        ActionCommand = com;
        publications = pubs;
        ServerSubPath = sub_path;
    }
*/

    public InternalRequest(int com, String sub_path) {
        ActionCommand = com;
        ServerSubPath = sub_path;
    }

    public InternalRequest(int com, boolean status) {
        ActionCommand = com;
        Status = status ? STATUS_OK : STATUS_FAIL;
    }

    public InternalRequest(int com, String subPath, ICanWriteSelfToJSONWriter data) {
        ActionCommand = com;
        ServerSubPath = subPath;
        canWriteSelfToJSONWriterObject = data;
    }

    public InternalRequest(int com, ArrayList<FCPublication> publications, ArrayList<RegisteredUserForPublication> regUsers){
        ActionCommand = com;
        this.publications =  publications;
        registeredUsers = regUsers;
    }

}
