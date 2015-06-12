package org.javagems.core.couchbase;

import com.couchbase.client.CouchbaseClient;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public abstract class CouchbaseDAO<T, ID extends Serializable> {

    protected Class<T> clazz;
    private final CouchbaseClientFactory couchbaseClientFactory;
    private CouchbaseClient couchbaseClient;

    public CouchbaseDAO(Class<T> clazz, CouchbaseClientFactory couchbaseClientFactory) {
        this.clazz= clazz;
        this.couchbaseClient = null;
        this.couchbaseClientFactory = couchbaseClientFactory;
    }
    
    public CouchbaseClient getClient() {
        if (this.couchbaseClient == null) {
            this.couchbaseClient = this.couchbaseClientFactory.newClient();
        }
        return this.couchbaseClient;
    }

    public void closeQuietly() {
        if (this.couchbaseClient != null) {
            this.couchbaseClientFactory.closeQuietly(this.couchbaseClient);
            this.couchbaseClient = null;
        }
    }

    protected String serialize(T document) throws Exception {
        return new ObjectMapper().writeValueAsString(document);
    }
    
    protected T deserialize(String json) throws Exception {
        return new ObjectMapper().readValue(json, this.clazz);
    }
    
    protected Serializable getKey(Serializable key) {
        return key;
    }
    
    protected Serializable getKey(T document) throws Exception {
        for (Field field : this.clazz.getDeclaredFields()) {
            Annotation annotation = field.getAnnotation(CouchbaseKey.class);
            if (annotation instanceof CouchbaseKey) {
                field.setAccessible(true);
                Object key = field.get(document);
                if (key != null && key instanceof Serializable) {
                    return (Serializable)this.getKey((Serializable)key);
                }
            }
        }
        throw new IllegalArgumentException("No couchbase key defined.");
    }
}
