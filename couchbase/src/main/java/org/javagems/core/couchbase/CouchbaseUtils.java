package org.javagems.core.couchbase;

import org.apache.commons.configuration.HierarchicalINIConfiguration;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CouchbaseUtils {

    private static final String CONFIG_NODES_KEY = "nodes";
    private static final String CONFIG_OP_TIMEOUT_KEY = "opTimeout";
    private static final String CONFIG_DEFAULT_BUCKET_KEY = "bucket";
    private static final String CONFIGURATION_ERROR_MSG = "CouchbaseUtils was not properly configured.";

    private static final Boolean LOCK = true;
    private static CouchbaseUtils INSTANCE = null;

    private Map<String, CouchbaseClientFactory> factories;
    private final File iniFile;
    private String instanceName;

    public static class StaticBuilder {
        private StaticBuilder() {}

        public static Builder setIniFile(File iniFile) {
            return new Builder().setIniFile(iniFile);
        }

        public static void newInstance() {
            new Builder().setUp();
        }
    }

    public static class Builder {
        private File iniFile;
        private String instanceName;

        private Builder() {}

        public Builder setIniFile(File iniFile) {
            this.iniFile = iniFile;
            return this;
        }

        public Builder setInstanceName(String instanceName) {
            this.instanceName = instanceName;
            return this;
        }

        public void setUp() {
            if (CouchbaseUtils.INSTANCE == null) {
                synchronized (LOCK) {
                    if (CouchbaseUtils.INSTANCE == null) {
                        CouchbaseUtils.INSTANCE = new CouchbaseUtils(iniFile, instanceName);
                    }
                }
            }
        }
    }

    private CouchbaseUtils(File iniFile, String instanceName) {
        this.factories = new HashMap<String, CouchbaseClientFactory>();
        this.iniFile = iniFile;
        this.instanceName = instanceName;
    }

    private static CouchbaseUtils getInstance() {
        if (CouchbaseUtils.INSTANCE == null) {
            throw new IllegalStateException(CONFIGURATION_ERROR_MSG);
        }
        return CouchbaseUtils.INSTANCE;
    }

    private static Map<String, CouchbaseClientFactory> getFactories() {
        return CouchbaseUtils.getInstance().factories;
    }

    public static void storeFactory(String key, CouchbaseClientFactory client) {
        synchronized (LOCK) {
            if (CouchbaseUtils.getFactories().containsKey(key)) {
                CouchbaseUtils.getFactories().get(key).shutdown();
            }
            CouchbaseUtils.getFactories().put(key, client);
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            for (CouchbaseClientFactory factory : CouchbaseUtils.getFactories().values()) {
                if (factory != null) {
                    factory.shutdown();
                }
            }
            CouchbaseUtils.getFactories().clear();
        }
    }

    public static Set<String> keySet() {
        return CouchbaseUtils.getFactories().keySet();
    }

    public static CouchbaseClientFactory getClientFactory(String key) {
        CouchbaseClientFactory factory = CouchbaseUtils.getFactories().get(key);
        if (factory == null) {
            synchronized (LOCK) {
                if (!CouchbaseUtils.getFactories().containsKey(key)) {
                    factory = CouchbaseUtils.getInstance().buildFactory(key);
                    CouchbaseUtils.storeFactory(key, factory);
                }
            }
        }
        return factory;
    }

    public static void shutdown() {
        if (CouchbaseUtils.INSTANCE != null) {
            CouchbaseUtils.clear();
        }
        CouchbaseUtils.INSTANCE = null;
    }

    private HierarchicalINIConfiguration loadIniFile() {
        try {
            HierarchicalINIConfiguration ini = new HierarchicalINIConfiguration(this.iniFile);
            if (ini.isEmpty()) {
                throw new IllegalStateException(CONFIGURATION_ERROR_MSG + " INI file is empty.");
            }
            return ini;
        } catch (Exception ex) {
            throw new IllegalStateException(CONFIGURATION_ERROR_MSG, ex);
        }
    }

    private CouchbaseClientFactory buildFactory(String key) {
        HierarchicalINIConfiguration ini = this.loadIniFile();
        if (ini.getSections().contains(key)) {
            String bucket = ini.getSection(key).getString(CONFIG_DEFAULT_BUCKET_KEY);
            int opTimeout = ini.getSection(key).getInt(CONFIG_OP_TIMEOUT_KEY);
            List<URI> nodes = new ArrayList<URI>();
            for (String node : ini.getSection(key).getStringArray(CONFIG_NODES_KEY)) {
                nodes.add(URI.create(node));
            }
            return new CouchbaseClientFactory(bucket, nodes, opTimeout, instanceName);
        } else {
            throw new IllegalArgumentException("Section \"" + key + "\" do not exists in file.");
        }
    }
}
