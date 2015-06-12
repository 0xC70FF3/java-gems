package org.javagems.core.hibernate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

public class SessionFactoryBuilder {
    private static final String HIBERNATE_CFG_PATH = "hibernate.cfg.file";

    private InputStream hibernateInputStream;
    private List<File> hiberbateFiles;
    private List<Class<?>> mapping;
    private File configurationDirectory;
    private Properties properties;

    public SessionFactoryBuilder() {
        this.hiberbateFiles = new ArrayList<File>();
        this.mapping = new ArrayList<Class<?>>();
        this.configurationDirectory = new File("/etc/");

    }

    public SessionFactoryBuilder setHibernateInputStream(InputStream is) {
        this.hibernateInputStream = is;
        return this;
    }

    public SessionFactoryBuilder setHibernateFile(File file) {
        this.hiberbateFiles = Arrays.asList(file);
        return this;
    }

    public SessionFactoryBuilder setProperties(Properties props) {
        this.properties = props;
        return this;
    }

    public SessionFactoryBuilder setMapping(Class<?>... mapping) {
        this.mapping = Arrays.asList(mapping);
        return this;
    }

    public SessionFactoryBuilder addMapping(Class<?>... mapping) {
        this.mapping.addAll(Arrays.asList(mapping));
        return this;
    }

    public SessionFactoryBuilder setConfigurationDirectory(File directory) {
        this.configurationDirectory = directory;
        return this;
    }

    public SessionFactory build() {
        try {
            if (this.hibernateInputStream != null) {
                return this.configure(this.hibernateInputStream);
            }
            if (this.properties != null) {
                return this.configure(this.properties);
            }
            if (this.hiberbateFiles.isEmpty()) {
                if (System.getProperty(HIBERNATE_CFG_PATH) != null) {
                    this.hiberbateFiles.add(new File(System.getProperty(HIBERNATE_CFG_PATH)));
                }
                this.hiberbateFiles.add(new File("./hibernate.cfg.xml"));
                this.hiberbateFiles.add(new File(System.getProperty("user.home") + "/.config/hibernate.cfg.xml"));
                this.hiberbateFiles.add(new File(this.configurationDirectory, "hibernate.cfg.xml"));
            }

            for (File hibernateFile : this.hiberbateFiles) {
                if (hibernateFile.isFile() && hibernateFile.canRead()) {
                    return this.configure(FileUtils.openInputStream(hibernateFile));
                }
            }

            return this.configure();
        } catch (Throwable ex) {
            throw new IllegalStateException("SessionFactory not properly configured.", ex);
        }
    }

    private SessionFactory configure(Properties props) throws Exception {
        return this.register(new Configuration().setProperties(props));
    }

    private SessionFactory configure() throws Exception {
        return this.register(new Configuration().configure());
    }

    private SessionFactory configure(InputStream is) throws Exception {
        Configuration configuration = null;
        try {
            configuration = new Configuration().configure(
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(is));
        } finally {
            IOUtils.closeQuietly(is);
        }

        if (!this.mapping.isEmpty()) {
            for (Class<?> clazz : this.mapping) {
                configuration.addAnnotatedClass(clazz);
            }
        }
        return this.register(configuration);
    }

    private SessionFactory register(Configuration configuration) {
        if (!this.mapping.isEmpty()) {
            for (Class<?> clazz : this.mapping) {
                configuration.addAnnotatedClass(clazz);
            }
        }
        ServiceRegistry service = new ServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .buildServiceRegistry();
        return configuration.buildSessionFactory(service);
    }
}
