package org.javagems.core.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestConfigurationFactory {

    private ConfigurationFactory manager ;
    
    @Before
    public void setUp() {
        this.manager = spy(new ConfigurationFactory());
    }
    
    @After
    public void tearDown() {
        ConfigurationFactory.reset();
    }
    
    @Test
    public void testGetDirectory() throws Exception {
        assertEquals("[my_section]\nmy_value=3", FileUtils.readFileToString(new File(this.manager.getDirectory(), "test.ini"))); 
    }

    @Test(expected=FileNotFoundException.class)
    public void testGetDirectoryDoesNotExist() throws Exception {
        FileUtils.readFileToString(new File(this.manager.getDirectory(), "nofile.ini")); 
    }
    
    @Test
    public void testGetDirectoryWithConfigurationPathDefined() throws Exception {
        assertEquals("/tmp", this.manager.getDirectory("conf-prod.properties").getAbsolutePath());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetDirectoryWithConfigurationDoesNotExist() throws Exception {
        this.manager.getDirectory("config-another.properties").getAbsolutePath();
    }
    
    @Test
    public void testGetConfiguration() throws Exception {
        Configuration configuration = ConfigurationFactory.getConfiguration();
        assertEquals(7, configuration.getInt("locos.timeout"));
        assertTrue(configuration.getList("my_section.my_value").contains("2"));
        assertTrue(configuration.getList("my_section.my_value").contains("3"));
    }
    
    @Test(expected=NoSuchElementException.class)
    public void testResetConfiguration() throws Exception {
        ConfigurationFactory.getConfiguration().setProperty("another_value", 4);
        assertEquals(4, ConfigurationFactory.getConfiguration().getInt("another_value"));

        ConfigurationFactory.reset();
        ConfigurationFactory.getConfiguration().getInt("another_value");
    }
}
