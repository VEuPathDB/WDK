package org.gusdb.wdk.model;

public class ColumnAttributeField extends AttributeField {

    private Column column;

    public ColumnAttributeField() {
        super();
        // initialize the optional field values
    }

    /**
     * @return Returns the column.
     */
    Column getColumn() {
        return this.column;
    }

    /**
     * @param column The column to set.
     */
    void setColumn(Column column) {
        this.column = column;
    }
    
    Query getQuery() {
        return column.getQuery();
    }
}
