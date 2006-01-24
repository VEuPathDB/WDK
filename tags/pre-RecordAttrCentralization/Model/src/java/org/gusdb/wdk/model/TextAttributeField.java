package org.gusdb.wdk.model;

public class TextAttributeField implements FieldI {

    String name;
    String displayName;
    String text;
    String help;
    Boolean isInternal = new Boolean(false);
    Integer truncateTo;

    ////////////////////////////////////////////////////////////////
    //       FieldI
    ////////////////////////////////////////////////////////////////

    public String getName() {
	return name;
    }
   
    public String getDisplayName() {
	return (displayName != null)? displayName : name;
    }
    
    public String getHelp() {
	return help;
    }
   
    public String getType() {
	return null;
    }

    public Boolean getIsInternal() {
	return isInternal;
    }
   
    public Integer getTruncate(){
	return truncateTo;
    }

    public String toString() {
	return getDisplayName();
    }
    
    /////////////////////////////////////////////////////////
    //    settters
    /////////////////////////////////////////////////////////

    public void setName(String name) {
	this.name = name;
    }
    
    public void setHelp(String help) {
	this.help = help;
    }
    
    public void setIsInternal(Boolean isInternal) {
	this.isInternal = isInternal;
    }
    
    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }
    
    public void setText(String text) {
	this.text = text;
    }

    public void setTruncateTo(String truncateTo){
	this.truncateTo = new Integer(truncateTo);
    }

    /////////////////////////////////////////////////////////////////
    //             package
    /////////////////////////////////////////////////////////////////

    String getText() {
	return text;
    }

    
}
