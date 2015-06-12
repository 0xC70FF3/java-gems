package org.javagems.core.couchbase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.couchbase.client.CouchbaseClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCouchbaseDAO {

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

    @SuppressWarnings("unused")
    private static class AClass {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    private CouchbaseDAO<ADocument, Long> dao;
    private CouchbaseClientFactory factory;
    private CouchbaseClient client;

    @Before
    public void setUp() throws Exception {
        factory = mock(CouchbaseClientFactory.class);
        client = mock(CouchbaseClient.class);
        when(factory.newClient()).thenReturn(client);
        this.dao = new CouchbaseDAO<ADocument, Long>(ADocument.class, factory) {};
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testGetKey() throws Exception {
        ADocument document = new ADocument();
        document.setId(2);
        assertEquals(new Long(2), this.dao.getKey(document));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetKeyWithObject() throws Exception {
        AClass clazz = new AClass();
        new CouchbaseDAO<AClass, Long>(AClass.class, null) {}.getKey(clazz);
    }

    @Test
    public void testGetClient() throws Exception {
        CouchbaseClient client = this.dao.getClient();
        assertNotNull(client);
        assertTrue(client instanceof CouchbaseClient);
        assertSame(client, this.dao.getClient());
    }

    @Test
    public void testCloseQuietly() throws Exception {
        this.dao.getClient();
        this.dao.closeQuietly();
        verify(factory).closeQuietly(client);
    }

    @Test
    public void testSerialize() throws Exception {
        ADocument document = new ADocument();
        document.setId(2);
        assertEquals("{\"id\":2}", this.dao.serialize(document));
    }

    @Test
    public void testDeserialize() throws Exception {
        ADocument document = this.dao.deserialize("{\"id\":2}");
        assertEquals(new Long(2), document.getId());
    }

}
