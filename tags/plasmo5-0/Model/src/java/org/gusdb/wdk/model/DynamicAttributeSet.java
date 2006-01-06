package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.gusdb.wdk.model.implementation.SqlQuery;

public class DynamicAttributeSet {

    HashMap <String, FieldI> attributesFieldMap;
    RecordClass recordClass;
    List <Column> columns;
    Query attributesQuery;
    Vector <FieldI> tempFieldsList = new Vector();
    public final static String RESULT_TABLE = "RESULTTABLE";
    //    public final static String RESULT_TABLE = "_RESULT_TABLE_";

    public DynamicAttributeSet() {
	attributesFieldMap = new HashMap();
	columns = new Vector();
    }

    public void addAttribute(Column column) throws WdkModelException {
	columns.add(column);
    }

    public void addTextAttribute(TextAttributeField textAttributeField) throws WdkModelException {
	tempFieldsList.add(textAttributeField);
    }
    
    public void addLinkAttribute(LinkAttributeField linkAttributeField) throws WdkModelException {
	tempFieldsList.add(linkAttributeField);
    }
    
    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer();
	buf.append("  dynamicAttributes:" + newline);

        for (String attrName : attributesFieldMap.keySet()) {
	    buf.append("    " + attrName + newline);
	}
	return buf.toString();
    }

    ///////////////////////////////////////////////////////////////////
    //             package methods                                   //
    ///////////////////////////////////////////////////////////////////

    void setResources(WdkModel model) throws WdkModelException {
	attributesQuery.setResources(model);
    }

    void setQuestion(Question question) throws WdkModelException {
	this.recordClass = question.getRecordClass();
	initAttributesQuery(question);
	for (FieldI field : tempFieldsList) {
	    checkAttributeName(field.getName(), question, false);	    
            attributesFieldMap.put(field.getName(), field);	    
	}
    }

    Map <String, FieldI> getAttributeFields() {
	return new HashMap(attributesFieldMap);
    }

    Query getAttributesQuery() {
	return attributesQuery;
    }

    ///////////////////////////////////////////////////////////////////
    //             private methods                                   //
    ///////////////////////////////////////////////////////////////////

    private void initAttributesQuery(Question question) throws WdkModelException {
	attributesQuery = new SqlQuery();
	attributesQuery.setName(question.getFullName() + ".DynAttrs");
	StringParam param = new StringParam();
	param.setName(RecordClass.PRIMARY_KEY_NAME);
	attributesQuery.addParam(param);
	param = new StringParam();
	param.setName(RESULT_TABLE);
	attributesQuery.addParam(param);

	String[] pkColNames =
	    Answer.findPrimaryKeyColumnNames(question.getQuery());

	StringBuffer sqlSelectBuf = new StringBuffer();
	String resultTableMacro = "$$" + RESULT_TABLE + "$$";

	Column col = question.getQuery().getColumn(pkColNames[0]);  // pk
	//	addColumn(col, sqlSelectBuf, RESULT_TABLE, attributesQuery);

	if (pkColNames[1] != null) {  // project id
	    col = question.getQuery().getColumn(pkColNames[1]);
	    //	    addColumn(col, sqlSelectBuf, RESULT_TABLE, attributesQuery);
	}

        for (Column column : columns) {
	    checkAttributeName(column.getName(), question, true);
	    column.setQuery(getAttributesQuery());
	    addColumn(column, sqlSelectBuf, RESULT_TABLE, attributesQuery);
        }

	String sqlSelect = 
	    sqlSelectBuf.substring(0, sqlSelectBuf.length()-2); // last comma
	
	String sqlWhere = 
	    " WHERE " + resultTableMacro + "." + pkColNames[0] + " = " + 
	    "$$" + RecordClass.PRIMARY_KEY_NAME + "$$";
	if (pkColNames[1] != null) 
	    sqlWhere += 
		" AND " + resultTableMacro + "." +  pkColNames[1] + " = " + 
		"$$" + RecordClass.PROJECT_ID_NAME + "$$";
				    
	String sql = 
	    "SELECT " + sqlSelect + 
	    " FROM dual" + 
	    sqlWhere;
	    
	((SqlQuery)attributesQuery).setSql(sql);
    }

    private void addColumn(Column column, StringBuffer sqlSelectBuf,
			   String resultTable, Query attributesQuery) {
	attributesQuery.addColumn(column);
	sqlSelectBuf.append(column.getName() + ", ");
	AttributeField field = new AttributeField(column);
	attributesFieldMap.put(field.getName(), field);	    
    }

    private void checkAttributeName(String name, Question question, boolean checkAgainstQuery) throws WdkModelException {
	String da = "dynamicAttributes of question " + question.getFullName();
	
        if (recordClass.getAttributeFieldsMap().containsKey(name)) 
            throw new WdkModelException(da + " introduces an attribute '" +
					name + "' that recordClass " + 
					recordClass.getName() +" already has");


        if (attributesFieldMap.containsKey(name)) 
            throw new WdkModelException(da + " contains a duplicate attribute '" + name + "'");

	if (checkAgainstQuery
	    && !question.getQuery().getColumnMap().containsKey(name)) 
            throw new WdkModelException(da + " contains an attribute '" + name + "' that does not appear in query " + question.getQuery().getFullName());

    }
}
