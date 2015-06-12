package org.dbunit.operation;

import com.mysql.jdbc.StringUtils;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.operation.AbstractOperation;

import java.sql.SQLException;

public class SchemaSpecificDeleteAllOperation extends AbstractOperation {

    private final String schema;

    SchemaSpecificDeleteAllOperation(String schema) {
        if (StringUtils.isNullOrEmpty(schema)) {
            throw new IllegalArgumentException("Schema can not be null.");
        }
        this.schema = schema;
    }

    @Override
    public void execute(IDatabaseConnection connection, IDataSet dataSet) throws DatabaseUnitException, SQLException {
        DatabaseConfig databaseConfig = connection.getConfig();
        IStatementFactory statementFactory = (IStatementFactory) databaseConfig.getProperty(DatabaseConfig.PROPERTY_STATEMENT_FACTORY);

        IBatchStatement statement = statementFactory.createBatchStatement(connection);
        try {
            int count = 0;

            ITableIterator iterator = dataSet.iterator();
            while (iterator.next()) {
                String tableName = iterator.getTableMetaData().getTableName();

                StringBuilder sql = new StringBuilder();
                sql.append("delete from ");
                sql.append(this.getQualifiedName(this.schema, tableName, connection));
                statement.addBatch(sql.toString());

                count++;
            }

            if (count > 0) {
                statement.executeBatch();
                statement.clearBatch();
            }
        } finally {
            statement.close();
        }
    }
}
