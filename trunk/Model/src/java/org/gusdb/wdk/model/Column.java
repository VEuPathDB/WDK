package org.gusdb.gus.wdk.model;

public class Column {

    String typeName;
    String displayName;
    String name;
    String help;

    public Column() {} 

    public void setSpecialDataType(String typeName) {
	this.typeName = typeName;
    }

    public String getSpecialDataType() {
	return typeName;
    }

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
	if (displayName != null) return displayName;
	else return name;
    }
}

