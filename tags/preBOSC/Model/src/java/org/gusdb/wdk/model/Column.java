package org.gusdb.gus.wdk.model;

public class Column {

    String displayName;
    String name;
    Query query;
    String help;
    String dataTypeName;

    public Column() {} 

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getDataTypeName() {
        return dataTypeName;
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
        return name;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       String classnm = this.getClass().getName();
       StringBuffer buf = 
	   new StringBuffer(classnm + ": name='" + name + "'" + newline +
			    "  displayName='" + displayName + "'" + newline +
			    "  help='" + help + "'" + newline +
			    "  dataTypeName='" + dataTypeName + "'" + newline
			    );

       return buf.toString();
	
    }
}

