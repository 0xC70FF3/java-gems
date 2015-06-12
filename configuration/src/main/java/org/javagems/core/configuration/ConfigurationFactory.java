package org.javagems.core.configuration;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class ConfigurationFactory {

    public static final String DATABASE_INSTANCE = "database.instance";
    public static final String CONFIGURATION_PATH = "configuration.path";    
    private static final String CONFIG_PROPERTIES_FILE = "conf.properties";        
    private static final String PROPERTIES_EXT = "properties";
    private static final String XML_EXT = "xml";
    private static final String INI_EXT = "ini";    
    private static final String FILE_NOT_FOUND_MSG = "File '%s' was not found in classpath";
    
    private static Configuration INSTANCE;
    private static final Boolean LOCK = Boolean.TRUE;
    
    public static Configuration getConfiguration() throws Exception {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new ConfigurationFactory().load();
                }
            }
        } 
        return INSTANCE;
    }

    public static void reset() {
        synchronized (LOCK) {
            INSTANCE = null;
        }
    }
    
    public ConfigurationFactory() {        
    }
    
    private Configuration load() throws Exception {
        CombinedConfiguration configuration = new CombinedConfiguration();
        File dir = this.getDirectory();
        
        if(!dir.exists()) {
        	throw new FileNotFoundException(dir.toString() + " directory does not exist");
        }
        
        String[] files = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("." + INI_EXT)
                        || name.toLowerCase().endsWith("." + XML_EXT)
                        || name.toLowerCase().endsWith("." + PROPERTIES_EXT);
            }
        });
                
        configuration.addConfiguration(this.getConfigProperties(CONFIG_PROPERTIES_FILE));       
        for (String file : files) {
            String ext = FilenameUtils.getExtension(file);
            if (XML_EXT.equalsIgnoreCase(ext)) {
                XMLConfiguration cfg = new XMLConfiguration(new File(dir, file));
                cfg.setReloadingStrategy(new FileChangedReloadingStrategy());
                configuration.addConfiguration(cfg);
            } 
            if (INI_EXT.equalsIgnoreCase(ext)) {
                HierarchicalINIConfiguration cfg = new HierarchicalINIConfiguration(new File(dir, file));
                cfg.setReloadingStrategy(new FileChangedReloadingStrategy());
                configuration.addConfiguration(cfg);                
            }
            if (PROPERTIES_EXT.equalsIgnoreCase(ext)) {
                PropertiesConfiguration cfg = new PropertiesConfiguration(new File(dir, file));
                cfg.setReloadingStrategy(new FileChangedReloadingStrategy());
                configuration.addConfiguration(cfg);
            }
        }
        return configuration;
    }
    
    protected PropertiesConfiguration getConfigProperties(String filename) throws Exception {
        try {
            PropertiesConfiguration cfg = new PropertiesConfiguration(filename);
            return cfg;
        } catch (org.apache.commons.configuration.ConfigurationException ex) {
            throw new IllegalArgumentException(String.format(FILE_NOT_FOUND_MSG, filename)); 
        }
    }
    
    public File getDirectory() throws Exception {
        return this.getDirectory(CONFIG_PROPERTIES_FILE);
    }
    
    public File getDirectory(String filename) throws Exception {
        PropertiesConfiguration cfg = this.getConfigProperties(filename);
        String dir = cfg.getString(CONFIGURATION_PATH);
        if (!StringUtils.isEmpty(dir)) {
            return new File(cfg.getString(CONFIGURATION_PATH));
        }
        return new File(this.getClass().getClassLoader().getResource(filename).getFile()).getParentFile();        
    }
}
