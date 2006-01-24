package org.gusdb.wdk.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.implementation.SqlQuery;

public class DynamicAttributeSet {

    public final static String RESULT_TABLE = "RESULTTABLE";

    private Map<String, AttributeField> attributesFieldMap;
    private RecordClass recordClass;
    private Query attributesQuery;
    private Set<String> columnAttributeFieldNames;

    public DynamicAttributeSet() {
        attributesFieldMap = new HashMap<String, AttributeField>();
        columnAttributeFieldNames = new HashSet<String>();
    }

    public void addAttributeField(AttributeField attributeField)
            throws WdkModelException {
        String name = attributeField.getName();
        // the attribute name must be unique
        if (attributesFieldMap.containsKey(name))
            throw new WdkModelException(
                    "DynamicAttributes contain a duplicate attribute '" + name
                            + "'");

        attributesFieldMap.put(name, attributeField);

        // check if it's a column attribute. this kind of attribute must be
        // matched with a column
        if (attributeField instanceof ColumnAttributeField)
            columnAttributeFieldNames.add(name);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        buf.append("  dynamicAttributes:" + newline);

        for (String attrName : attributesFieldMap.keySet()) {
            buf.append("    " + attrName + newline);
        }
        return buf.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // package methods //
    // /////////////////////////////////////////////////////////////////

    void setResources(WdkModel model) throws WdkModelException {
        attributesQuery.setResources(model);
    }

    void setQuestion(Question question) throws WdkModelException {
        this.recordClass = question.getRecordClass();

        // validate attributeFields
        String message = "dynamicAttributes of question "
                + question.getFullName();
        Map<String, AttributeField> rsAttributeFields = recordClass.getAttributeFieldMap();
        for (String attributeName : attributesFieldMap.keySet()) {
            // check the uniqueness of attributeFields in the RecordClass level
            if (rsAttributeFields.containsKey(attributeName))
                throw new WdkModelException(message
                        + " introduces an attribute '" + attributeName
                        + "' that recordClass " + recordClass.getName()
                        + " already has");
        }

        // create and validate the attribute query
        initAttributesQuery(question);
    }

    Map<String, AttributeField> getAttributeFields() {
        return new HashMap<String, AttributeField>(attributesFieldMap);
    }

    private Query getAttributesQuery() {
        return attributesQuery;
    }

    // /////////////////////////////////////////////////////////////////
    // private methods //
    // /////////////////////////////////////////////////////////////////

    private void initAttributesQuery(Question question)
            throws WdkModelException {
        attributesQuery = new SqlQuery();
        attributesQuery.setName(question.getFullName() + ".DynAttrs");
        StringParam param = new StringParam();
        param.setName(RecordClass.PRIMARY_KEY_NAME);
        attributesQuery.addParam(param);
        param = new StringParam();
        param.setName(RESULT_TABLE);
        attributesQuery.addParam(param);

        // get columns from Question.Query, and check the paired Column
        String message = "dynamicAttributes of question "
                + question.getFullName();
        Map<String, Column> columnMap = question.getQuery().getColumnMap();
        // check if all column names appear in column attributes
        Set<String> names = columnMap.keySet();
        for (String name : names) {
            if (!columnAttributeFieldNames.contains(name))
                throw new WdkModelException(message
                        + " doesn't contain an attribute '" + name
                        + "' that appears in query "
                        + question.getQuery().getFullName());
        }
        // check if all column attribute names appear in columns
        for (String name : columnAttributeFieldNames) {
            if (!columnMap.containsKey(name))
                throw new WdkModelException(message
                        + " contains an attribute '" + name
                        + "' that does not appear in query "
                        + question.getQuery().getFullName());

            // associate columns with column attribute fields
            Column column = columnMap.get(name);
            ColumnAttributeField field = (ColumnAttributeField) attributesFieldMap.get(name);
            field.setColumn(column);
        }

        StringBuffer sqlSelectBuf = new StringBuffer();
        String resultTableMacro = "$$" + RESULT_TABLE + "$$";

        // Never used?
        // Column col = question.getQuery().getColumn(pkColNames[0]); // pk
        //
        // if (pkColNames[1] != null) { // project id
        // col = question.getQuery().getColumn(pkColNames[1]);
        // }
        //

        Collection<Column> columns = columnMap.values();
        for (Column column : columns) {
            column.setQuery(getAttributesQuery());
            addColumn(column, sqlSelectBuf, RESULT_TABLE, attributesQuery);
        }

        String[] pkColNames = Answer.findPrimaryKeyColumnNames(question.getQuery());

        // last comma
        String sqlSelect = sqlSelectBuf.substring(0, sqlSelectBuf.length() - 2);

        String sqlWhere = " WHERE " + resultTableMacro + "." + pkColNames[0]
                + " = " + "$$" + RecordClass.PRIMARY_KEY_NAME + "$$";
        if (pkColNames[1] != null)
            sqlWhere += " AND " + resultTableMacro + "." + pkColNames[1]
                    + " = " + "$$" + RecordClass.PROJECT_ID_NAME + "$$";

        String sql = "SELECT " + sqlSelect + " FROM dual" + sqlWhere;

        ((SqlQuery) attributesQuery).setSql(sql);
    }

    private void addColumn(Column column, StringBuffer sqlSelectBuf,
            String resultTable, Query attributesQuery) {
        attributesQuery.addColumn(column);
        sqlSelectBuf.append(column.getName() + ", ");
        ColumnAttributeField field = new ColumnAttributeField();
        field.setColumn(column);
        attributesFieldMap.put(field.getName(), field);
    }
}
