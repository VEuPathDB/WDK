package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gusdb.wdk.model.query.Column;

public class ColumnAttributeField extends AttributeField {

    private static final long serialVersionUID = 6599899173932240144L;
    // private static Logger logger =
    // Logger.getLogger(ColumnAttributeField.class);

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
     * @param column
     *                The column to set.
     * @throws WdkModelException
     */
    void setColumn(Column column) {
        this.column = column;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // nothing to be exclude, do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Field#presolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
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
    public Collection<AttributeField> getDependents() {
        List<AttributeField> dependents = new ArrayList<AttributeField>();
        dependents.add(this);
        return dependents;
    }
}
