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

public class DynamicAttributeSet extends WdkModelBase {

    /**
     * 
     */
    private static final long serialVersionUID = -1373806354317917813L;
    // private static Logger logger =
    // Logger.getLogger(DynamicAttributeSet.class);

    private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
    private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<String, AttributeField>();

    private Question question;

    public DynamicAttributeSet() {}

    public void addAttributeField(AttributeField attributeField) {
        attributeFieldList.add(attributeField);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        buf.append("  dynamicAttributes:" + newline);

        for (String attrName : attributeFieldMap.keySet()) {
            buf.append("    " + attrName + newline);
        }
        return buf.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // package methods //
    // /////////////////////////////////////////////////////////////////

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return question;
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return getAttributeFieldMap(FieldScope.ALL);
    }

    public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope) {
        Map<String, AttributeField> map = new LinkedHashMap<String, AttributeField>();
        for (AttributeField field : attributeFieldMap.values()) {
            if (scope.isFieldInScope(field)) {
                map.put(field.getName(), field);
            }
        }
        return map;
    }

    // /////////////////////////////////////////////////////////////////
    // private methods //
    // /////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the attribute fields
        for (AttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();

                // the attribute name must be unique
                if (attributeFieldMap.containsKey(fieldName))
                    throw new WdkModelException("DynamicAttributes contain a "
                            + "duplicate attribute '" + fieldName + "'");

                attributeFieldMap.put(fieldName, field);
            }
        }
        attributeFieldList = null;

        // check if weight is defined, if not, create it
        if (!attributeFieldMap.containsKey(Utilities.COLUMN_WEIGHT)) {
            // always have weight as a dynamic attribute
            ColumnAttributeField attribute = new ColumnAttributeField();
            attribute.setName(Utilities.COLUMN_WEIGHT);
            attribute.setDisplayName("Weight");
            attribute.setInternal(false);
            attribute.setInReportMaker(true);
            attribute.setSortable(true);
            // attribute.setHelp("User-defined integer value; in a search strategy, unions and intersects will sum the weights, giving higher scores to items found in multiple searches. ");
            attribute
                    .setHelp("The *search result weight*. This is the sum of "
                            + "the weights you specified for individual searches that "
                            + "found this record.   The more searches that found this "
                            + "record the higher will be its weight.  To give a search "
                            + "a weight, click \"Use Weights\" on the search page, and "
                            + "provide the desired weight.");

            attribute.excludeResources(projectId);

            attributeFieldMap.put(Utilities.COLUMN_WEIGHT, attribute);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        RecordClass recordClass = question.getRecordClass();
        Query dynamicAttributeQuery = question.getDynamicAttributeQuery();
        Map<String, Column> columns = dynamicAttributeQuery.getColumnMap();

        // make sure the dynamic attribute set doesn't have duplicated
        // attributes
        Map<String, AttributeField> fields = recordClass.getAttributeFieldMap();
        for (String fieldName : attributeFieldMap.keySet()) {
            if (fields.containsKey(fieldName))
                throw new WdkModelException("Dynamic attribute field "
                        + fieldName + " in question " + question.getFullName()
                        + " already exists in recordClass "
                        + recordClass.getFullName());
            AttributeField field = attributeFieldMap.get(fieldName);
            field.setRecordClass(recordClass);
            field.setContainer(question);
            if (field instanceof ColumnAttributeField) {
                // need to set the column before resolving references
                Column column = columns.get(fieldName);
                if (column == null)
                    throw new WdkModelException("Dynamic column attribute of "
                            + "question [" + question.getFullName() + "] is "
                            + "defined, but the underlying id query doesn't"
                            + " have the column '" + fieldName + "'");
                ((ColumnAttributeField) field).setColumn(column);
            }
            field.resolveReferences(wodkModel);
        }
    }
}
