package org.gusdb.wdk.model;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

public class RecordInstance {
    
    String primaryKey;
    RecordClass recordClass;
    HashMap attributesResultSetsMap;
    Answer answer;

    public RecordInstance(RecordClass recordClass) {
	this.recordClass = recordClass;
	attributesResultSetsMap = new HashMap();
    }

    public RecordClass getRecordClass() { return recordClass; }

    public void setPrimaryKey(String primaryKey) {
	this.primaryKey = primaryKey;
    }

    public String getPrimaryKey() {
	return primaryKey;
    }

    /**
     * Get the value for a attribute or a text attribute
     */
    public Object getAttributeValue(String attributeName) throws WdkModelException {
	Object value;
	FieldI field = (FieldI)recordClass.getField(attributeName); 
	if (field instanceof PrimaryKeyField) {
	    value = recordClass.getIdPrefix() + getPrimaryKey();

	} else if (field instanceof TextAttributeField) {
	    TextAttributeField taField = (TextAttributeField)field;
	    value = instantiateTextAttribute(attributeName, 
					     taField, 
					     new HashMap());

	} else if (field instanceof LinkAttributeField) {
	    LinkAttributeField laField = (LinkAttributeField)field;
	    value = instantiateLinkAttribute(attributeName, 
					     laField, 
					     new HashMap());

	} else if (field instanceof AttributeField){
	    AttributeField aField = (AttributeField)field;
	    Query query = aField.getQuery();
	    String queryName = query.getName();

	    if (!attributesResultSetsMap.containsKey(queryName)) {
		runAttributesQuery(query);
	    }
	    HashMap resultMap = (HashMap)attributesResultSetsMap.get(queryName);
	    if (resultMap == null) {
	        throw new WdkModelException("Unable to get resultMap for queryName of '"+queryName+"'");
	    }
	    value = resultMap.get(attributeName);
	} else {
	    throw new WdkModelException("Unsupported field type: " + field.getClass());
	}
	return value;
    }

    public ResultList getTableValue(String tableName) throws WdkModelException {
	Query query = recordClass.getTableField(tableName).getQuery();
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

    /**
     * @return Map of tableName -> TableFieldValue
     */
    public Map getTables() {
	return new FieldValueMap(recordClass, this, true);
    }

    /**
     * @return Map of tableName -> AttributeFieldValue
     */
    public Map getAttributes() {
	return new FieldValueMap(recordClass, this, false);
    }

    public String print() throws WdkModelException {

	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer();
	
	Map attributeFields = getAttributes();
	Iterator fieldNames = attributeFields.keySet().iterator();
	while (fieldNames.hasNext()) {
	    String fieldName = (String)fieldNames.next();
	    AttributeFieldValue field = 
		(AttributeFieldValue)attributeFields.get(fieldName);
	    buf.append(field.getDisplayName() + ":   " + 
		       field.getValue()).append( newline );
	}


	Map tableFields = getTables();
	fieldNames = tableFields.keySet().iterator();
	
	while (fieldNames.hasNext()) {
	    
	    String fieldName = (String)fieldNames.next();
	    TableFieldValue field = 
		(TableFieldValue)tableFields.get(fieldName);
	    buf.append("Table " + field.getDisplayName()).append( newline );
	    ResultList resultList = getTableValue(fieldName);
	    resultList.write(buf);
	    
	    buf.append(newline);
	}

	return buf.toString();
	
    }
    

    ///////////////////////////////////////////////////////////////////////////
    // package methods
    ///////////////////////////////////////////////////////////////////////////

    void setAnswer(Answer answer){
	this.answer = answer;
    }

    ///////////////////////////////////////////////////////////////////////////
    // protected methods
    ///////////////////////////////////////////////////////////////////////////

    protected void setAttributeValue(String attributeName, Object attributeValue) throws WdkModelException{
	
	AttributeField field 
	    = (AttributeField)recordClass.getField(attributeName);
	String queryName = field.getQuery().getName();
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
	if (answer != null){
	    answer.setMultiMode(instance);
	    ResultList rl = instance.getResult();
	    answer.setQueryResult(rl);
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
		String msg = "Attributes query '" + query.getFullName() + "' in Record '" + recordClass.getFullName() + "' does not return any rows";
		throw new WdkModelException(msg);
	    }
	    for (int i=0; i<columns.length; i++) {
		String columnName = columns[i].getName();
		setAttributeValue(columnName, 
				  rl.getAttributeFieldValue(columnName).getValue());
	    }
	    if (rl.next()) {
		String msg = "Attributes query '" + query.getFullName() + "' in Record '" + recordClass.getFullName() + "' returns more than one row";
		throw new WdkModelException(msg);
	    }
	    rl.close();
	}
    }

    protected String instantiateTextAttribute(String textAttributeName, 
					      TextAttributeField field, 
					      HashMap alreadyVisited) throws WdkModelException {

	if (alreadyVisited.containsKey(textAttributeName)) {
	    throw new WdkModelException("Circular text attribute subsitution involving text attribute '" 
					+ textAttributeName + "'");
	}

	alreadyVisited.put(textAttributeName, textAttributeName);
	return instantiateAttr(field.getText(), textAttributeName);
    }

    protected LinkValue instantiateLinkAttribute(String linkAttributeName, 
						 LinkAttributeField field, 
						 HashMap alreadyVisited) throws WdkModelException {

	if (alreadyVisited.containsKey(linkAttributeName)) {
	    throw new WdkModelException("Circular link attribute subsitution involving text attribute '" 
					+ linkAttributeName + "'");
	}

	alreadyVisited.put(linkAttributeName, linkAttributeName);

	return new LinkValue(instantiateAttr(field.getVisible(), 
					     linkAttributeName),
			     instantiateAttr(field.getUrl(), 
					     linkAttributeName));
    }

    private String instantiateAttr(String rawText, String targetAttrName) throws WdkModelException { 
	String instantiatedText = rawText;

	Iterator attributeNames = getAttributes().keySet().iterator();
	while (attributeNames.hasNext()) {
	    String attrName = (String)attributeNames.next();
	    if (attrName.equals(targetAttrName)) continue;
	    if (containsMacro(instantiatedText, attrName)) {
		String valString =  
		    getAttributeValue(attrName.toString()).toString();
		instantiatedText = instantiateText(instantiatedText, 
						   attrName, 
						   valString);
	    }
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

    public static boolean containsMacro(String text, String macroName) {
	String macro = "$$" + macroName + "$$";
	return text.indexOf(macro) != -1;
    }

    public static void checkInstantiatedText(String instantiatedText) throws WdkModelException {
	if (instantiatedText.matches("\\$\\$\\w+\\$\\$")) 
	    throw new WdkModelException ("'" + instantiatedText + 
				 "' contains unrecognized macro");
    }
	
}
