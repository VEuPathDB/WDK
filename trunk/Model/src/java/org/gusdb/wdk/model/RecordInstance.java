package org.gusdb.gus.wdk.model;

import java.util.HashMap;

public class RecordInstance {
    
    String primaryKey;
    Record record;
    HashMap fieldsResultSetsMap;
    SummaryInstance summaryInstance;

    public RecordInstance(Record record) {
	this.record = record;
	fieldsResultSetsMap = new HashMap();
    }

    public Record getRecord() { return record; }

    public void setPrimaryKey(String primaryKey) {
	this.primaryKey = primaryKey;
    }

    public String getPrimaryKey() {
	return primaryKey;
    }

    public void setSummaryInstance(SummaryInstance rli){
	this.summaryInstance = rli;
    }

    /**
     * Get the value for a field or a text field
     */
    public Object getFieldValue(String fieldName) throws WdkModelException {
	Object value;
	if (record.isTextField(fieldName)) {
	    String rawText = record.getTextField(fieldName);
	    value = instantiateTextField(fieldName, rawText, new HashMap());
	} else {
	    Query query = record.getFieldsQuery(fieldName);
	    String queryName = query.getName();
	    if (!fieldsResultSetsMap.containsKey(queryName)) {
		runFieldsQuery(query);
	    }
	    HashMap resultMap = (HashMap)fieldsResultSetsMap.get(queryName);

	    value = resultMap.get(fieldName);
	}
	return value;
    }

    public String getFieldSpecialType(String fieldName) {
	return null;
    }

    public ResultList getTableValue(String tableName) throws WdkModelException {
	Query query = record.getTableQuery(tableName);
	QueryInstance instance = query.makeInstance();
	instance.setIsCacheable(false);
	HashMap paramHash = new HashMap();
	if (primaryKey == null) 
	    throw new WdkModelException("primaryKey is null");
	paramHash.put("primaryKey", primaryKey);
	try {
	    instance.setValues(paramHash);
	} catch (WdkUserException e) {
	    throw new WdkModelException(e);
	}
	return instance.getResult();
    }

    public String print() throws WdkModelException {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf =
	    new StringBuffer(record.getType() + " " + record.getIdPrefix() + primaryKey).append( newline );

	String[] fieldNames = record.getNonTextFieldNames();
	for (int i=0; i<fieldNames.length; i++) {
	    String fieldName = fieldNames[i];
	    buf.append(fieldName + ":   " + getFieldValue(fieldName)).append( newline );
	}
	
	String[] textFieldNames = record.getTextFieldNames();
	for (int i=0; i<textFieldNames.length; i++){
	    String fieldName = textFieldNames[i];
	    buf.append(fieldName + ":   " + getFieldValue(fieldName)).append( newline );
	}
	
	String[] tableNames = record.getTableNames();
	for (int i=0; i<tableNames.length; i++){
	    String tableName = tableNames[i];
	    buf.append("Table " + tableName).append( newline );
	    ResultList resultList = getTableValue(tableName);
	    resultList.write(buf);
	    buf.append(newline);
	}

	return buf.toString();
	
    }

    


    ///////////////////////////////////////////////////////////////////////////////////////
    // protected
    ///////////////////////////////////////////////////////////////////////////////////////

    protected void setFieldValue(String fieldName, Object fieldValue) throws WdkModelException{
	
	Query query = record.getFieldsQuery(fieldName);
	String queryName = query.getName();
	HashMap resultMap = (HashMap)fieldsResultSetsMap.get(queryName);
	if (resultMap == null){
	    resultMap = new HashMap();
	    fieldsResultSetsMap.put(queryName, resultMap);
	}
	resultMap.put(fieldName, fieldValue);
    }

    /**
     * Place hash of single row result into hash keyed on query name
     */
    protected void runFieldsQuery(Query query) throws WdkModelException {
	QueryInstance instance = query.makeInstance();
	instance.setIsCacheable(false);
	if (summaryInstance != null){
	    summaryInstance.setMultiMode(instance);
	    ResultList rl = instance.getResult();
	    summaryInstance.setQueryResult(rl);
	    rl.close();
	}
	else{ //do it all myself
	    HashMap paramHash = new HashMap();
	    if (primaryKey == null) 
		throw new WdkModelException("primaryKey is null");
	    paramHash.put("primaryKey", primaryKey);
	    try {
		instance.setValues(paramHash);
	    } catch (WdkUserException e) {
		throw new WdkModelException(e);
	    }
	    ResultList rl = instance.getResult();
	    //	rl.checkQueryColumns(query, true);
	    
	    Column[] columns = query.getColumns();
	    rl.next();
	    for (int i=0; i<columns.length; i++) {
		String columnName = columns[i].getName();
		setFieldValue(columnName, rl.getValue(columnName));
	    }
	}
    }

    protected String instantiateTextField(String textFieldName, String rawText, 
					  HashMap alreadyVisited) throws WdkModelException {

	if (alreadyVisited.containsKey(textFieldName)) {
	    throw new WdkModelException("Circular text field subsitution involving text field '" 
				+ textFieldName + "'");
	}

	alreadyVisited.put(textFieldName, textFieldName);

	String instantiatedText = rawText;

	// primary key
	instantiatedText = instantiateText(instantiatedText, "primaryKey", 
					   getPrimaryKey());
	
	// get all non-text field names, and see if they appear as a macro
	String[] allNonTextFieldNames = record.getNonTextFieldNames();
	for (int i=0; i<allNonTextFieldNames.length; i++) {
	    String fieldName = allNonTextFieldNames[i];
	    if (getFieldValue(fieldName) != null){
		instantiatedText = instantiateText(instantiatedText, fieldName, 
				getFieldValue(fieldName).toString());
	    }
	    //returns as 'null' otherwise

	}

	// get all text field names, and see if they appear as macro
	String[] allTextFieldNames = record.getTextFieldNames();
	for (int i=0; i<allTextFieldNames.length; i++) {
	    String fieldName = allTextFieldNames[i];
	    if (fieldName.equals(textFieldName)) continue;

	    instantiatedText = instantiateText(instantiatedText, fieldName, 
					       getFieldValue(fieldName).toString());
	}

	checkInstantiatedText(instantiatedText);

	return instantiatedText;
    }

    ////////////////////////////////////////////////////////////////////
    //   static
    ////////////////////////////////////////////////////////////////////

    /**
     * substitute a value for a macro in a text string.  The macro is delimited by $$
     @param text the text which contains the macro
     @param macroName the name of the macro, without the delimiter
     @param value the value to substitute in
     */
    public static String instantiateText(String text, String macroName, String value) {
	String macro = "$$" + macroName + "$$";
	String macroRegex = "\\$\\$" + macroName + "\\$\\$";
	if (text.indexOf(macro) != -1) {
	    text = text.replaceAll(macroRegex, value);
	}
	return text;
    }

    public static void checkInstantiatedText(String instantiatedText) throws WdkModelException {
	if (instantiatedText.matches("\\$\\$\\w+\\$\\$")) 
	    throw new WdkModelException ("'" + instantiatedText + 
				 "' contains unrecognized macro");
    }
	
}
