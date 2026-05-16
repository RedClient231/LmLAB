package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;

/**
 * Stub for hidden Android API android.app.IActivityTaskManager.
 * This interface is not part of the public SDK but is available at runtime.
 */
public interface IActivityTaskManager extends IInterface {

    abstract class Stub extends Binder implements IActivityTaskManager {

        private static final String DESCRIPTOR = "android.app.IActivityTaskManager";

        public static IActivityTaskManager asInterface(IBinder obj) {
            if (obj == null) return null;
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin instanceof IActivityTaskManager) {
                return (IActivityTaskManager) iin;
            }
            return null;
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
