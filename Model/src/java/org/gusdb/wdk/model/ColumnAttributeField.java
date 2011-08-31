package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.query.Column;
import org.json.JSONException;

public class ColumnAttributeField extends AttributeField {

    private Column column;

    public ColumnAttributeField() {
        super();
        // initialize the optional field values
    }

    /**
     * @return Returns the column.
     */
    public Column getColumn() {
        return this.column;
    }

    /**
     * @param column
     *            The column to set.
     * @throws WdkModelException
     */
    void setColumn(Column column) {
        this.column = column;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.Field#presolveReferences(org.gusdb.wdk.model.WdkModel
     * )
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        super.resolveReferences(wdkModel);

        // verify the name
        if (!name.equals(column.getName()))
            throw new WdkModelException("The name of the ColumnAttributeField"
                    + " '" + name + "' does not match the column name '"
                    + column.getName() + "'");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeField#getDependents()
     */
    @Override
    protected Collection<AttributeField> getDependents() {
        return new ArrayList<AttributeField>();
    }

    @Override
    public Map<String, ColumnAttributeField> getColumnAttributeFields() {
        Map<String, ColumnAttributeField> fields = new LinkedHashMap<String, ColumnAttributeField>();
        fields.put(name, this);
        return fields;
    }
}
