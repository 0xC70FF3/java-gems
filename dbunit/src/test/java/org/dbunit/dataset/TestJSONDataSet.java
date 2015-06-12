package org.dbunit.dataset;

import static org.junit.Assert.*;

import org.dbunit.dataset.JSONDataSet;
import org.junit.Test;

import java.io.File;

public class TestJSONDataSet {
    @Test
    public void testJSONDataSetFile() throws Exception {
        JSONDataSet ds = new JSONDataSet(
            new File(this.getClass().getResource("customer.json").getFile())
        );
        
        assertArrayEquals(new String[]{"customer"}, ds.getTableNames());        
        assertEquals(2, ds.getTable("customer").getRowCount());
    }
    
    @Test
    public void testJSONDataSetInputStream() throws Exception {
        JSONDataSet ds = new JSONDataSet(
            this.getClass().getResourceAsStream("customer.json")
        );
        
        assertArrayEquals(new String[]{"customer"}, ds.getTableNames());        
        assertEquals(2, ds.getTable("customer").getRowCount());
    }
    
    @Test(expected=RuntimeException.class)
    public void testJsonDataSetException () throws Exception {
        new JSONDataSet(new File("nosuchfile"));
    }
}
