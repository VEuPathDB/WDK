package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TableField extends Field {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.TableField");

    private String queryTwoPartName;
    private Query query;
    private Map<String, AttributeField> attributeFields;

    public TableField() {
        super();
        attributeFields = new LinkedHashMap<String, AttributeField>();
    }

    Query getQuery() {
        return query;
    }

    void setQuery(Query query) {
        this.query = query;
    }

    public void setQueryRef(String queryRef) {
        this.queryTwoPartName = queryRef;
    }

    public String getQueryRef() {
        return queryTwoPartName;
    }

    public void addAttributeField(AttributeField attributeField) {
        attributeFields.put(attributeField.getName(), attributeField);
    }

    public AttributeField[] getAttributeFields() {
        AttributeField[] array = new AttributeField[attributeFields.size()];
        attributeFields.values().toArray(array);
        return array;
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return new LinkedHashMap<String, AttributeField>(attributeFields);
    }

    /*
     * (non-Javadoc) Should never be called, but is necessary because TableField
     * implements FieldI.
     * 
     * @see org.gusdb.wdk.model.FieldI#getTruncateTo()
     */
    public int getTruncateTo() {
        throw new RuntimeException("getTruncate does not apply to TableField");
    }

    void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve Query
        query = (Query) model.resolveReference(queryTwoPartName,
                getName(), this.getClass().getName(), "tableQueryRef");
        Column[] columns = query.getColumns();
        for (Column column : columns) {
            AttributeField field = attributeFields.get(column.getName());
            if (field != null && field instanceof ColumnAttributeField) {
                ((ColumnAttributeField) field).setColumn(column);
            } else {
                String message = "The Column of name '" + column.getName()
                        + "' doesn't match with any ColumnAttributeField in"
                        + " TableField " + getName() + ".";
                logger.finest(message);
                throw new WdkModelException(message);
            }
        }
    }
}
