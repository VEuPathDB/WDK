package org.gusdb.wdk.model;

public class PrimaryKeyField implements FieldI {

    private String name;
    private String displayName;
    private String help;
    private Integer truncate;

    public PrimaryKeyField(String name, String displayName, String help) {
	this.name = name;
	this.displayName = displayName;
	this.help = help;
    }

    public String getName() {
	return name;
    }
	
    public Boolean getIsInternal() {
	return new Boolean(false);
    }
   
    public String getDisplayName() {
	return displayName;
    }
	
    public String getHelp() {
	return help;
    }

    public String getType() {
	return null;
    }

    public String toString() {
	return getDisplayName();
    }

    public void setTruncate(String truncate){
	this.truncate = new Integer(truncate);
    }

    public Integer getTruncate(){
	return truncate;
    }


}

