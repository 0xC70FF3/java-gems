package org.javagems.core.couchbase;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CouchbaseClientFactory {

    protected String bucket;
    protected List<URI> nodes;
    protected long opTimeout;
    protected List<CouchbaseClient> registeredClients;
    private CouchbaseConnectionFactoryBuilder builder;
    private String instanceName;

    static {
        Properties systemProperties = System.getProperties();
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger");
        System.setProperties(systemProperties);
    }

    public CouchbaseClientFactory(String bucket, List<URI> nodes, long opTimeout, String instanceName) {
        this.builder = new CouchbaseConnectionFactoryBuilder();
        this.registeredClients = new ArrayList<CouchbaseClient>();
        this.bucket = bucket;
        this.nodes = nodes;
        this.opTimeout = opTimeout;
        this.instanceName = instanceName;
    }
    
    public String getBucket() {
        return bucket;
    }

    public List<URI> getNodes() {
        return nodes;
    }

    public long getOpTimeout() {
        return opTimeout;
    }
    
    public String getInstanceName() {
        return instanceName;
    }

    protected void setBuilder(CouchbaseConnectionFactoryBuilder builder) {
        this.builder = builder;
    }

    public CouchbaseClient newClient() {
        try {
            this.builder.setOpTimeout(opTimeout);
            CouchbaseClient client = new CouchbaseClient(this.builder.buildCouchbaseConnection(nodes, bucket, ""));
            this.registeredClients.add(client);
            return client;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void closeQuietly(CouchbaseClient client) {
        if (client != null) {
            this.registeredClients.remove(client);
            client.shutdown();
        }
    }

    public void shutdown() {
        for (CouchbaseClient client : this.registeredClients) {
            if (client != null) {
                client.shutdown();
            }
        }
        this.registeredClients.clear();
    }
}
