package org.javagems.dbunit;

import com.mysql.jdbc.StringUtils;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.javagems.mysql.EmbeddedMySQLDb;

import java.sql.ResultSet;


public class MySQLDBMock extends DBMock {

    private static final String URL_PREFIX = "jdbc:mysql:mxj://localhost";
    private static final String URL_OPTIONS = "createDatabaseIfNotExist=true&server.initialize-user=false&useUnicode=true&characterEncoding=UTF-8&server.collation-server=utf8_general_ci&server.character-set-server=utf8&server.max_allowed_packet=32M";
    
    private EmbeddedMySQLDb embeddedMySQLdb;

    protected MySQLDBMock(boolean useSchemas, String databaseName, int port) {
        super(useSchemas);
        this.embeddedMySQLdb = new EmbeddedMySQLDb(databaseName, port);
    }

    @Override
    public void startUp() throws Exception {
        this.embeddedMySQLdb.setUp();
        super.startUp();
    }
    
    @Override
    public void shutDown() throws Exception {
        super.shutDown();
        if (this.embeddedMySQLdb != null) {
            this.embeddedMySQLdb.tearDown();
        }
    }
    
    @Override
    public void setUp(Class<?> resourceBase, String[] tables, String[] inserts, String[] schemas) throws Exception {
        super.setUp(resourceBase, tables, inserts, schemas);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected IDatabaseTester newDatabaseTesterInstance() throws Exception {
        JdbcDatabaseTester jdbcDatabaseTester = new JdbcDatabaseTester(
                    com.mysql.jdbc.Driver.class.getName(),
                    URL_PREFIX + ":" + this.embeddedMySQLdb.getPort() + "/" + this.embeddedMySQLdb.getDatabaseName() + "?" + URL_OPTIONS,
                    this.embeddedMySQLdb.getUsername(),
                    this.embeddedMySQLdb.getPassword());
        
        return jdbcDatabaseTester;
    }
    
    @Override
    public void dropDatabase() throws Exception {
        MySQLDBMock.this.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
        ResultSet rs = MySQLDBMock.this.executeQuery(
                "SELECT GROUP_CONCAT(table_schema, '.', table_name) AS tables\n"
                        + "    FROM information_schema.tables\n"
                        + "    WHERE table_schema NOT IN ('information_schema', 'mysql', 'performance_schema');");
        rs.next();
        String tables = rs.getString("tables");
        if (!StringUtils.isNullOrEmpty(tables)) {
            MySQLDBMock.this.executeUpdate("DROP TABLE IF EXISTS " + tables + ";");
        }
        MySQLDBMock.this.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
    }

    @Override
    public void setSchema(String schema) throws Exception {
        if (!StringUtils.isNullOrEmpty(schema)) {
            this.executeUpdate("USE " + schema + ";");
        }
    }

}
