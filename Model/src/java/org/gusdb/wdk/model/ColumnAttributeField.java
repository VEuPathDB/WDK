package org.gusdb.wdk.model;


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
     * The column to set.
     */
    void setColumn(Column column) {
        this.column = column;
    }

    Query getQuery() throws WdkModelException {
        if (column == null)
            throw new WdkModelException("Null column in Column Attribute: "
                    + name + " This may "
                    + "happen if you have declared a column attribute in the "
                    + "record, but the underlying query doesn't have that "
                    + "column declared");
        return column.getQuery();
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
}
