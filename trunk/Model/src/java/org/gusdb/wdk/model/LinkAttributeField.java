package org.gusdb.wdk.model;


public class LinkAttributeField implements FieldI {

    String name;
    String displayName;
    String visible;
    String url;
    String help;

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }
   
    public void setHelp(String help) {
	this.help = help;
    }
    
    public String getHelp() {
	return help;
    }
   
    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }
    
    public String getDisplayName() {
	return (displayName != null)? displayName : name;
    }
    
    public Boolean getIsInternal() {
	return new Boolean(false);
    }

    public void setVisible(String visible) {
	this.visible = visible;
    }

    public String getVisible() {
	return visible;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getUrl() {
	return url;
    }

   public String getType() {
	return null;
    }

    public String toString() {
	return getDisplayName();
    }
    
}
