package ru.kontur.intern.shortener.data;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.stereotype.Component;
import ru.kontur.intern.shortener.models.Link;

@Component
public class IgniteWorker {

    private Ignite ignite;
    private IgniteCache<String, Link> cache;
    private boolean isStarted;

    public void startNode() throws UnknownHostException {
        ignite = Ignition.getOrStart(getConfiguration());
        ignite.cluster().active(true);
        createCache();
    }

    private void createCache() {
        cache = ignite.getOrCreateCache("Links");
    }

    public void stopNode() {
        Ignition.stop(true);
    }

    public Link getRecord(String key) {
        return cache.get(key);
    }

    public boolean containsRecord(String key) {
        return cache.containsKey(key);
    }

    public void putRecord(String key, Link link) {
        cache.put(key, link);
    }

    public void clear() {
        cache.clear();
    }

    public Map<String, Link> getAllRecords() {
        final Map<String, Link> map = new HashMap<>();
        for (Cache.Entry<String, Link> entry : cache) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    private IgniteConfiguration getConfiguration() throws UnknownHostException {
        final DataStorageConfiguration dataStorageConf = new DataStorageConfiguration();
        dataStorageConf.setStoragePath("/db");
        dataStorageConf.setWalPath("/db/wal");
        dataStorageConf.setWalArchivePath("/db/wal/archive");
        dataStorageConf.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);

        final IgniteConfiguration igniteConf = new IgniteConfiguration();
        igniteConf.setDataStorageConfiguration(dataStorageConf);

        Ignition.setClientMode(false);

        igniteConf.setConsistentId(InetAddress.getLocalHost().getHostName());

        return igniteConf;
    }
}
