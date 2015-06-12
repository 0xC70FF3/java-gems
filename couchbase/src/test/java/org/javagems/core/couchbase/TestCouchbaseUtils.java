package org.javagems.core.couchbase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;

public class TestCouchbaseUtils {


    @Before
    public void setUp() throws Exception {
        CouchbaseUtils
            .StaticBuilder
            .setIniFile(new File(this.getClass().getClassLoader().getResource("couchbase.ini").getFile()))
            .setUp();
    }

    @After
    public void tearDown() throws Exception {
        CouchbaseUtils.shutdown();
    }

    @Test(expected=IllegalStateException.class)
    public void testNotInitialized() {
        CouchbaseUtils.shutdown();
        CouchbaseUtils.getClientFactory("anyString");
    }
    
    @Test
    public void testGetClientFactory() {
        CouchbaseClientFactory factory = CouchbaseUtils.getClientFactory("abucket");
        assertNotNull(factory);
        assertEquals("abucket", factory.getBucket());
        assertEquals(Arrays.asList(URI.create("http://anyhostserver:8091/pools")), factory.getNodes());
        assertEquals(3000, factory.getOpTimeout());
    }
    
    @Test
    public void testStoreFactory() {
        CouchbaseClientFactory factory = mock(CouchbaseClientFactory.class);
        
        CouchbaseUtils.storeFactory("myfactory", factory);
        
        assertSame(factory, CouchbaseUtils.getClientFactory("myfactory"));
    }
    
    @Test
    public void testKeySet() {
        CouchbaseClientFactory factory = mock(CouchbaseClientFactory.class);
        CouchbaseUtils.storeFactory("myfactory", factory);
        
        Set<String> set = CouchbaseUtils.keySet();
        assertEquals(1, set.size());
        assertEquals("myfactory", set.iterator().next());
    }
    
    @Test
    public void testClear() {
        CouchbaseClientFactory factory = mock(CouchbaseClientFactory.class);
        CouchbaseUtils.storeFactory("myfactory", factory);
        
        CouchbaseUtils.clear();
        
        Set<String> set = CouchbaseUtils.keySet();
        assertEquals(0, set.size());
        verify(factory).shutdown();
    }

}
