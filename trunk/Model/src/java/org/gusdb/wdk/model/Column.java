package org.gusdb.wdk.model;



import java.util.logging.Logger;

public class Column {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.Column");
    
    String displayName;
    String name;
    Query query;
    String help;
    String dataTypeName;
    private boolean inSummary = true;

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
			    "  dataTypeName='" + dataTypeName + "'" + newline +
                "  inSummary='" + inSummary +"'" + newline
			    );

       return buf.toString();
	
    }
    
    public String isInSummary() {
        return inSummary? "true" : "false";
    }
    
    public boolean isInSummaryAsBool() {
        return inSummary;
    }
    
    public void setInSummary(String in) {
        this.inSummary = Boolean.valueOf(in.trim()).booleanValue();
    }
}

