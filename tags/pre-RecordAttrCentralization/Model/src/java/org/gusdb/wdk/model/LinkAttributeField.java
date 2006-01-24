package org.gusdb.wdk.model;


public class LinkAttributeField implements FieldI {

    String name;
    String displayName;
    String visible;
    String url;
    String help;
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
	return new Boolean(false);
    }
    
    public Integer getTruncate(){
	return truncateTo;
    }   
    
    public String toString() {
	return getDisplayName();
    }


    ////////////////////////////////////////////////////////////
    //    setters
    ////////////////////////////////////////////////////////////


    public void setTruncateTo(String truncateTo){
	this.truncateTo = new Integer(truncateTo);
    }

    public void setName(String name) {
	this.name = name;
    }
    
    public void setHelp(String help) {
	this.help = help;
    }
    
    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }
    
    public void setVisible(String visible) {
	this.visible = visible;
    }

    public void setUrl(String url) {
	this.url = url;
    }


    ////////////////////////////////////////////////////////////
    //      package
    ////////////////////////////////////////////////////////////

    String getUrl() {
	return url;
    }

    String getVisible() {
	return visible;
    }

}
