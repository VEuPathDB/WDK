package org.gusdb.wdk.model;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

public class RecordInstance implements Map {
    
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
	if (attributeName.equals(record.getType())) {
	    value = record.getIdPrefix() + getPrimaryKey();
	} else if (record.isTextAttribute(attributeName)) {
	    String rawText = record.getTextAttribute(attributeName);
	    value = instantiateTextAttribute(attributeName, rawText, new HashMap());
	} else {
	    Query query = record.getAttributesQuery(attributeName);
	    String queryName = query.getName();

	    if (!attributesResultSetsMap.containsKey(queryName)) {
		runAttributesQuery(query);
	    }
	    HashMap resultMap = (HashMap)attributesResultSetsMap.get(queryName);
	    if (resultMap == null) {
	        throw new WdkModelException("Unable to get resultMap for queryName of '"+queryName+"'");
	    }
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
	StringBuffer buf = new StringBuffer();
	
	Iterator attributeNamesIt = record.getNonTextAttributeNames().iterator();
	while (attributeNamesIt.hasNext()) {
	    String attributeName = (String) attributeNamesIt.next();
	    buf.append(record.getDisplayName(attributeName) + ":   " + getAttributeValue(attributeName)).append( newline );
	}
	Iterator textAttributeNamesIt = record.getTextAttributeNames().iterator();
	while (textAttributeNamesIt.hasNext()) {
	    String attributeName = (String) textAttributeNamesIt.next();
	    buf.append(attributeName + ":   " + getAttributeValue(attributeName)).append( newline );
	}
	Iterator tableNamesIt = record.getTableNames().iterator();
	while (tableNamesIt.hasNext()){
	    String tableName = (String) tableNamesIt.next();
	    buf.append("Table " + tableName).append( newline );
	    ResultList resultList = getTableValue(tableName);
	    resultList.write(buf);
	    
	    buf.append(newline);
	}

	return buf.toString();
	
    }

    
    ////////////////////////////////////////////////////////////////////
    //  implementation of Map
    ////////////////////////////////////////////////////////////////////

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return record.getAllNames().size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
	return record.containsName((String)key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
	throw new UnsupportedOperationException("Illegal operation 'containsValue' on RecordINstance");
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
	Object value = null;
	try {
	    if (record.getNonTextAttributeNames().contains(key)
		|| record.getNonTextAttributeNames().contains(key)) 
		value = getAttributeValue((String)key);
	    
	    else if (record.getTableNames().contains(key))
		value = getTableValue((String)key);

	    else throw new IllegalArgumentException("Record " + record.getFullName() + " does not have any value with key " + key);

	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
	return value;
    }
    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
	throw new UnsupportedOperationException("Illegal operation 'put' on RecordINstance");
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
	throw new UnsupportedOperationException("Illegal operation 'remove' on RecordINstance");
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
	throw new UnsupportedOperationException("Illegal operation 'putAll' on RecordINstance");
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
	return record.getAllNames();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
	throw new UnsupportedOperationException("Illegal operation 'values' on RecordINstance");
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
	throw new UnsupportedOperationException("Illegal operation 'entrySet' on RecordINstance");
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
	    if (!rl.next()) {
		String msg = "Attributes query '" + query.getFullName() + "' in Record '" + record.getFullName() + "' does not return any rows";
		throw new WdkModelException(msg);
	    }
	    for (int i=0; i<columns.length; i++) {
		String columnName = columns[i].getName();
		setAttributeValue(columnName, rl.getValue(columnName));
	    }
	    if (rl.next()) {
		String msg = "Attributes query '" + query.getFullName() + "' in Record '" + record.getFullName() + "' returns more than one row";
		throw new WdkModelException(msg);
	    }
	    rl.close();
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
	Iterator allNonTextAttributeNamesIt = record.getNonTextAttributeNames().iterator();
	while (allNonTextAttributeNamesIt.hasNext()) {
	    String attributeName = (String) allNonTextAttributeNamesIt.next();
	    if (getAttributeValue(attributeName) != null){
	        instantiatedText = instantiateText(instantiatedText, attributeName, 
				getAttributeValue(attributeName).toString());
	    }
	}

	// get all text attribute names, and see if they appear as macro
	Iterator allTextAttributeNamesIt = record.getTextAttributeNames().iterator();
    while (allTextAttributeNamesIt.hasNext()) {
	    String attributeName = (String) allTextAttributeNamesIt.next();
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
