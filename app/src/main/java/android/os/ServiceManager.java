package android.os;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub for hidden Android API android.os.ServiceManager.
 * This class is not part of the public SDK but is available at runtime.
 */
public class ServiceManager {

    @SuppressWarnings("unchecked")
    private static Map<String, IBinder> sCache = new HashMap<>();

    public static IBinder getService(String name) {
        return sCache.get(name);
    }

    public static void addService(String name, IBinder service) {
        sCache.put(name, service);
    }

    public static String[] listServices() {
        return sCache.keySet().toArray(new String[0]);
    }
}
