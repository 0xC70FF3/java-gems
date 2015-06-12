package org.dbunit.operation;

import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;

public abstract class SchemaSpecificDatabaseOperation {

    public static final DatabaseOperation CLEAN_INSERT(String schema) {
        return new CompositeOperation(
                new SchemaSpecificDeleteAllOperation(schema),
                new SchemaSpecificInsertOperation(schema));
    }
}
