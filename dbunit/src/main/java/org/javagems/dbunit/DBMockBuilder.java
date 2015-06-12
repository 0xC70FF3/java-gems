package org.javagems.dbunit;

public class DBMockBuilder {
    
    public enum DBEngine {
        H2,
        MySQL};
    
    private boolean useSchemas = false;
    private String databaseName = null;
    private int port = -1;
    private DBMockBuilder() {}

    public DBMockBuilder setUseSchemas(boolean useSchemas) {
        this.useSchemas = useSchemas;
        return this;
    }
    
    public DBMockBuilder setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;            
    }
    
    public DBMockBuilder setPort(int port) {
        this.port = port;
        return this;            
    }
    
    public DBMock build(DBEngine engine) {
        switch (engine) {
            case H2:
                return new H2DBMock(useSchemas, databaseName);
            case MySQL:
                return new MySQLDBMock(useSchemas, databaseName, port);                
            default:
                throw new IllegalArgumentException("Engine '" + engine.name() + "' not supported.");
        }
    }

    public static DBMockBuilder newInstance() {
        return new DBMockBuilder();
    }
}
