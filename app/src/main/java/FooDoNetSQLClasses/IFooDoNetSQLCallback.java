package FooDoNetSQLClasses;

import java.util.ArrayList;

import DataModel.FCPublication;

/**
 * Created by Asher on 24-Jul-15.
 */
public interface IFooDoNetSQLCallback {
    public void OnUpdateLocalDBComplete(ArrayList<FCPublication> publications);
}
