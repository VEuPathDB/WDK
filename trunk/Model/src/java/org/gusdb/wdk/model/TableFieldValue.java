package org.gusdb.wdk.model;

import java.util.logging.Logger;
import java.util.Iterator;

public class TableFieldValue {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.FieldValue");
    
    FieldI field;
    ResultList resultList;
    FieldI[] columnFields = null;

    public TableFieldValue(FieldI field, ResultList resultList) {
	this.field = field;
	this.resultList = resultList;
    } 

    public String getName() {
        return field.getName();
    }

    public String getHelp() {
        return field.getHelp();
    }

    public String getDisplayName() {
        return field.getDisplayName();
    }

    /**
     * @returns A list of fields, one describing each column.
     */
    public FieldI[] getFields() {
	if (columnFields == null) {
	    Column[] columns = resultList.getColumns();
	    columnFields = new FieldI[columns.length];
	    for (int i=0; i< columns.length; i++) {
		columnFields[i] = new AttributeField(columns[i]);
	    }
	}
	return columnFields;
    }

    /**
     * @returns Iterator of Map: columnName --> AttributeFieldValue
     */
    public Iterator getRows() {
	return resultList.getRows();
    }

    public Object getClose() {
	try {
	    resultList.close(); 
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
	return null;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       String classnm = this.getClass().getName();
       StringBuffer buf = 
	   new StringBuffer(classnm + ": name='" + getName() + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  help='" + getHelp() + "'" + newline
			    );

       return buf.toString();
	
    }
    
}

