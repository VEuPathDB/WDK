package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;

public class Record {
    
    HashMap fieldsQueryMap;  // fieldName -> SimpleQueryI
    HashMap tableQueryMap;   // tableName -> SimpleQueryI
    HashMap textFieldMap;    // fieldName -> text (String)
    HashSet tableQueryRefs;
    HashSet fieldsQueryRefs;
    String name;
    String type;
    String idPrefix;
    
    public Record() {
	fieldsQueryMap = new HashMap();
	tableQueryMap = new HashMap();
	fieldsQueryRefs = new HashSet();
	tableQueryRefs = new HashSet();
	textFieldMap = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setIdPrefix(String idPrefix) {
	this.idPrefix = idPrefix;
    }

    public String getIdPrefix() {
	return idPrefix;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getType() {
	return type;
    }

    public String getFieldSpecialType(String fieldName) {
	return null;
    }

    /**
     * @param fieldsQueryRef two part query name (set.name)
     */
    public void addFieldsQueryRef(Reference fieldsQueryRef) {
	fieldsQueryRefs.add(fieldsQueryRef.getReferent());
    }

    /**
     * @param tableQueryRef two part query name (set.name)
     */
    public void addTableQueryRef(Reference tableQueryRef) {
	tableQueryRefs.add(tableQueryRef.getReferent());
    }

    public void resolveReferences(Map querySetMap) throws Exception {
	Iterator fQueryRefs = fieldsQueryRefs.iterator();
	while (fQueryRefs.hasNext()) {
	    String queryName = (String)fQueryRefs.next();
	    SimpleQueryI query = 
		(SimpleQueryI)SimpleQuerySet.resolveReference(querySetMap, 
							      queryName,
							      this.getClass().getName(),
							      getName(),
							      "fieldsQueryRef");
	    addFieldsQuery(query);
	}

	Iterator tQueryRefs = tableQueryRefs.iterator();
	while (tQueryRefs.hasNext()) {
	    String queryName = (String)tQueryRefs.next();
	    SimpleQueryI query = 
		(SimpleQueryI)SimpleQuerySet.resolveReference(querySetMap, 
							      queryName,
							      this.getClass().getName(),
							      getName(),
							      "tableQueryRef");
	    addTableQuery(query);
	}
    }

    public void addTextField(TextField textField) throws Exception {
	checkFieldName(textField.getName());
	textFieldMap.put(textField.getName(), textField.getText());
    }

    public RecordInstance makeInstance() {
	return new RecordInstance(this);
    }

    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf =
	    new StringBuffer("Record: name='" + name + "'").append( newline );

	buf.append( "--- Fields ---" ).append( newline );
	String[] fieldNames = getNonTextFieldNames();
	for (int i=0; i<fieldNames.length; i++) {
	    buf.append(fieldNames[i]).append( newline );
	}
	
	buf.append( "--- Text Fields ---" ).append( newline );
	String[] textFieldNames = getTextFieldNames();
	for (int i=0; i<textFieldNames.length; i++){
	    buf.append(textFieldNames[i]).append( newline );
	}
	
	buf.append( "--- Tables ---" ).append( newline );
	Iterator tableIterator = tableQueryMap.keySet().iterator();
	while (tableIterator.hasNext()) {
	    buf.append(tableIterator.next()).append( newline );
	}
	return buf.toString();
    }
    
    public String[] getTableNames() {
	String[] s = {};
	return (String[])tableQueryMap.keySet().toArray(s);
    }

    public String[] getNonTextFieldNames() {
	String[] s = {};
	return (String[])fieldsQueryMap.keySet().toArray(s);
    }

    public String[] getTextFieldNames() {
	String[] s = {};
	return (String[])textFieldMap.keySet().toArray(s);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // protected
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a fields query. Map it by all of its column names
     */
    protected void addFieldsQuery(SimpleQueryI query) throws Exception {
	Column[] columns = query.getColumns();
	for (int i=0; i<columns.length;i++) {
	    Column column = columns[i];
	    checkFieldName(column.getName());
	    fieldsQueryMap.put(column.getName(), query);
	}
    }

    /**
     * Add a table query. Map it by its query name
     */
    protected void addTableQuery(SimpleQueryI query) throws Exception {
	if (tableQueryMap.containsKey(query.getName())) {
	    throw new Exception("already have table query named " + query.getName());
	}
	tableQueryMap.put(query.getName(), query);
    }

    protected void checkFieldName(String name) throws Exception {
	if (fieldsQueryMap.containsKey(name)) {
	    throw new Exception("already have a field named" + name);
	}
	if (textFieldMap.containsKey(name)) {
	    throw new Exception("already have a field named" + name);
	}
	
    }

    protected void checkQueryParams(SimpleQueryI query, String queryType) {

	String s = "The " + queryType + " " + query.getName() + 
	    " contained in Record " + getName();
	Param[] params = query.getParams();
	if (params.length != 1 || !params[0].getName().equals("primaryKey")) 
	    throw new IllegalArgumentException(s + " must have only one param, and it must be named 'primaryKey'");
    }

    protected SimpleQueryI getFieldsQuery(String fieldName) throws Exception {
	SimpleQueryI query = (SimpleQueryI)fieldsQueryMap.get(fieldName);
	if (query == null) {
	    throw new Exception("Record " + getName() + 
				" does not have a field with name '" +
				fieldName + "'");
	}
	return query;
    }

    protected SimpleQueryI getTableQuery(String tableQueryName) throws Exception {
	SimpleQueryI query = (SimpleQueryI)tableQueryMap.get(tableQueryName);
	if (query == null) {
	    throw new Exception("Record " + getName() + 
				" does not have a tableQuery with name '" +
				tableQueryName + "'");
	}
	return query;
    }

    protected String getTextField(String textFieldName) throws Exception {
	String text = (String)textFieldMap.get(textFieldName);
	if (text == null) {
	    throw new Exception("Record " + getName() + 
				" does not have a text field with name '" +
				textFieldName + "'");
	}
	return text;
    }

}
