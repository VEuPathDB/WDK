package org.gusdb.wdk.model;




public class Column {

    // never used locally
    //private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.Column");
    
    String name;
    Query query;
    String dataTypeName;
    int width;  // for wsColumns (width of datatype)

    public Column() {} 

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSpecialType(String specialType) {
        this.dataTypeName = specialType;
    }

    public String getSpecialType() {
        return dataTypeName;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setWidth(int width) {
	this.width = width;
    }

    public Query getQuery() {
        return query;
    }

    public int getWidth() {
	return width;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       String classnm = this.getClass().getName();
       StringBuffer buf = 
	   new StringBuffer(classnm + ": name='" + name + "'" + newline +
			    "  dataTypeName='" + dataTypeName + "'" + newline);

       return buf.toString();
    }
}

