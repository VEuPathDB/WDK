package org.gusdb.gus.wdk.model;

import java.util.HashMap;

import java.sql.ResultSet;

public class RecordInstance {
    
    String primaryKey;
    Record record;
    HashMap fieldsResultSetsMap;

    public RecordInstance(Record record) {
	this.record = record;
	fieldsResultSetsMap = new HashMap();
    }

    public void setPrimaryKey(String primaryKey) {
	this.primaryKey = primaryKey;
    }

    public Object getFieldValue(String fieldName) throws Exception {
	SimpleQueryI query = record.getFieldsQuery(fieldName);
	String queryName = query.getName();
	if (!fieldsResultSetsMap.containsKey(queryName)) {
	    runFieldsQuery(query);
	}
	HashMap resultMap = (HashMap)fieldsResultSetsMap.get(queryName);
	return resultMap.get(fieldName);
    }

    public String getFieldSpecialType(String fieldName) {
	return null;
    }

    public ResultSet getTableValue(String tableName) throws Exception {
	SimpleQueryI query = record.getTableQuery(tableName);
	SimpleQueryInstanceI instance = query.makeInstance();
	instance.setIsCacheable(false);
	HashMap paramHash = new HashMap();
	if (primaryKey == null) 
	    throw new NullPointerException("primaryKey is null");
	paramHash.put("primaryKey", primaryKey);
	instance.setValues(paramHash);
	return instance.getResult();
    }

    public String getTextFieldValue(String textFieldName) throws Exception {
	String rawText = record.getTextField(textFieldName);
	return instantiateTextField(textFieldName, rawText, new HashMap());
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // protected
    ///////////////////////////////////////////////////////////////////////////////////////


    /**
     * Place hash of single row result into hash keyed on query name
     */
    protected void runFieldsQuery(SimpleQueryI query) throws Exception {
	SimpleQueryInstanceI instance = query.makeInstance();
	instance.setIsCacheable(false);
	HashMap paramHash = new HashMap();
	if (primaryKey == null) 
	    throw new NullPointerException("primaryKey is null");
	paramHash.put("primaryKey", primaryKey);
	instance.setValues(paramHash);
	ResultSet rs = instance.getResult();
	instance.checkColumns(rs, true);
	HashMap queryResult = new HashMap();
	Column[] columns = query.getColumns();
	for (int i=0; i<columns.length; i++) {
	    String columnName = columns[i].getName();
	    queryResult.put(columnName, rs.getObject(columnName));
	}
	fieldsResultSetsMap.put(query.getName(), queryResult);
    }

    protected String instantiateTextField(String textFieldName, String rawText, 
					  HashMap alreadyVisited) throws Exception {

	if (alreadyVisited.containsKey(textFieldName)) {
	    throw new Exception("Circular text field subsitution involving text field '" 
				+ textFieldName + "'");
	}

	alreadyVisited.put(textFieldName, textFieldName);

	// get all non-text field names, and see if they appear as macro
	String[] allNonTextFieldNames = record.getAllNonTextFieldNames();
	String instantiatedText = rawText;
	for (int i=0; i<allNonTextFieldNames.length; i++) {
	    String fieldName = allNonTextFieldNames[i];
	    String macro = "$$" + fieldName + "$$";
	    if (instantiatedText.indexOf(macro) != -1) {
		instantiatedText = 
		    instantiatedText.replaceAll(macro, 
						getFieldValue(fieldName).toString());
	    }
	}

	// get all text field names, and see if they appear as macro
	String[] allTextFieldNames = record.getAllTextFieldNames();
	for (int i=0; i<allTextFieldNames.length; i++) {
	    String fieldName = allTextFieldNames[i];
	    String macro = "$$" + fieldName + "$$";
	    if (instantiatedText.indexOf(macro) != -1) {
		instantiatedText = 
		    instantiatedText.replaceAll(macro, 
						getTextFieldValue(fieldName));
	    }
	}

	if (instantiatedText.matches("$$\\w+$$")) {
	    throw new Exception ("textField '" + textFieldName + "' contains unrecognized field macro: " + instantiatedText);
	}
	return instantiatedText;
    }

}
