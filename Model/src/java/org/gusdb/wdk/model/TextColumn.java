package org.gusdb.wdk.model;

/**
 * A subclass of Column which returns a text string containing interpolated values.  
 */
public class TextColumn extends Column implements DerivedColumnI {

    String text;

    public TextColumn() {
	super();
    }

    public void setText(String text) {
	this.text = text;
    }

    public Object getDerivedValue(ResultList resultList) throws WdkModelException {
	Column[] columns = query.getColumns();
	String instantiatedText = text;
	for (int i =0; i<columns.length; i++) {
	    Column column = columns[i];
	    if (column instanceof DerivedColumnI) continue;
	    String columnName = column.getName();

	    Object columnVal = 
		resultList.getAttributeFieldValue(columnName).getValue();
	    String columnValStr = columnVal == null ? "???" : columnVal.toString();
	    instantiatedText = 
		RecordInstance.instantiateText(instantiatedText, columnName, columnValStr);
	    
	}
	RecordInstance.checkInstantiatedText(instantiatedText);
	return instantiatedText;
    }

}

