package org.dbunit.operation;

import com.mysql.jdbc.StringUtils;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.ResultSetTableMetaData;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.operation.AbstractBatchOperation;
import org.dbunit.operation.AbstractOperation;
import org.dbunit.operation.OperationData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SchemaSpecificInsertOperation extends AbstractOperation {

    private final String schema;

    SchemaSpecificInsertOperation(String schema) {
        if (StringUtils.isNullOrEmpty(schema)) {
            throw new IllegalArgumentException("Schema can not be null.");
        }
        this.schema = schema;
    }

    @Override
    public void execute(IDatabaseConnection connection, IDataSet dataSet) throws DatabaseUnitException, SQLException {
        DatabaseConfig databaseConfig = connection.getConfig();
        IStatementFactory statementFactory = (IStatementFactory) databaseConfig.getProperty(DatabaseConfig.PROPERTY_STATEMENT_FACTORY);

        IPreparedBatchStatement statement = null;
        try {
            ITableIterator iterator = dataSet.iterator();
            while (iterator.next()) {
                ITable table = iterator.getTable();

                if (AbstractBatchOperation.isEmpty(table)) {
                    continue;
                }

                ITableMetaData tableMetaData = table.getTableMetaData();
                
                ResultSet rs = connection.getConnection().createStatement().executeQuery(
                        new StringBuilder()
                                .append("select * from ")
                                .append(this.getQualifiedName(this.schema, tableMetaData.getTableName(), connection))
                                .toString());
                ITableMetaData metaData = new ResultSetTableMetaData(tableMetaData.getTableName(), rs, connection, true);

                OperationData operationData = this.getOperationData(metaData, tableMetaData, connection);
                statement = statementFactory.createPreparedBatchStatement(operationData.getSql(), connection);

                for (int row = 0; row < table.getRowCount(); row++) {
                    for (Column column : operationData.getColumns()) {
                        try {
                            statement.addValue(table.getValue(row, column.getColumnName()), column.getDataType());
                        } catch (NoSuchColumnException e) {
                            // the input do not contains value for this column. skip.
                        } catch (TypeCastException e) {
                            throw new TypeCastException("Error casting value for table '" +
                                    table.getTableMetaData().getTableName() + "' and column '" +
                                    column.getColumnName() + "'", e);
                        }
                    }
                    statement.addBatch();
                }

                statement.executeBatch();
                statement.clearBatch();
            }

        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public OperationData getOperationData(ITableMetaData metaData,ITableMetaData tableMetaData, IDatabaseConnection connection) throws DataSetException {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ");
        sql.append(getQualifiedName(this.schema, metaData.getTableName(), connection));

        sql.append(" (");
        String columnSeparator = "";
        
        StringBuilder values = new StringBuilder();
        for (Column column : metaData.getColumns()) {
            try {
                tableMetaData.getColumnIndex(column.getColumnName()); 
                
                String columnName = getQualifiedName(null, column.getColumnName(), connection);
                sql.append(columnSeparator);
                sql.append(columnName);
                
                values.append(columnSeparator);
                values.append("?");
                columnSeparator = ", ";
            } catch (NoSuchColumnException ex) {         
                // the input do not contains value for this column. skip.
            }
        }

        sql.append(") values (");
        sql.append(values);
        sql.append(")");

        return new OperationData(sql.toString(), metaData.getColumns());
    }
}
