package org.javagems.dbunit;

import static org.junit.Assert.*;

import org.javagems.dbunit.DBMockBuilder.DBEngine;
import org.javagems.dbunit.DBMock.Insert;
import org.javagems.dbunit.DBMock.Schemas;
import org.javagems.dbunit.DBMock.Tables;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.ResultSet;

public class TestMySQLDBMockWithSchemas {

    @ClassRule
    public static DBMock DATABASE = DBMockBuilder.newInstance()
            .setUseSchemas(true)
            .build(DBEngine.MySQL);
    
    @Rule
    public DBMock database = DATABASE;

    @Tables("mydb.customer.sql")
    @Insert("mydb.json")
    @Test
    public void testSchema() throws Exception {
        ResultSet rs = DATABASE.executeQuery("SELECT * FROM mydb.customer WHERE id = 2");
        assertTrue(rs.next());
        assertEquals(null, rs.getString("initial"));
        assertEquals("Mary", rs.getString("first_name"));
        assertEquals("Jones", rs.getString("last_name"));
    }

    @Tables("mydb.customer.sql")
    @Insert("mydb.json")
    @Test
    public void testUseSchema() throws Exception {
        DATABASE.execute("USE `mydb`");

        ResultSet rs = DATABASE.executeQuery("SELECT * FROM customer WHERE id = 2");
        assertTrue(rs.next());
        assertEquals(null, rs.getString("initial"));
        assertEquals("Mary", rs.getString("first_name"));
        assertEquals("Jones", rs.getString("last_name"));

        DATABASE.execute("USE mydb");

        rs = DATABASE.executeQuery("SELECT * FROM customer WHERE id = 2");
        assertTrue(rs.next());
        assertEquals(null, rs.getString("initial"));
        assertEquals("Mary", rs.getString("first_name"));
        assertEquals("Jones", rs.getString("last_name"));
    }

    @Schemas("mydb")
    @Test(expected = com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException.class)
    public void testCannotUseSchema() throws Exception {
        DATABASE.execute("USE doesnotexists");
    }

}
