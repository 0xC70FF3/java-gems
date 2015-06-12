package org.javagems.dbunit;

import com.mysql.jdbc.StringUtils;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;

/**
 * http://danhaywood.com/2011/12/20/db-unit-testing-with-dbunit-json-hsqldb-and-junit-rules/
 * https://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner.html
 */
public class H2DBMock extends DBMock {

    private static final String USERNAME = "SA";
    private static final String PASSWORD = "";
    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String URL_OPTIONS = "MODE=MySQL;DATABASE_TO_UPPER=false";

    private String databaseName;
    
    protected H2DBMock(boolean useSchemas, String databaseName) {
        super(useSchemas);
        this.databaseName = databaseName; 
    }

    @Override
    protected IDatabaseTester newDatabaseTesterInstance() throws Exception {
        return new JdbcDatabaseTester(
                org.h2.Driver.class.getName(),
                URL_PREFIX + this.databaseName + ";" + URL_OPTIONS,
                USERNAME,
                PASSWORD);
    }
    
    @Override
    protected void dropDatabase() throws Exception {
        H2DBMock.this.executeUpdate("DROP ALL OBJECTS");
    }

    @Override
    public void setSchema(String schema) throws Exception {
        if (!StringUtils.isNullOrEmpty(schema)) {
            this.executeUpdate("USE SCHEMA " + schema + ";");
        } else {
            this.executeUpdate("SET SCHEMA PUBLIC;");
        }
    }
}
