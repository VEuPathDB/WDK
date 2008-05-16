package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.implementation.SqlQuery;

public class DynamicAttributeSet extends WdkModelBase {

    /**
     * 
     */
    private static final long serialVersionUID = -1373806354317917813L;
    // private static Logger logger =
    // Logger.getLogger(DynamicAttributeSet.class);

    public final static String RESULT_TABLE = "RESULTTABLE";

    private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
    private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<String, AttributeField>();
    private RecordClass recordClass;
    private Query attributesQuery;
    private Set<String> columnAttributeFieldNames = new LinkedHashSet<String>();
    private Set<String> noncolumnAttributeFieldNames = new LinkedHashSet<String>();

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

    void setResources(WdkModel model) throws WdkModelException {
        attributesQuery.setResources(model);
    }

    void setQuestion(Question question) throws WdkModelException {
        this.recordClass = question.getRecordClass();

        // validate attributeFields
        String message = "dynamicAttributes of question "
                + question.getFullName();
        Map<String, AttributeField> rsAttributeFields = recordClass.getAttributeFieldMap();
        for (String attributeName : attributeFieldMap.keySet()) {
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
        return new LinkedHashMap<String, AttributeField>(attributeFieldMap);
    }

    Map<String, AttributeField> getReportMakerAttributeFieldMap() {
        Map<String, AttributeField> rmfields = new LinkedHashMap<String, AttributeField>();
        Set<String> names = attributeFieldMap.keySet();
        for (String name : names) {
            AttributeField field = attributeFieldMap.get(name);
            if (field.getInReportMaker())
                rmfields.put(name, field);
        }
        return rmfields;
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
        
        // a param for result table name. the "quote" is forced to be false, since the param value is used as a table name
        param = new StringParam();
        param.setName(RESULT_TABLE);
        param.setQuote(false);
        attributesQuery.addParam(param);

        // also add project_id into the attribute query
        if (recordClass.getPrimaryKeyField().getProjectParam() != null) {
            param = new StringParam();
            param.setName(RecordClass.PROJECT_ID_NAME);
            attributesQuery.addParam(param);
        }

        // get columns from Question.Query, and check the paired Column
        String message = "dynamicAttributes of question "
                + question.getFullName();
        Map<String, Column> columnMap = question.getQuery().getColumnMap();

        // // check if all column names appear in column attributes
        // The column may contain pk_column, which may not appear in attributes
        // Set<String> names = columnMap.keySet();
        // for (String name : names) {
        // if (!columnAttributeFieldNames.contains(name))
        // throw new WdkModelException(message
        // + " doesn't contain an attribute '" + name
        // + "' that appears in query "
        // + question.getQuery().getFullName());
        // }

        // check if all column attribute names appear in columns
        for (String name : columnAttributeFieldNames) {
            if (!columnMap.containsKey(name))
                throw new WdkModelException(message
                        + " contains an attribute '" + name
                        + "' that does not appear in query "
                        + question.getQuery().getFullName());

            // associate columns with column attribute fields
            Column column = columnMap.get(name);
            column.setDynamicColumn(true);
            ColumnAttributeField field = (ColumnAttributeField) attributeFieldMap.get(name);

            // TEST
            // logger.debug("Dyna Column '" + column.getName() + "' from Query:
            // "
            // + column.getQuery().getFullName());

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
            // TEST
            // if (column.getName().equalsIgnoreCase("score"))
            // logger.debug("DynamicAttributeSet '" + this.hashCode()
            // + "' is setting column.");

            column.setQuery(getAttributesQuery());
            addColumn(column, sqlSelectBuf, attributesQuery, resultTableMacro);
        }

        String[] pkColNames = Answer.findPrimaryKeyColumnNames(question.getQuery());

        // add other attribute fields into the query, such as text and link
        // attributes

        // last comma
        String sqlSelect = sqlSelectBuf.substring(0, sqlSelectBuf.length() - 2);

        String sqlWhere = " WHERE " + resultTableMacro + "." + pkColNames[0]
                + " = " + "$$" + RecordClass.PRIMARY_KEY_NAME + "$$";
        if (pkColNames[1] != null)
            sqlWhere += " AND " + resultTableMacro + "." + pkColNames[1]
                    + " = " + "$$" + RecordClass.PROJECT_ID_NAME + "$$";

        // the use of "dual" causes trouble on PostgreSQL.
        // Solution: create a "dual" table in PostgreSQL - consider putting the
        // table creation code into wdkCache -new

        // String sql = "SELECT " + sqlSelect + " FROM dual " + sqlWhere;

        // the dual cause another problem in PostgreSQL, if there's no row in
        // it, the query will return nothing; if has value, then the result will
        // be a cross product, which may not be expected.
        // The solution is, without using dual, just remove the last comma in
        // FROM clause in SqlMunger.
        String sql = "SELECT " + sqlSelect + " FROM " + sqlWhere;

        ((SqlQuery) attributesQuery).setSql(sql);
    }

    private void addColumn(Column column, StringBuffer sqlSelectBuf,
            Query attributesQuery, String resultTableMacro)
            throws WdkModelException {
        attributesQuery.addColumnToMap(column);
        sqlSelectBuf.append(resultTableMacro + "." + column.getName() + ", ");

        // commented by Jerric
        // The fields has already been in the field map, no need to add it
        // again?
        // ColumnAttributeField field = new ColumnAttributeField();
        // field.setColumn(column);
        // attributesFieldMap.put(field.getName(), field);
    }

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

                // check if it's a column attribute.
                if (field instanceof ColumnAttributeField) {
                    columnAttributeFieldNames.add(fieldName);
                } else {
                    noncolumnAttributeFieldNames.add(fieldName);
                }
            }
        }
        attributeFieldList = null;
    }
}
