package org.javagems.dbunit;

import static org.junit.Assert.*;

import org.javagems.dbunit.DBMockBuilder.DBEngine;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class TestDBMockCloseConnections {

    @ClassRule
    public static DBMock DATABASE = DBMockBuilder.newInstance()
            .setUseSchemas(false)
            .build(DBEngine.H2);

    @Rule
    public DBMock database = DATABASE;

    private void commonTest() throws Exception {
        assertFalse(DATABASE.getConnection().isClosed());
    }

    @Test
    public void testOne() throws Exception {
        this.commonTest();        
        DATABASE.getConnection().close();
    }

    @Test
    public void testTwo() throws Exception {
        this.commonTest();
        DATABASE.getConnection().close();
    }

}
