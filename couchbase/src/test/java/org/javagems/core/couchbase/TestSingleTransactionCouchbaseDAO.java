package org.javagems.core.couchbase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.couchbase.client.CouchbaseClient;

import net.spy.memcached.internal.OperationFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSingleTransactionCouchbaseDAO {

    private static class ADocument {
        @CouchbaseKey
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    private SingleTransactionCouchbaseDAO<ADocument, Long> dao;
    private CouchbaseClientFactory factory;
    private CouchbaseClient client;
    private OperationFuture<Boolean> op;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        op = (OperationFuture<Boolean>)mock(OperationFuture.class);
        factory = mock(CouchbaseClientFactory.class);
        client = mock(CouchbaseClient.class);
        when(factory.newClient()).thenReturn(client);
        this.dao = new SingleTransactionCouchbaseDAO<ADocument, Long>(ADocument.class, factory) {};
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testGet() throws Exception {
        when(client.get(anyString())).thenReturn("{\"id\":2}");
        
        ADocument document = this.dao.get(2l);
        
        assertEquals(new Long(2), document.getId());
        verify(client).get("2");
        verify(factory).closeQuietly(client);
    }
    
    @Test
    public void testSet() throws Exception {
        when(client.set(anyString(), anyInt(), anyString())).thenReturn(op);
        
        ADocument document = new ADocument();
        document.setId(2l);
        
        this.dao.set(document);
        
        verify(client).set("2", 0, "{\"id\":2}");
        verify(op).get();
        verify(factory).closeQuietly(client);
    }
    
    @Test
    public void testDelete() throws Exception {
        when(client.delete(anyString())).thenReturn(op);
        
        this.dao.delete(2l);
        
        verify(client).delete("2");
        verify(op).get();
        verify(factory).closeQuietly(client);
    }
    
    @Test
    public void testFlush() throws Exception {
        when(client.flush()).thenReturn(op);
        
        this.dao.flush();
        
        verify(client).flush();
        verify(op).get();
        verify(factory).closeQuietly(client);
    }
}
