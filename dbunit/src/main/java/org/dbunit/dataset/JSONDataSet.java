package org.dbunit.dataset;

import com.google.gson.Gson;

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// http://code.google.com/p/dbunit-mongodb/source/browse/adlib_dbunit_mongo/trunk/src/main/java/org/adclear/dbunit/json/JSONDataSet.java?r=3

public class JSONDataSet extends AbstractDataSet {
    private JSONITableParser tableParser = new JSONITableParser();
    private List<ITable> tables;

    public JSONDataSet(File file) {
        tables = tableParser.getTables(file);
    }

    public JSONDataSet(InputStream is) {
        tables = tableParser.getTables(is);
    }

    @Override
    protected ITableIterator createIterator(boolean reverse) throws DataSetException {
        return new DefaultTableIterator(tables.toArray(new ITable[tables.size()]));
    }

    private class JSONITableParser {

        private Gson mapper = new Gson();

        public List<ITable> getTables(File jsonFile) {
            try {
                return getTables(new FileInputStream(jsonFile));
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @SuppressWarnings("unchecked")
        public List<ITable> getTables(InputStream jsonStream) {
            List<ITable> rtables = new ArrayList<ITable>();

            Map<String, Object> dataset = mapper.fromJson(new InputStreamReader(jsonStream), Map.class);

            for (Map.Entry<String, Object> entry : dataset.entrySet()) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) entry.getValue();
                ITableMetaData meta = getMetaData(entry.getKey(), rows);
                DefaultTable table = new DefaultTable(meta);
                int rowIndex = 0;
                for (Map<String, Object> row : rows) {
                    fillRow(table, row, rowIndex++);
                }
                rtables.add(table);
            }

            return rtables;
        }

        private ITableMetaData getMetaData(String tableName, List<Map<String, Object>> rows) {
            Set<String> columns = new LinkedHashSet<String>();
            for (Map<String, Object> row : rows) {
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    columns.add(column.getKey());
                }
            }
            List<Column> list = new ArrayList<Column>(columns.size());
            for (String s : columns) {
                list.add(new Column(s, DataType.UNKNOWN));
            }
            return new DefaultTableMetaData(tableName, list.toArray(new Column[list.size()]));
        }

        private void fillRow(DefaultTable table, Map<String, Object> row, int rowIndex) {
            try {
                table.addRow();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    table.setValue(rowIndex, column.getKey(), column.getValue());

                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
