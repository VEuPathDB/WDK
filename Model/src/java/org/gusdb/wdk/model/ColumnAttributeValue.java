/**
 * 
 */
package org.gusdb.wdk.model;

import java.sql.Clob;
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
        if (value == null) return value;

        // handle the types that don't provide proper toString() implementation
        if (value instanceof Clob) {
            Clob clob = (Clob) value;
            return clob.getSubString(1, (int) clob.length());
        } else return value;
    }

}
