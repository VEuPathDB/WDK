package org.gusdb.gus.wdk.model;

import java.util.HashMap;

public class RecordInstance {
    
    String primaryKey;
    Record record;
    HashMap attributesResultSetsMap;
    SummaryInstance summaryInstance;

    public RecordInstance(Record record) {
	this.record = record;
	attributesResultSetsMap = new HashMap();
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
     * Get the value for a attribute or a text attribute
     */
    public Object getAttributeValue(String attributeName) throws WdkModelException {
	Object value;
	if (record.isTextAttribute(attributeName)) {
	    String rawText = record.getTextAttribute(attributeName);
	    value = instantiateTextAttribute(attributeName, rawText, new HashMap());
	} else {
	    Query query = record.getAttributesQuery(attributeName);
	    String queryName = query.getName();
	    if (!attributesResultSetsMap.containsKey(queryName)) {
		runAttributesQuery(query);
	    }
	    HashMap resultMap = (HashMap)attributesResultSetsMap.get(queryName);

	    value = resultMap.get(attributeName);
	}
	return value;
    }

    public String getAttributeSpecialType(String attributeName) {
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

	String[] attributeNames = record.getNonTextAttributeNames();
	for (int i=0; i<attributeNames.length; i++) {
	    String attributeName = attributeNames[i];
	    buf.append(attributeName + ":   " + getAttributeValue(attributeName)).append( newline );
	}
	
	String[] textAttributeNames = record.getTextAttributeNames();
	for (int i=0; i<textAttributeNames.length; i++){
	    String attributeName = textAttributeNames[i];
	    buf.append(attributeName + ":   " + getAttributeValue(attributeName)).append( newline );
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

    protected void setAttributeValue(String attributeName, Object attributeValue) throws WdkModelException{
	
	Query query = record.getAttributesQuery(attributeName);
	String queryName = query.getName();
	HashMap resultMap = (HashMap)attributesResultSetsMap.get(queryName);
	if (resultMap == null){
	    resultMap = new HashMap();
	    attributesResultSetsMap.put(queryName, resultMap);
	}
	resultMap.put(attributeName, attributeValue);
    }

    /**
     * Place hash of single row result into hash keyed on query name
     */
    protected void runAttributesQuery(Query query) throws WdkModelException {
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
		setAttributeValue(columnName, rl.getValue(columnName));
	    }
	}
    }

    protected String instantiateTextAttribute(String textAttributeName, String rawText, 
					  HashMap alreadyVisited) throws WdkModelException {

	if (alreadyVisited.containsKey(textAttributeName)) {
	    throw new WdkModelException("Circular text attribute subsitution involving text attribute '" 
				+ textAttributeName + "'");
	}

	alreadyVisited.put(textAttributeName, textAttributeName);

	String instantiatedText = rawText;

	// primary key
	instantiatedText = instantiateText(instantiatedText, "primaryKey", 
					   getPrimaryKey());
	
	// get all non-text attribute names, and see if they appear as a macro
	String[] allNonTextAttributeNames = record.getNonTextAttributeNames();
	for (int i=0; i<allNonTextAttributeNames.length; i++) {
	    String attributeName = allNonTextAttributeNames[i];
	    if (getAttributeValue(attributeName) != null){
		instantiatedText = instantiateText(instantiatedText, attributeName, 
				getAttributeValue(attributeName).toString());
	    }
	}

	// get all text attribute names, and see if they appear as macro
	String[] allTextAttributeNames = record.getTextAttributeNames();
	for (int i=0; i<allTextAttributeNames.length; i++) {
	    String attributeName = allTextAttributeNames[i];
	    if (attributeName.equals(textAttributeName)) continue;

	    instantiatedText = instantiateText(instantiatedText, attributeName, 
					       getAttributeValue(attributeName).toString());
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
