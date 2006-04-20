package org.gusdb.wdk.model;


public class AttributeFieldValue {

    //private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.FieldValue");
    
    AttributeField field;
    Object value;
    boolean isSummary;
    boolean needsTruncate;

    public AttributeFieldValue(AttributeField field, Object value) {
	this.field = field;
	this.value = value;
	this.isSummary = false;
    } 

    public String getName() {
        return field.getName();
    }

    public Boolean getInternal() {
        return field.getInternal();
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

    public Object getBriefValue(){

	if (value == null){
	    return null;
	}
	if (value instanceof LinkAttributeField) 
	    return ((LinkAttributeField)value).getUrl();

	if (value instanceof LinkValue) 
	    return ((LinkValue)value).getUrl();

	String briefValue = value.toString();
	
	int truncate = field.getTruncateTo();
	if (truncate == 0){
	    truncate = WdkModel.TRUNCATE_DEFAULT;
	}

	if (briefValue.length() <= truncate){
	    return briefValue;
	}
	else {
	    String returned = briefValue.substring(0, truncate) + ". . .";
	    return returned;
	}
    }

    public boolean isSummary() {
	return this.isSummary;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        String classnm = this.getClass().getName();
        StringBuffer buf = new StringBuffer(classnm + ": name = '" + getName()
                + "'" + newline + "  displayName = '" + getDisplayName() + "'"
                + newline + "  help = '" + getHelp() + "'" + newline
                + "  inSummary? = '" + isSummary() + "'" + newline
                + "  value = '" + getValue() + "'" + newline);
        return buf.toString();
    }

    void setIsSummary(boolean isSummary){
	this.isSummary = isSummary;
    }
    
}

