package FooDoNetServerClasses;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import DataModel.FCPublication;

/**
 * Created by Asher on 23-Jul-15.
 */
public class InternalRequest {

    public static final int ACTION_GET_ALL_PUBLICATIONS = 0;
    public static final int ACTION_POST_NEW_PUBLICATION = 1;
    public static final int ACTION_POST_REGISTER = 2;

    public static final int STATUS_OK = 1;
    public static final int STATUS_FAIL = 0;

    public InternalRequest(int actionCommand, JSONObject obj){
        ActionCommand = actionCommand;
        jsonObject = obj;
    }

    public InternalRequest(int com, JSONArray arr){
        ActionCommand = com;
        jsonArray = arr;
    }

    public InternalRequest(int com, ArrayList<FCPublication> pubs){
        ActionCommand = com;
        publications = pubs;
    }

    public InternalRequest(int com){
        ActionCommand = com;
    }

    public InternalRequest(int com, boolean status){
        ActionCommand = com;
        Status = status? STATUS_OK:STATUS_FAIL;
    }

    public int ActionCommand;
    public JSONObject jsonObject;
    public JSONArray jsonArray;
    public ArrayList<FCPublication> publications;
    public int Status;

}
