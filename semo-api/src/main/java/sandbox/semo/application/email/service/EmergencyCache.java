package sandbox.semo.application.email.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmergencyCache {

    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public void put(String key, String value, long ttl, TimeUnit timeUnit) {
        localCache.put(key, value);
        executorService.schedule(() -> localCache.remove(key), ttl, timeUnit);
    }

    public String get(String key) {
        return localCache.get(key);
    }

    public void remove(String key) {
        localCache.remove(key);
    }

    public boolean containsKey(String key) {
        return localCache.containsKey(key);
    }

    public ConcurrentHashMap<String, String> getAllEntries() {
        return localCache;
    }

}
