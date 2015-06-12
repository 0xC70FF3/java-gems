package org.javagems.dbunit;

import static org.junit.Assert.*;

import org.javagems.dbunit.DBMock.Insert;
import org.javagems.dbunit.DBMock.Tables;
import org.javagems.dbunit.DBMockBuilder.DBEngine;

import org.apache.commons.io.FileUtils;
import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class TestMySQLDBMock {

    @ClassRule
    public static DBMock DATABASE = DBMockBuilder.newInstance()
            .setUseSchemas(false)
            .build(DBEngine.MySQL);

    @Rule
    public DBMock database = DATABASE;

    private void commonTest() throws Exception {
        assertFalse(DATABASE.getConnection().isClosed());

        ResultSet rs = DATABASE.executeQuery("select * from `customer` where id = 325");
        assertTrue(rs.next());
        assertEquals(null, rs.getString("initial"));

        DATABASE.executeUpdate("update customer set last_name='Bloggs' where id=325");

        ResultSet rs2 = DATABASE.executeQuery("select * from `customer` where id = 325");
        assertTrue(rs2.next());
        assertEquals(null, rs2.getString("initial"));
        assertEquals("Bloggs", rs2.getString("last_name"));

        ITable actualTable =
                DATABASE.createQueryTable("customer", "select * from customer order by id");

        ITable expectedTable = DATABASE.readFileToDataSet(
                new File(this.getClass().getResource("customer-updated.flat.xml").getFile())
                ).getTable("customer");
        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Tables("customer.sql")
    @Insert("customer")
    @Test
    public void testCsvImport() throws Exception {
        this.commonTest();
    }

    @Tables("customer.sql")
    @Insert("customer.xls")
    @Test
    public void testXlsImport() throws Exception {
        this.commonTest();
    }

    @Tables("customer.sql")
    @Insert("customer.flat.xml")
    @Test
    public void testFlatXmlImport() throws Exception {
        this.commonTest();
    }

    @Tables("customer.sql")
    @Insert("customer.xml")
    @Test
    public void testXmlImport() throws Exception {
        this.commonTest();
    }

    @Tables("customer.sql")
    @Insert("customer")
    @Test
    public void testwriteQueryTablesToFile() throws Exception {
        Map<String, String> queries = new HashMap<String, String>();
        queries.put("customer", "select * from customer order by id");

        File output = new File("output/");
        DATABASE.writeQueryTablesToFile(queries, output);
        assertTrue(output.exists());
        assertTrue(output.isDirectory());
        File csv = new File(output, "customer.csv");
        assertTrue(csv.exists());
        assertTrue(csv.isFile());
        assertEquals(
                FileUtils.readFileToString(
                        new File(this.getClass().getResource("customer-expected-export.csv").getFile())),
                FileUtils.readFileToString(csv));
        File table_ordering = new File(output, "table-ordering.txt");
        assertTrue(table_ordering.exists());
        assertTrue(table_ordering.isFile());
        assertEquals(
                "customer\n",
                FileUtils.readFileToString(table_ordering));
        FileUtils.deleteDirectory(output);

        File outputXml = new File("output.xml");
        DATABASE.writeQueryTablesToFile(queries, outputXml);
        assertTrue(outputXml.exists());
        assertTrue(outputXml.isFile());
        FileUtils.deleteQuietly(outputXml);

        File outputXls = new File("output.xls");
        DATABASE.writeQueryTablesToFile(queries, outputXls);
        assertTrue(outputXls.exists());
        assertTrue(outputXls.isFile());
        FileUtils.deleteQuietly(outputXls);
    }
}
