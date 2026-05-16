package android.app

import android.os.IBinder
import android.os.IInterface

/**
 * Stub for hidden Android API android.app.IActivityTaskManager.
 * This interface is not part of the public SDK but is available at runtime.
 */
interface IActivityTaskManager : IInterface {

    abstract class Stub : Binder(), IActivityTaskManager {

        companion object {
            @JvmStatic
            fun asInterface(binder: IBinder): IActivityTaskManager {
                throw UnsupportedOperationException("Stub - not implemented in compilation stub")
            }
        }
    }
}
