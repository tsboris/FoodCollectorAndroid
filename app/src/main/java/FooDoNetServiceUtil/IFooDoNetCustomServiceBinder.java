package FooDoNetServiceUtil;

import android.os.Binder;

/**
 * Created by Asher on 30-Jul-15.
 */
public interface IFooDoNetCustomServiceBinder {
    void AttachToService(IFooDoNetServiceCallback callback);
}
