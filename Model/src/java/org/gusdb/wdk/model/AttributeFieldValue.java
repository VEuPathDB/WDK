package org.gusdb.wdk.model;

import java.util.logging.Logger;

public class AttributeFieldValue {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.FieldValue");
    
    FieldI field;
    Object value;
    boolean isSummary;

    public AttributeFieldValue(FieldI field, Object value) {
	this.field = field;
	this.value = value;
	this.isSummary = false;
    } 

    public String getName() {
        return field.getName();
    }

    public Boolean getIsInternal() {
        return field.getIsInternal();
    }

    public String getHelp() {
        return field.getHelp();
    }

    public String getDisplayName() {
        return field.getDisplayName();
    }

    public Object getValue() {
	return value;
    }

    public boolean isSummary() {
	return this.isSummary;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       String classnm = this.getClass().getName();
       StringBuffer buf = 
	   new StringBuffer(classnm + ": name='" + getName() + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  help='" + getHelp() + "'" + newline + "isSummary? = '" + isSummary() + "'" + newline
			    );

       return buf.toString();
	
    }

    void setIsSummary(boolean isSummary){
	this.isSummary = isSummary;
    }
    
}

