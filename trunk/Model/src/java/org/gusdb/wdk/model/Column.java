package org.gusdb.gus.wdk.model;

public class Column {

    String displayName;
    String name;
    Query query;
    String help;

    public Column() {} 

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setQuery(Query query) {
	this.query = query;
    }

    public Query getQuery() {
	return query;
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

