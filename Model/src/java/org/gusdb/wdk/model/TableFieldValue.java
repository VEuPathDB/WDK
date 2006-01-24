package org.gusdb.wdk.model;

import java.util.logging.Logger;
import java.util.Iterator;

public class TableFieldValue {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.FieldValue");
    
    TableField tableField;
    ResultList resultList;
    AttributeField[] columnFields = null;

    public TableFieldValue(TableField field, ResultList resultList) {
	this.tableField = field;
	this.resultList = resultList;
    } 

    public String getName() {
        return tableField.getName();
    }

    public String getHelp() {
        return tableField.getHelp();
    }

    public String getDisplayName() {
        return tableField.getDisplayName();
    }

    public Boolean getInternal() {
        return tableField.getInternal();
    }

    /**
     * @return A list of fields, one describing each column.
     */
    public AttributeField[] getAttributeFields() {
        return tableField.getAttributeFields();
    }

    /**
     * @return A list of rows where each row is a Map of columnName --> {@link AttributeFieldValue}
     */
    public Iterator getRows() {
	return resultList.getRows();
    }

    /**
     * Must be called to close the table.
     * @return null
     */
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
    
    void closeResult() throws WdkModelException {
	
	if (resultList != null){
	    resultList.close();
	}

    }
    
    
}

