package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Record {
    
    HashMap attributesQueryMap;  // attributeName -> Query
    HashMap tableQueryMap;   // tableName -> Query
    HashMap textAttributeMap;    // attributeName -> text (String)
    HashSet tableQueryRefs;
    HashSet attributesQueryRefs;
    String name;
    String type;
    String idPrefix;
    
    public Record() {
	attributesQueryMap = new HashMap();
	tableQueryMap = new HashMap();
	attributesQueryRefs = new HashSet();
	tableQueryRefs = new HashSet();
	textAttributeMap = new HashMap();
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

    public String getAttributeSpecialType(String attributeName) {
	return null;
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

    public void resolveReferences(WdkModel model) throws WdkModelException {
	
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
    }



    public void addTextAttribute(TextAttribute textAttribute) throws WdkModelException {
	checkAttributeName(textAttribute.getName());
	textAttributeMap.put(textAttribute.getName(), textAttribute.getText());
    }

    public RecordInstance makeInstance() {
	return new RecordInstance(this);
    }

    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf =
	    new StringBuffer("Record: name='" + name + "'").append( newline );

	buf.append( "--- Attributes ---" ).append( newline );
	String[] attributeNames = getNonTextAttributeNames();
	for (int i=0; i<attributeNames.length; i++) {
	    buf.append(attributeNames[i]).append( newline );
	}
	
	buf.append( "--- Text Attributes ---" ).append( newline );
	String[] textAttributeNames = getTextAttributeNames();
	for (int i=0; i<textAttributeNames.length; i++){
	    buf.append(textAttributeNames[i]).append( newline );
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

    public String[] getNonTextAttributeNames() {
	String[] s = {};
	return (String[])attributesQueryMap.keySet().toArray(s);
    }

    public String[] getTextAttributeNames() {
	String[] s = {};
	return (String[])textAttributeMap.keySet().toArray(s);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // protected
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a attributes query. Map it by all of its column names
     */
    protected void addAttributesQuery(Query query) throws WdkModelException {
	Column[] columns = query.getColumns();
	for (int i=0; i<columns.length;i++) {
	    Column column = columns[i];
	    checkAttributeName(column.getName());
	    attributesQueryMap.put(column.getName(), query);
	}
    }

    /**
     * Add a table query. Map it by its query name
     */
    protected void addTableQuery(Query query) throws WdkModelException {
	if (tableQueryMap.containsKey(query.getName())) {
	    throw new WdkModelException("Record " + getName() + " already has table query named " + query.getName());
	}
	tableQueryMap.put(query.getName(), query);
    }

    protected void checkAttributeName(String name) throws WdkModelException {
	if (attributesQueryMap.containsKey(name) 
	    || textAttributeMap.containsKey(name)) {
	    throw new WdkModelException("Record " + getName() + 
				" already has a attribute named " + name);
	}
    }

    protected void checkQueryParams(Query query, String queryType) throws WdkUserException {

	String s = "The " + queryType + " " + query.getName() + 
	    " contained in Record " + getName();
	Param[] params = query.getParams();
	if (params.length != 1 || !params[0].getName().equals("primaryKey")) 
	    throw new WdkUserException(s + " must have only one param, and it must be named 'primaryKey'");
    }

    protected Query getAttributesQuery(String attributeName) throws WdkModelException {
	Query query = (Query)attributesQueryMap.get(attributeName);
	if (query == null) {
	    throw new WdkModelException("Record " + getName() + 
				" does not have a attribute with name '" +
				attributeName + "'");
	}
	return query;
    }

    protected Query getTableQuery(String tableQueryName) throws WdkModelException {
	Query query = (Query)tableQueryMap.get(tableQueryName);
	if (query == null) {
	    throw new WdkModelException("Record " + getName() + 
				" does not have a tableQuery with name '" +
				tableQueryName + "'");
	}
	return query;
    }

    protected String getTextAttribute(String textAttributeName) throws WdkModelException {
	String text = (String)textAttributeMap.get(textAttributeName);
	if (text == null) {
	    throw new WdkModelException("Record " + getName() + 
				" does not have a text attribute with name '" +
				textAttributeName + "'");
	}
	return text;
    }

    protected boolean isTextAttribute(String attributeName) {
	return textAttributeMap.containsKey(attributeName);
    }
}
