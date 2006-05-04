package org.gusdb.wdk.model;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class ColumnAttributeField extends AttributeField implements Serializable {

    private static final long serialVersionUID = 6599899173932240144L;
    private static Logger logger = Logger.getLogger(ColumnAttributeField.class);
    
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
        // TEST
//        if (column.getName().equalsIgnoreCase("score"))
//            logger.debug("Field ID: " + hashCode() + ", Column ID: " + column.hashCode());
        
        this.column = column;
    }
    
    Query getQuery() {
        return column.getQuery();
    }
}
