package org.javagems.dbunit;

import com.mysql.jdbc.StringUtils;

import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.JSONDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.SchemaSpecificDatabaseOperation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://danhaywood.com/2011/12/20/db-unit-testing-with-dbunit-json-hsqldb-and-junit-rules/
 * https://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner.html
 */

public abstract class DBMock implements TestRule {
    private final class StartUpStatement extends Statement {
        private final Statement base;

        private StartUpStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            try {                    
                DBMock.this.startUp();
                base.evaluate();
            } finally {
                DBMock.this.shutDown();
            }
        }
    }

    private final class SetUpStatement extends Statement {
        private final Statement base;
        private final Description description;

        private SetUpStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                DBMock.this.setUp(description);
                base.evaluate();
            } finally {
                DBMock.this.tearDown();
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Schemas {
        String[] value();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Tables {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Insert {
        String[] value();
    }

    private Class<?> resourceBase;
    private IDatabaseTester databaseTester;
    private IDatabaseConnection dbUnitConnection;
    private Connection connection;
    private java.sql.Statement statement;
    private boolean useSchemas;
    private String schemaEscapePattern;

    protected DBMock(boolean useSchemas) {
        this.useSchemas = useSchemas;
        this.schemaEscapePattern = "?";
    }

    protected abstract IDatabaseTester newDatabaseTesterInstance() throws Exception;
    
    protected void startUp() throws Exception {   
    }
    
    protected void setUp(final Description description) throws Exception {
        Tables tables = description.getAnnotation(Tables.class);
        Insert inserts = description.getAnnotation(Insert.class);
        Schemas schemas = description.getAnnotation(Schemas.class);
        setUp(description.getTestClass(),
                tables == null ? null : tables.value(),
                inserts == null ? null : inserts.value(),
                schemas == null ? null : schemas.value());
    }
    
    protected void setUp(Class<?> resourceBase, String[] tables, String[] inserts, String[] schemas) throws Exception {
        this.resourceBase = resourceBase;
        this.databaseTester = this.newDatabaseTesterInstance();
        this.dbUnitConnection = this.databaseTester.getConnection();
        this.dbUnitConnection.getConfig().setProperty(
                DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES,
                true);
        this.dbUnitConnection.getConfig().setProperty(
                DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                new MySqlDataTypeFactory());
        
        
        this.connection = this.dbUnitConnection.getConnection();
        this.statement = this.connection.createStatement();
        
        if (tables != null) {
            for (String filename : tables) {
                String script = new Scanner(this.getResourceFile(filename)).useDelimiter("\\A").next();

                if (this.useSchemas) {
                    this.guessSchemaAndCreateCreateIfNotExists(script);
                }
                for (String subscript : script.split(";")) {
                    if (!StringUtils.isNullOrEmpty(subscript.trim())) {
                        this.executeUpdate(subscript + ";");
                    }
                }
            }
        }

        if (inserts != null) {
            for (String filename : inserts) {
                File file = this.getResourceFile(filename);
                IDataSet ds = this.readFileToDataSet(file);
                if (this.useSchemas) {
                    SchemaSpecificDatabaseOperation.CLEAN_INSERT(this.guessSchema(file)).execute(dbUnitConnection, ds);
                } else {                
                    DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, ds);
                }
            }
        }
        
        if (schemas != null) {
            for (String schema : schemas) {
                this.executeUpdate(
                    "CREATE SCHEMA IF NOT EXISTS "
                            + schema
                            + ";");
            }
        }
    }

    private String guessSchemaAndCreateCreateIfNotExists(String script) throws Exception {
        Pattern pattern = Pattern.compile("([^\\s^\\.]*)\\.");
        Matcher matcher = pattern.matcher(script);
        if (matcher.find()) {
            this.executeUpdate(
                    "CREATE SCHEMA IF NOT EXISTS "
                            + matcher.group(1)
                            + ";");

            this.schemaEscapePattern = matcher.group(1).replaceFirst("(\\w)+", "?");
            return matcher.group(1);
        }
        return null;
    }

    private String guessSchema(File datasetResourceFile) throws Exception {
        return this.schemaEscapePattern.replace("?",
                datasetResourceFile.getName().replaceFirst("[.][^.]+$", ""));
    }

    protected void shutDown() throws Exception {    
    }

    protected abstract void dropDatabase() throws Exception ;
    
    protected void tearDown() throws Exception  {
        if (!this.connection.isClosed()) {
            this.dropDatabase();
        }
        if (this.databaseTester != null) {
            this.databaseTester.onTearDown();
        }
        if (this.connection != null) {
            this.connection.close();
        }
        if (this.dbUnitConnection != null) {
            this.dbUnitConnection.close();
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {        
        if(description.getChildren().isEmpty()){        
            return new SetUpStatement(base, description);
        } else {
            return new StartUpStatement(base);
        }
    }

    public abstract void setSchema(String schema) throws Exception;

    public Connection getConnection() {
        return this.connection;
    }

    public int executeUpdate(String sql) throws Exception {
        return this.statement.executeUpdate(sql);
    }

    public ResultSet executeQuery(String sql) throws Exception {
        return this.statement.executeQuery(sql);
    }

    public boolean execute(String sql) throws Exception {
        return this.statement.execute(sql);
    }

    private File getResourceFile(String datasetResourceFilename) {
        if (this.resourceBase != null) {
            URL resource = this.resourceBase.getResource(datasetResourceFilename);
            if (resource != null) {
                return new File(resource.getFile());
            }
            throw new IllegalArgumentException("Resource '" + datasetResourceFilename + "' not found." + " / resource="+resource + "/" + resourceBase.getName());
        }
        return new File(datasetResourceFilename);
    }

    public IDataSet readFileToDataSet(File datasetResourceFile) throws Exception {
        if (datasetResourceFile.getName().endsWith(".flat.xml")) {
            return new FlatXmlDataSetBuilder().setColumnSensing(true).build(datasetResourceFile);
        } else if (datasetResourceFile.getName().endsWith(".xml")) {
            return new XmlDataSet(new FileInputStream(datasetResourceFile));
        } else if (datasetResourceFile.getName().endsWith(".xls")) {
            return new XlsDataSet(new FileInputStream(datasetResourceFile));
        } else if (datasetResourceFile.getName().endsWith(".json")) {
            return new JSONDataSet(new FileInputStream(datasetResourceFile));
        } else {
            return new CsvDataSet(datasetResourceFile);
        }
    }

    public ITable createQueryTable(String string, String string2) throws Exception {
        return this.dbUnitConnection.createQueryTable(string, string2);
    }

    public void writeQueryTablesToFile(Map<String, String> queries, File output) throws Exception {
        QueryDataSet dataSet = new QueryDataSet(this.dbUnitConnection);
        for (String table : queries.keySet()) {
            dataSet.addTable(table, queries.get(table));
        }
        if (output.getName().endsWith(".xml")) {
            FlatXmlDataSet.write(dataSet, new FileOutputStream(output));
        } else if (output.getName().endsWith(".xls")) {
            XlsDataSet.write(dataSet, new FileOutputStream(output));
        } else {
            new CsvDataSetWriter(output).write(dataSet);
        }
    }
}
