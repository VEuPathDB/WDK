/**
 * 
 */
package org.gusdb.wdk.model;

import java.sql.SQLException;

/**
 * @author Jerric Gao
 * 
 */
public class ColumnAttributeValue extends AttributeValue {

    /**
     * @param instance
     * @param field
     */
    public ColumnAttributeValue(ColumnAttributeField field, Object value) {
        super(field);
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeValue#getValue()
     */
    @Override
    public Object getValue() throws WdkModelException, SQLException {
        return Utilities.parseValue(value);
    }

}
