package org.gusdb.wdk.model;

/**
 * A subclass of Column which returns a Link object
 */
public class LinkColumn extends Column implements DerivedColumnI {

    String visible;
    String url;

    public LinkColumn() {
	super();
    }

    /**
     * A string that may have macros for values from other columns
     */
    public void setVisible(String visible) {
	this.visible = visible;
    }

    /**
     * A string that may have macros for values from other columns
     */
    public void setURL(String url) {
	this.url = url;
    }

    public Object getDerivedValue(ResultList resultList) throws WdkModelException {
	String instantiatedVisible = instantiateText(visible, resultList);
	String instantiatedUrl = instantiateText(url, resultList);

	return new LinkValue(instantiatedVisible, instantiatedUrl);
    }

    ////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////

    private String instantiateText(String text, ResultList resultList) throws WdkModelException {
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

