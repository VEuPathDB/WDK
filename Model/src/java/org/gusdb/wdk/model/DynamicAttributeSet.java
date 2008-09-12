package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.query.Query;

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
            if ((scope == FieldScope.ALL)
                    || (scope == FieldScope.NON_INTERNAL && !field.isInternal())
                    || (scope == FieldScope.REPORT_MAKER && field.isInReportMaker()))
                map.put(field.getName(), field);
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
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
            field.setContainer(recordClass);
            if (field instanceof ColumnAttributeField) {
                // need to set the column before resolving references
                Column column = columns.get(fieldName);
                ((ColumnAttributeField) field).setColumn(column);
            }
            field.resolveReferences(wodkModel);
        }
    }
}
