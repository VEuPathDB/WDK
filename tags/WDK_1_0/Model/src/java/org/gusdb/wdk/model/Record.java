package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Record {
    
    public static final String PRIMARY_KEY_NAME = "primary_key";

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.Record");
    
    private Map attributesQueryMap = new LinkedHashMap();  // attributeName -> Query
    private Map tableQueryMap = new LinkedHashMap();   // tableName -> Query
    private Map textAttributeMap = new LinkedHashMap();    // attributeName -> TextAttribute
    private List summaryColumnNames = new ArrayList();
    private Set tableQueryRefs = new LinkedHashSet();
    private Set attributesQueryRefs = new LinkedHashSet();
    private Set allNames;
    private Set allAttributeNames;
    private String name;
    private String type;
    private String idPrefix;
    private String fullName;

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFullName() {
	return fullName;
    }

    /**
     * Assumes that the name of this record has already been set.  Note this is slightly
     * different than a simple accessor in that the full name of the record is <code>recordSetName</code>
     * concatenated with ".recordName".
     *
     * @param recordSetName name of the recordSet to which this record belongs.
     */
    public void setFullName(String recordSetName){
	this.fullName = recordSetName + "." + name;
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
    }
    
    
    
    public void addTextAttribute(TextAttribute textAttribute) throws WdkModelException {
        checkAttributeName(textAttribute.getName());
        textAttributeMap.put(textAttribute.getName(), textAttribute);
    }
    
    public RecordInstance makeRecordInstance() {
        return new RecordInstance(this);
    }
    
    public String toString() {
        String newline = System.getProperty( "line.separator" );
        StringBuffer buf =
            new StringBuffer("Record: name='" + name + "'").append( newline );
        
        buf.append( "--- Attributes ---" ).append( newline );
        Iterator attributeNamesIt = getNonTextAttributeNames().iterator();
        while (attributeNamesIt.hasNext()) {
            buf.append(attributeNamesIt.next()).append( newline );
        }
        
        buf.append( "--- Text Attributes ---" ).append( newline );
        Iterator textAttributeNamesIt = getTextAttributeNames().iterator();
        while (textAttributeNamesIt.hasNext()) {
            buf.append(textAttributeNamesIt.next());
            buf.append(newline);
        }
        
        buf.append( "--- Tables ---" ).append( newline );
        Iterator tableIterator = getTableNames().iterator();
        while (tableIterator.hasNext()) {
            buf.append(tableIterator.next()).append( newline );
        }
        return buf.toString();
    }
    
    public Set getTableNames() {
        return tableQueryMap.keySet();
    }


    public Set getNonTextAttributeNames() {
	LinkedHashSet orderedSet = new LinkedHashSet();
	orderedSet.add(PRIMARY_KEY_NAME);
	orderedSet.addAll( attributesQueryMap.keySet());
        return orderedSet;
    }
    

    public Set getTextAttributeNames() {
        return textAttributeMap.keySet();
    }

    public Set getAllAttributeNames() {
	if (allAttributeNames == null) {
	    allAttributeNames = new LinkedHashSet();
	    allAttributeNames.addAll(getNonTextAttributeNames());
	    allAttributeNames.addAll(getTextAttributeNames());
	}
	return allAttributeNames;
    }

    public Set getAllNames() {
	if (allNames == null) {
	    allNames = new LinkedHashSet();
	    allNames.addAll(getNonTextAttributeNames());
	    allNames.addAll(getTextAttributeNames());
	    allNames.addAll(getTableNames());
	}
        return allNames;
    }

    public boolean containsName(String name) {
	return getAllNames().contains(name);
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
            if (column.isInSummaryAsBool()) {
                summaryColumnNames.add(column.getName());
            }
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
	    || textAttributeMap.containsKey(name)
	    || getType().equals(name)) {
            throw new WdkModelException("Record " + getName() + 
                    " already has a attribute named " + name);
        }
    }
    
    protected void checkQueryParams(Query query, String queryType) throws WdkModelException {
        
        String s = "The " + queryType + " " + query.getName() + 
        " contained in Record " + getName();
        Param[] params = query.getParams();
        if (params.length != 1 || !params[0].getName().equals("primaryKey")) 
            throw new WdkModelException(s + " must have only one param, and it must be named 'primaryKey'");
    }
    
    protected Query getAttributesQuery(String attributeName) throws WdkModelException {
        Query query = (Query)attributesQueryMap.get(attributeName);
        if (query == null) {
            throw new WdkModelException("Record " + getName() + 
                    " doesn't have an attribute with name '" +
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
        TextAttribute textAttr = 
	    (TextAttribute)textAttributeMap.get(textAttributeName);
        String text = textAttr.getText();
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
    
    public Reference getReference() throws WdkModelException {
        return new Reference(getFullName());
    }

    /**
     * @return
     */
    public List getSummaryColumnNames() {
        return summaryColumnNames;
    }

    /**
     * @param name2
     * @return
     * @throws 
     */
    public String getDisplayName(String attributeName) {
	String displayName = attributeName;

	if (attributeName.equals(PRIMARY_KEY_NAME)) {
	    displayName = getType();

	} else if (isTextAttribute(attributeName)) {
	    TextAttribute textAttr = 
		(TextAttribute)textAttributeMap.get(attributeName);
	    displayName = textAttr.getDisplayName();

	} else {
	    Query q = (Query) attributesQueryMap.get(attributeName);
	    try {
		Column c = q.getColumn(attributeName);
		displayName = c.getDisplayName();
	    }
	    catch (WdkModelException exp) {
		logger.severe("Can't get displayName for "+attributeName+" in "+q.getName());
	    }
	}
	return displayName;
    }
}
