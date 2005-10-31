package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class RecordInstance {

    public static final int MAXIMUM_NESTED_RECORD_INSTANCES = 100;
    
    PrimaryKeyValue primaryKey;
    RecordClass recordClass;
    HashMap attributesResultSetsMap;
    HashMap summaryAttributeMap;
    Answer answer;


    public RecordInstance(RecordClass recordClass) {
	this.recordClass = recordClass;
	attributesResultSetsMap = new HashMap();
	summaryAttributeMap = new HashMap();
    }

    public RecordClass getRecordClass() { return recordClass; }

    /**
     * Modified by Jerric - Use two parts as primarykeyValue, projectId and the
     * original recordId String
     * @param projectId
     * @param recordId
     * @throws WdkModelException 
     */
    public void setPrimaryKey(String projectId, String recordId) 
    throws WdkModelException {
        PrimaryKeyField field = 
            (PrimaryKeyField) recordClass.getField(RecordClass.PRIMARY_KEY_NAME);
        // create primary key
        this.primaryKey = new PrimaryKeyValue(field, projectId, recordId);
    }

    /**
     * Modified by Jerric - use an object for primaryKeyValue
     * @return
     */
    public PrimaryKeyValue getPrimaryKey() {
	return primaryKey;
    }

    /**
     * Get the value for a attribute or a text attribute
     */
    public Object getAttributeValue(String attributeName) throws WdkModelException {
	Object value;
	FieldI field = (FieldI)recordClass.getField(attributeName); 
	if (field instanceof PrimaryKeyField) {
        // modified by Jerric
	    //value = recordClass.getIdPrefix() + getPrimaryKey();
        value = getPrimaryKey().getValue();

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

	String projectId = primaryKey.getProjectId();
	if (projectId != null) 
	    paramHash.put(RecordClass.PROJECT_ID_NAME, projectId);
	paramHash.put(RecordClass.PRIMARY_KEY_NAME, primaryKey.getRecordId());

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
    
    //change name of method?
    public Map getNestedRecordInstances() throws WdkModelException, WdkUserException {

	Map riMap = new LinkedHashMap();
	Question nq[] = this.recordClass.getNestedRecordQuestions();
	
	if (nq != null){
	    for (int i = 0; i < nq.length; i++){
		Question nextNq = nq[i];
		Answer a = getNestedRecordAnswer(nextNq);
		a.resetRecordInstanceCounter();
		RecordInstance nextRi = a.getNextRecordInstance();

		if (a.getNextRecordInstance() != null){
		    throw new WdkModelException("NestedQuestion " + nextNq.getName() +
						" returned more than one RecordInstance when called from " + this.recordClass.getName());
		}
		if (nextRi != null){
		    riMap.put(nextNq.getName(), nextRi);
		}
	    }
	}
	return riMap;
    }

    
    public Map getNestedRecordInstanceLists() throws WdkModelException, WdkUserException {

	Question nql[] = this.recordClass.getNestedRecordListQuestions();
	Map riListMap = new LinkedHashMap();
	
	if (nql != null){
	    for (int i = 0; i < nql.length; i++){
		Question nextNql = nql[i];
		Answer a = getNestedRecordAnswer(nextNql);
		Vector riVector = new Vector();
		while (a.hasMoreRecordInstances()){
		    RecordInstance nextRi = a.getNextRecordInstance();
		    riVector.add(nextRi);
		}
		RecordInstance[] riList = new RecordInstance[riVector.size()];
		riVector.copyInto(riList);
		if (riList != null){
		    riListMap.put(nextNql.getName(), riList);
		}
	    }
	}
	return riListMap;
    }

    private Answer getNestedRecordAnswer(Question q) throws WdkModelException, WdkUserException {
	
	Param nestedQueryParams[] = q.getQuery().getParams();
	HashMap queryValues = new HashMap();
	for (int j = 0; j < nestedQueryParams.length; j++){
	    Param nextParam = nestedQueryParams[j];
	    String paramName = nextParam.getName();

        // Modified by Jerric
        // The parameters of query don't map to attributes of the record
	    
//        FieldI field = (FieldI)this.getRecordClass().getField(paramName);
//	    String value;
//	    if (field instanceof PrimaryKeyField){
//		value = this.getPrimaryKey().toString();
//	    }
//	    else if (field instanceof AttributeField){
//		value = this.getAttributeValue(paramName).toString();
//	    }
//	    else {
//		throw new WdkModelException ("Illegal to link NestedRecordList " + q.getName() + " on attribute of type " + field.getClass().getName());
//	    }

        String value;
        if (paramName.equalsIgnoreCase("projectId")) {
            value = this.getPrimaryKey().getProjectId();
        } else {
            FieldI field = (FieldI)this.getRecordClass().getField(paramName);
            if (field instanceof PrimaryKeyField){
                value = this.getPrimaryKey().getRecordId();
            }
            else if (field instanceof AttributeField){
                value = this.getAttributeValue(paramName).toString();
            }
            else {
                throw new WdkModelException ("Illegal to link NestedRecordList " 
                        + q.getName() + " on attribute of type " 
                        + field.getClass().getName());
            }
        }

	    queryValues.put(paramName, value);
	}
	Answer a = q.makeAnswer(queryValues, 1, MAXIMUM_NESTED_RECORD_INSTANCES); 
	return a;
    }

	

    //maybe change this to RecordInstance[][] for jspwrap purposes?
    /*    public Vector getNestedRecordListInstances() throws WdkModelException, WdkUserException{
	NestedRecordList nrLists[] = this.recordClass.getNestedRecordLists();
	Vector nrVector = new Vector();
	if (nrLists != null){
	    for (int i = 0; i < nrLists.length; i++){
		NestedRecordList nextNrList = nrLists[i];
		RecordInstance riList[] = nextNrList.getRecordInstances(this);
		nrVector.add(riList);
	    }
	}
	return nrVector;

	}*/

    public String print() throws WdkModelException, WdkUserException {

	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer();
	
	Map attributeFields = getAttributes();
	
	HashMap summaryAttributes = new HashMap();
	HashMap nonSummaryAttributes = new HashMap();
	
	splitSummaryAttributes(attributeFields, summaryAttributes, nonSummaryAttributes);

	printAtts_Aux(buf, "Summary Attributes: " + newline, summaryAttributes);
	printAtts_Aux(buf, "Non-Summary Attributes: " + newline, nonSummaryAttributes);
	
	Map tableFields = getTables();
	Iterator fieldNames = tableFields.keySet().iterator();
	
	while (fieldNames.hasNext()) {
	    
	    String fieldName = (String)fieldNames.next();
	    TableFieldValue field = 
		(TableFieldValue)tableFields.get(fieldName);
	    buf.append("Table " + field.getDisplayName()).append( newline );
	    ResultList resultList = getTableValue(fieldName);
	    resultList.write(buf);
	    resultList.close();
	    field.closeResult();
	    buf.append(newline);
	}
	
	buf.append("Nested Records belonging to this RecordInstance:" + newline);
	Map nestedRecords = getNestedRecordInstances();
	Iterator recordNames = nestedRecords.keySet().iterator();
	while (recordNames.hasNext()){
	    String nextRecordName = (String)recordNames.next();
	    RecordInstance nextNr = (RecordInstance)nestedRecords.get(nextRecordName);
	    buf.append ("***" + nextRecordName + "***" + newline + nextNr.printSummary() + newline);
	}
    
	buf.append("Nested Record Lists belonging to this RecordInstance:" + newline);

	Map nestedRecordLists = getNestedRecordInstanceLists();
	Iterator recordListNames = nestedRecordLists.keySet().iterator();
	while (recordListNames.hasNext()){
	    String nextRecordListName = (String)recordListNames.next();
	    RecordInstance nextNrList[] = (RecordInstance[])nestedRecordLists.get(nextRecordListName);
	    buf.append ("***" + nextRecordListName + "***" + newline);
	    for (int i = 0; i < nextNrList.length; i++){
		buf.append(nextNrList[i].printSummary() + newline);
	    }
	}
	
	return buf.toString();
    }
    
    public String printSummary() throws WdkModelException {

	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer();
	
	Map attributeFields = getAttributes();
	
	HashMap summaryAttributes = new HashMap();
	HashMap nonSummaryAttributes = new HashMap();
	
	splitSummaryAttributes(attributeFields, summaryAttributes, nonSummaryAttributes);

	printAtts_Aux(buf, "Summary Attributes: " + newline, summaryAttributes);
	return buf.toString();
    }
	


    ///////////////////////////////////////////////////////////////////////////
    // package methods
    ///////////////////////////////////////////////////////////////////////////

    void setAnswer(Answer answer){
	this.answer = answer;
    }

    void setSummaryAttributeList(String[] summaryAttributeList){
	if (summaryAttributeList != null){
	    for (int i = 0; i < summaryAttributeList.length; i++){
		summaryAttributeMap.put(summaryAttributeList[i], new Integer(1));
	    }
	}
    }

    public boolean isSummaryAttribute(String attName){
	
	if (answer != null){
	    return answer.isSummaryAttribute(attName);
	}
	else return false;

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
	QueryInstance qInstance = query.makeInstance();
	qInstance.setIsCacheable(false);

	// If in the context of an Answer, then we are doing a "summary"
	// and need to do a join against the result table
	if (answer != null){
        answer.integrateAttributesQueryResult(qInstance);
	}	

	// otherwise, set values in record directly
	else{ 
 
        HashMap paramHash = new HashMap();
	    if (primaryKey == null) 
		throw new WdkModelException("primaryKey is null");

	    String projectId = primaryKey.getProjectId();
	    if (projectId != null) 
		paramHash.put(RecordClass.PROJECT_ID_NAME, projectId);
	    paramHash.put(RecordClass.PRIMARY_KEY_NAME, 
			  primaryKey.getRecordId());
	    
	    try {
		qInstance.setValues(paramHash);
	    } catch (WdkUserException e) {
		throw new WdkModelException(e);
	    }
	    
	    ResultList rl = qInstance.getResult();
	    
	    Column[] columns = query.getColumns();
	    //this could be factored a bit more cleanly, but it is a bit tricky, and this way works.
	    if (!rl.next()){
		for (int i=0; i<columns.length; i++) {
		    String columnName = columns[i].getName();
		    setAttributeValue(columnName, 
				      "null");
		}
	    }
	    else {
		for (int i=0; i<columns.length; i++) {
		    String columnName = columns[i].getName();
		    setAttributeValue(columnName, 
				      rl.getAttributeFieldValue(columnName).getValue());
		}
		if (rl.next()) {
		    String msg = "Attributes query '" + query.getFullName() + "' in Record '" + recordClass.getFullName() + "' returns more than one row";
		    throw new WdkModelException(msg);
		}
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

    /**
     * Given a map of all attributes in this recordInstance, separate them into those that are summary attributes
     * and those that are not summary attributes.  Place results into @param summaryAttributes and @param
     * nonSummaryAttributes.
     */
    
    private void splitSummaryAttributes(Map attributeFields, Map summaryAttributes, Map nonSummaryAttributes){

	Iterator fieldNames = attributeFields.keySet().iterator();
	//	if (fieldNames
	while (fieldNames.hasNext()) {
	    String fieldName = (String)fieldNames.next();
	    AttributeFieldValue field = 
		(AttributeFieldValue)attributeFields.get(fieldName);
	    if (field.isSummary()){
		summaryAttributes.put(fieldName, field);
	    }
	    else {
		nonSummaryAttributes.put(fieldName, field);
	    }
	}
    }


    private void printAtts_Aux(StringBuffer buf, String header, Map attributeFields){
	String newline = System.getProperty( "line.separator" );
	buf.append(header);
	
	Iterator fieldNames = attributeFields.keySet().iterator();
	while (fieldNames.hasNext()) {
	    String fieldName = (String)fieldNames.next();

	    AttributeFieldValue field = 
		(AttributeFieldValue)attributeFields.get(fieldName);
	    buf.append(field.getDisplayName() + ":   " + 
		       field.getBriefValue()).append( newline );
	}
	buf.append(newline);
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
