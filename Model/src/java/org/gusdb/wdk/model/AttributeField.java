package org.gusdb.wdk.model;


public class AttributeField implements FieldI {

    private Column column;

    public AttributeField(Column column) {
	this.column = column;
    }

    public String getName() {
	return column.getName();
    }
	
    public String getDisplayName() {
	return column.getDisplayName();
    }
	
    public String getHelp() {
	return column.getHelp();
    }
	
    public String getType() {
	return column.getSpecialType();
    }
	
    public Boolean getIsInternal() {
	return column.getIsInternal();
    }
	
    public String toString() {
	return getDisplayName();
    }

    Query getQuery() {
	return column.getQuery();
    }

}

