package org.javagems.core.couchbase;

import java.io.Serializable;

public class SingleTransactionCouchbaseDAO<T, ID extends Serializable> extends CouchbaseDAO<T, ID> {

    public SingleTransactionCouchbaseDAO(Class<T> clazz, CouchbaseClientFactory couchbaseClientFactory) {
        super(clazz, couchbaseClientFactory);
    }

    public T get(ID id) throws Exception {
        try {
            String json;
            if ((json = (String) this.getClient().get(this.getKey(id).toString())) != null) {
                return this.deserialize(json);
            }
            throw new Exception(
                    String.format("Could not get document: [%s#%s]", this.clazz.getCanonicalName(), id.toString()));
        } finally {
            this.closeQuietly();
        }
    }

    public void set(T... documents) throws Exception {
        this.set(0, documents);
    }

    public void set(int expTime, T... documents) throws Exception {
        try {
            for (T document : documents) {
                String json = this.serialize(document);
                Serializable key = this.getKey(document);
                this.getClient().set(key.toString(), expTime, json).get();
            }
        } finally {
            this.closeQuietly();
        }
    }

    public void delete(ID... ids) throws Exception {
        try {
            for (ID id : ids) {
                this.getClient().delete(this.getKey(id).toString()).get();
            }
        } finally {
            this.closeQuietly();
        }
    }

    public void flush() throws Exception {
        try {
            this.getClient().flush().get();
        } finally {
            this.closeQuietly();
        }
    }
}
