package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.json.JSONException;

public class TableField extends Field implements AttributeFieldContainer {

    //private static final Logger logger = Logger.getLogger(TableField.class);

    private String queryTwoPartName;
    private Query query;
    private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
    private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<String, AttributeField>();

    private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
    private String description;

    public Query getQuery() {
        return query;
    }

    void setQuery(Query query) {
        this.query = query;
    }

    public void setQueryRef(String queryRef) {
        this.queryTwoPartName = queryRef;
    }

    public void addAttributeField(AttributeField attributeField) {
        attributeField.setRecordClass(recordClass);
        attributeField.setContainer(this);
        attributeFieldList.add(attributeField);
    }

    public String getQueryRef() {
        return queryTwoPartName;
    }

    public AttributeField[] getAttributeFields() {
        return getAttributeFields(FieldScope.ALL);
    }

    public AttributeField[] getAttributeFields(FieldScope scope) {
        Map<String, AttributeField> fieldMap = getAttributeFieldMap(scope);
        AttributeField[] array = new AttributeField[fieldMap.size()];
        fieldMap.values().toArray(array);
        return array;
    }

    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeFieldContainer#getAttributeFieldMap()
     */
    public Map<String, AttributeField> getAttributeFieldMap() {
        return getAttributeFieldMap(FieldScope.ALL);
    }

    public String getDescription() {
        return (description == null) ? "" : description;
    }

    public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope) {
        Map<String, AttributeField> map = new LinkedHashMap<String, AttributeField>();
        for (AttributeField field : attributeFieldMap.values()) {
            if ((scope == FieldScope.ALL)
                    || (scope == FieldScope.NON_INTERNAL && !field.isInternal())
                    || (scope == FieldScope.REPORT_MAKER && field.isInReportMaker()))
                map.put(field.getName(), field);
        }
        return map;
    }

    public AttributeField getAttributeField(String fieldName) {
        return attributeFieldMap.get(fieldName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.Field#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resolved) return;
        super.resolveReferences(wdkModel);
        
        // resolve Query
        Query query = (Query) wdkModel.resolveReference(queryTwoPartName);

        // validate the table query
        recordClass.validateBulkQuery(query);

        // prepare the query and add primary key params
        String[] paramNames = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        query = RecordClass.prepareQuery(wdkModel, query, paramNames);
        this.query = query;

        Column[] columns = query.getColumns();
        for (Column column : columns) {
            AttributeField field = attributeFieldMap.get(column.getName());
            if (field != null && field instanceof ColumnAttributeField) {
                ((ColumnAttributeField) field).setColumn(column);
            } // else, it's okay to have unmatched columns
        }
        resolved = true;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);
        
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The table field " + name
                            + " of recordClass " + recordClass.getFullName()
                            + " has more than one description for project "
                            + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;

        // exclude attributes
        for (AttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFieldMap.containsKey(fieldName))
                    throw new WdkModelException("The attributeField "
                            + fieldName + " is duplicated in table "
                            + this.name);
                attributeFieldMap.put(fieldName, field);
            }
        }
        attributeFieldList = null;
    }
}
