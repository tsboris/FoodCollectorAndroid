package FooDoNetServiceUtil;

import java.util.ArrayList;

import DataModel.FCPublication;

/**
 * Created by Asher on 30-Jul-15.
 */
public interface IFooDoNetServiceCallback {
    void OnNotifiedToFetchData();
    void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList);
}
