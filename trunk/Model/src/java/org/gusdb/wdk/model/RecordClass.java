package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class RecordClass {
    
    static final String PRIMARY_KEY_NAME = "primaryKey";

    private static final Logger logger = 
	WdkLogManager.getLogger("org.gusdb.wdk.model.Record");
    
    private Set tableQueryRefs = new LinkedHashSet();
    private Set attributesQueryRefs = new LinkedHashSet();
    private Map attributeFieldsMap = new LinkedHashMap(); 
    private Map tableFieldsMap = new LinkedHashMap();  
    private Map fieldsMap = new LinkedHashMap();  
    private Set allNames;
    private List summaryColumnNames = new ArrayList();
    private String name;
    private String type;
    private String idPrefix;
    private String fullName;
    private String summaryAttributeList;
    private String attributeOrdering;
    private HashMap questions = new HashMap();

    public RecordClass() {
	// make sure these keys are at the front of the list
	// (don't assume setType is called before adding attributes)
	attributeFieldsMap.put(PRIMARY_KEY_NAME, null);	    
	fieldsMap.put(PRIMARY_KEY_NAME, null);	    
    }

    //////////////////////////////////////////////////////////////////////
    // Called at model creation time
    //////////////////////////////////////////////////////////////////////

    public void setName(String name) {
        this.name = name;
    }
    
    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }
    
    public void setType(String type) {
        this.type = type;
	PrimaryKeyField pkField = 
	    new PrimaryKeyField(PRIMARY_KEY_NAME, getType(),
				"Some help here");
	attributeFieldsMap.put(PRIMARY_KEY_NAME, pkField);	    
	fieldsMap.put(PRIMARY_KEY_NAME, pkField);	    
    }

    /** 
     * @param attList comma separated list of attributes in a summary containing
     * this recordClass.
     */
    /*public void setSummaryAttributeList (String attList){
	this.summaryAttributeList = attList;
	}*/

    public void setAttributeOrdering (String attOrder) {
	this.attributeOrdering = attOrder;
    }

    /**
     * @param attributesQueryRef two part query name (set.name)
     */
    public void addAttributesQueryRef(Reference attributesQueryRef) {
        attributesQueryRefs.add(attributesQueryRef.getTwoPartName());
    }
    
    /**
     * @param tableQueryRef two part query name (set.name)
     */
    public void addTableQueryRef(Reference tableQueryRef) {
        tableQueryRefs.add(tableQueryRef.getTwoPartName());
    }
    
    public void addTextAttribute(TextAttributeField textAttributeField) throws WdkModelException {
        checkAttributeName(textAttributeField.getName());
	attributeFieldsMap.put(textAttributeField.getName(), 
			       textAttributeField);	    
	fieldsMap.put(textAttributeField.getName(), textAttributeField);	    
    }
    
    public void addLinkAttribute(LinkAttributeField linkAttributeField) throws WdkModelException {
        checkAttributeName(linkAttributeField.getName());
	attributeFieldsMap.put(linkAttributeField.getName(), 
			       linkAttributeField);	    
	fieldsMap.put(linkAttributeField.getName(), linkAttributeField);	    
    }

    public void addQuestion(Question q){
	questions.put(q.getFullName(), q);
    }
    
    //////////////////////////////////////////////////////////////
    // public getters
    //////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }
    
    public String getFullName() {
	return fullName;
    }

    public String getIdPrefix() {
        return idPrefix;
    }
    
    public String getType() {
        return type;
    }
    
    public Map getTableFields() {
        return new LinkedHashMap(tableFieldsMap);
    }

    public Map getAttributeFields() {
        return new LinkedHashMap(attributeFieldsMap);
    }

    public Map getFields() {
        return new LinkedHashMap(fieldsMap);
    }

    public FieldI getField(String fieldName) {
	return (FieldI) fieldsMap.get(fieldName);
    }

    /**
     * @return all Questions in the current model that are using this record class as their return type.
     */
    public Question[] getQuestions(){
	Iterator it = questions.values().iterator();
	
	Question[] returnedQuestions = new Question[questions.size()];
	int i = 0;
	while (it.hasNext()){
	    Question nextQuestion = (Question)it.next();
	    returnedQuestions[i] = nextQuestion;
	    i++;
	}
	return returnedQuestions;
    }    
    /**
     * @return
     */
    public List getSummaryColumnNames() {
	ArrayList list = new ArrayList();
	list.add(PRIMARY_KEY_NAME);
	list.addAll(summaryColumnNames);
	return list;
    }

    public Reference getReference() throws WdkModelException {
        return new Reference(getFullName());
    }

    public RecordInstance makeRecordInstance() {
        return new RecordInstance(this);
    }
    
    public String toString() {
        String newline = System.getProperty( "line.separator" );
        StringBuffer buf =
            new StringBuffer("Record: name='" + name + "'").append( newline );
        
        buf.append( "--- Attributes ---" ).append( newline );
        Iterator attributes = attributeFieldsMap.values().iterator();
        while (attributes.hasNext()) {
            buf.append(attributes.next()).append( newline );
        }
        
        buf.append( "--- Tables ---" ).append( newline );
        Iterator tables = tableFieldsMap.values().iterator();
        while (tables.hasNext()) {
            buf.append(tables.next()).append( newline );
	}
        return buf.toString();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // package scope methods
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * @param recordSetName name of the recordSet to which this record belongs.
     */
    void setFullName(String recordSetName){
	this.fullName = recordSetName + "." + name;
    }

    /**
     * Add a attributes query. Map it by all of its column names
     */
    void addAttributesQuery(Query query) throws WdkModelException {
        Column[] columns = query.getColumns();
        for (int i=0; i<columns.length;i++) {
            Column column = columns[i];
            checkAttributeName(column.getName());
            if (column.isInSummaryAsBool()) {
                summaryColumnNames.add(column.getName());
            }
	    AttributeField field = new AttributeField(column);
            attributeFieldsMap.put(field.getName(), field);	    
            fieldsMap.put(field.getName(), field);	    
        }
    }
    
    /**
     * Add a table query. Map it by its query name
     */
    void addTableQuery(Query query) throws WdkModelException {
        if (tableFieldsMap.containsKey(query.getName())) {
            throw new WdkModelException("Record " + getName() + 
					" already has table query named " + 
					query.getName());
        }    
	TableField field = new TableField(query);
	tableFieldsMap.put(field.getName(), field);	    
	fieldsMap.put(field.getName(), field);	    
    }
    
    AttributeField getAttributeField(String attributeName) throws WdkModelException {
        AttributeField field= 
	    (AttributeField)attributeFieldsMap.get(attributeName);
        if (field == null) {
            throw new WdkModelException("RecordClass " + getName() + 
                    " doesn't have an attribute with name '" +
                    attributeName + "'");
        }
        return field;
    }
    
    TableField getTableField (String tableName) throws WdkModelException {
        TableField field = (TableField)tableFieldsMap.get(tableName);
        if (field == null) {
            throw new WdkModelException("Record " + getName() + 
                    " does not have a table field with name '" +
                    tableName + "'");
        }
        return field;
    }

    
    void checkAttributeName(String name) throws WdkModelException {
        if (attributeFieldsMap.containsKey(name)) {
            throw new WdkModelException("Record " + getName() + 
                    " already has a attribute named " + name);
        }
    }
    
    void checkQueryParams(Query query, String queryType) throws WdkModelException {
        
        String s = "The " + queryType + " " + query.getName() + 
        " contained in Record " + getName();
        Param[] params = query.getParams();
        if (params.length != 1 || !params[0].getName().equals("primaryKey")) 
            throw new WdkModelException(s + " must have only one param, and it must be named 'primaryKey'");
    }
    
    void resolveReferences(WdkModel model) throws WdkModelException {
        
        Iterator fQueryRefs = attributesQueryRefs.iterator();
        while (fQueryRefs.hasNext()) {
            String queryName = (String)fQueryRefs.next();
            Query query = 
                (Query)model.resolveReference(queryName,
					      getName(), 
					      this.getClass().getName(),
					      "attributesQueryRef");
            addAttributesQuery(query);
        }
        attributesQueryRefs = null;
        
        Iterator tQueryRefs = tableQueryRefs.iterator();
        while (tQueryRefs.hasNext()) {
            String queryName = (String)tQueryRefs.next();
            Query query = 
                (Query)model.resolveReference(queryName,
                        getName(), 
                        "record",
                "tableQueryRef");
            addTableQuery(query);
        }
        tableQueryRefs = null;
	if (attributeOrdering != null){
	    LinkedHashMap orderedAttributes = sortAllAttributes();
	    attributeFieldsMap = orderedAttributes;
	}
    }

    private LinkedHashMap sortAllAttributes() throws WdkModelException{
	String orderedAtts[] = attributeOrdering.split(",");
	LinkedHashMap orderedAttsMap = new LinkedHashMap();
	for (int i = 0; i < orderedAtts.length; i++){
	    String nextAtt = orderedAtts[i];
	    FieldI nextAttField = (FieldI)attributeFieldsMap.get(nextAtt);
	    
	    if (nextAttField == null){
		throw new WdkModelException("RecordClass " + getName() + " defined attribute " +
					    nextAtt + " in its attribute ordering, but that is not a " + 
					    "valid attribute for this RecordClass");
	    }
	    orderedAttsMap.put(nextAtt, nextAttField);
    	}
	//add all attributes not in the ordering
	Iterator allAttNames = attributeFieldsMap.keySet().iterator();
	while (allAttNames.hasNext()){
	    String nextAtt = (String)allAttNames.next();
	    if (!orderedAttsMap.containsKey(nextAtt)){
		
		FieldI nextField = (FieldI)attributeFieldsMap.get(nextAtt);
		orderedAttsMap.put(nextAtt, nextField);
	    }
	}
	return orderedAttsMap;
    }
    





}
