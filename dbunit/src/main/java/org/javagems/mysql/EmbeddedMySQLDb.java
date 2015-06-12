package org.javagems.mysql;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmbeddedMySQLDb {
    private MysqldResource mysqldResource;
    private String baseDatabaseDir;
    private String databaseName;
    private int port;
    private String username;
    private String password;

    public EmbeddedMySQLDb() {
        this(null, -1);
    }
    
    public EmbeddedMySQLDb(String databaseName, int port) {
        this.baseDatabaseDir = System.getProperty("java.io.tmpdir");
        this.databaseName = (databaseName == null) ? ("test_db_" + System.nanoTime()) : databaseName;
        this.port = (port < 0) ? (13306 + new Random().nextInt(10000)) : port;
        this.username = "root";
        this.password = "";        
    }
    
    private PrintStream getNullAppender() {
        try {
            if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                return new PrintStream(new FileOutputStream("NUL:"));
            }
            return new PrintStream(new FileOutputStream("/dev/null"));
        } catch (Exception ex) {}
        return null;
    }
    
    public void setUp() {
        File databaseDir = new File(new File(baseDatabaseDir), databaseName);
        
        PrintStream nps = this.getNullAppender();
        mysqldResource = new MysqldResource(databaseDir, null, null, nps, nps);        
        
        Map<String, String> database_options = new HashMap<String, String>();
        database_options.put(MysqldResourceI.PORT, Integer.toString(port));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, username);
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, password);        
        
        mysqldResource.start("embedded-mysqld-thread-" + System.currentTimeMillis(), database_options);

        if (!mysqldResource.isRunning()) {
            throw new RuntimeException("MySQL did not start.");
        }
    }

    public void tearDown() {
        mysqldResource.shutdown();
        if (mysqldResource.isRunning() == false) {
            try {
                FileUtils.forceDelete(mysqldResource.getBaseDir());
            } catch (IOException e) {} //do nothing
        }
    }

    public final String getBaseDatabaseDir() {
        return this.baseDatabaseDir;
    }

    public final void setBaseDatabaseDir(String baseDatabaseDir) {
        this.baseDatabaseDir = baseDatabaseDir;
    }

    public final String getDatabaseName() {
        return this.databaseName;
    }

    public final void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public final int getPort() {
        return this.port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

    public final String getUsername() {
        return this.username;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final String getPassword() {
        return this.password;
    }

    public final void setPassword(String password) {
        this.password = password;
    }
}
