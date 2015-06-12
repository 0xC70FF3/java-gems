package org.javagems.core.couchbase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.couchbase.client.CouchbaseClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class TestCouchbaseClientFactory {

    private String bucket;
    private List<URI> nodes;
    private long opTimeout;
    private CouchbaseClientFactory factory;

    @Before
    public void setUp() throws Exception {
        bucket = "labelsmap";
        nodes = Arrays.asList();
        opTimeout = 3000;
        factory = new CouchbaseClientFactory(bucket, nodes, opTimeout, "local");
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testConstructor() {
        assertEquals(bucket, factory.bucket);
        assertEquals(nodes, factory.nodes);
        assertEquals(opTimeout, factory.opTimeout);
        assertEquals(0, factory.registeredClients.size());
    }

    @Test
    public void testNewClient() throws Exception {
//        CouchbaseConnectionFactoryBuilder builder = mock(CouchbaseConnectionFactoryBuilder.class);
//        CouchbaseConnectionFactory connexionFactory = mock(CouchbaseConnectionFactory.class);
//        Config config = mock(Config.class);
//        ConfigurationProvider configProvider = mock(ConfigurationProvider.class);
//
//        when(config.getServers()).thenReturn(Arrays.asList("http://anyhostserver:8091"));
//        when(connexionFactory.getOperationTimeout()).thenReturn(3000l);
//        when(connexionFactory.getConfigurationProvider()).thenReturn(configProvider);
//        when(connexionFactory.getVBucketConfig()).thenReturn(config);
//        when(builder.buildCouchbaseConnection(anyListOf(URI.class), anyString(), anyString())).thenReturn(connexionFactory);
//        
//        when(connexionFactory.getOperationFactory()).thenReturn(mock(OperationFactory.class));
//
//        CouchbaseClient client = factory.newClient();
//
//        assertNotNull(client);
//        assertEquals(1, factory.registeredClients.size());
//        assertSame(client, factory.registeredClients.get(0));
    }

    @Test
    public void testCloseQuietly() throws Exception {
        CouchbaseClient client = mock(CouchbaseClient.class);
        factory.registeredClients.add(client);
        assertEquals(1, factory.registeredClients.size());

        factory.closeQuietly(client);

        verify(client).shutdown();
        assertEquals(0, factory.registeredClients.size());
    }

    @Test
    public void testShutDown() throws Exception {
        CouchbaseClient client1 = mock(CouchbaseClient.class);
        CouchbaseClient client2 = mock(CouchbaseClient.class);
        factory.registeredClients.addAll(Arrays.asList(client1, client2));
        assertEquals(2, factory.registeredClients.size());

        factory.shutdown();

        verify(client1).shutdown();
        verify(client2).shutdown();
        assertEquals(0, factory.registeredClients.size());
    }

}
