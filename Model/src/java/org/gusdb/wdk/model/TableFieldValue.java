package org.gusdb.wdk.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

public class TableFieldValue {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.FieldValue");
    
    TableField tableField;
    ResultList resultList;
    AttributeField[] columnFields = null;

    public TableFieldValue(TableField field, ResultList resultList) {
        this.tableField = field;
        this.resultList = resultList;

        // set the order of the resultList
        Set<String> names = new LinkedHashSet<String>();
        AttributeField[] attributeFields = tableField.getAttributeFields();
        for (AttributeField attributeField : attributeFields) {
            // only use those ColumnAttributeFields
            if (attributeField instanceof ColumnAttributeField)
                names.add(attributeField.getName());
        }
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
        return new TableFieldValueIterator(resultList.getRows());
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
    
    public void write(StringBuffer buf) throws WdkModelException {
        String newline = System.getProperty("line.separator");
        Iterator rows = getRows();
        while (rows.hasNext()) {
            Map rowMap = (Map) rows.next();
            Iterator colNames = rowMap.keySet().iterator();
            while (colNames.hasNext()) {
                String colName = (String)colNames.next();
                Object fVal = rowMap.get(colName);
                buf.append(fVal);
                buf.append("\t");
            }
            buf.append(newline);
        }
        resultList.close();
    }
    
    void closeResult() throws WdkModelException {

        if (resultList != null) {
            resultList.close();
        }

    }
    
    private String instantiateText(String initialText,
            Map<String, Object> resultListRow, Set<String> columnNames)
            throws WdkModelException {
        String finalText = initialText;
        for (String columnName : columnNames) {
            Object columnValue = resultListRow.get(columnName);
            String columnValStr = columnValue == null ? "???"
                    : columnValue.toString();
            finalText = RecordInstance.instantiateText(finalText, columnName,
                    columnValStr);
        }
        RecordInstance.checkInstantiatedText(finalText);
        return finalText;
    }
    


    public void toXML(StringBuffer buf, String rowTag, String ident)
            throws WdkModelException {
        String newline = System.getProperty("line.separator");
        Iterator rows = getRows();
        while (rows.hasNext()) {
            buf.append(ident + "<" + rowTag + ">" + newline);
            Map rowMap = (Map)rows.next();
            Iterator colNames = rowMap.keySet().iterator();
            while (colNames.hasNext()) {
                String colName = (String)colNames.next();
                // get the field
                AttributeField attributeField = tableField.getAttributeFieldMap().get(colName);
                if (!attributeField.getInternal()) {
                    buf.append(ident + "    " + "<" + colName + ">");
                    if (attributeField instanceof ColumnAttributeField) {
                        AttributeFieldValue fieldValue = (AttributeFieldValue) rowMap.get(colName);
                        buf.append(fieldValue.getValue());
                    } else if (attributeField instanceof LinkAttributeField) {
                        LinkValue fieldValue = (LinkValue) rowMap.get(colName);
                        buf.append(fieldValue.getUrl());
                    } else{
                        buf.append(rowMap.get(colName));
                    }
                    buf.append("</" + colName + ">" + newline);
                }
            }
            buf.append(ident + "</" + rowTag + ">" + newline);
        }
        resultList.close();
    }

    public class TableFieldValueIterator implements Iterator {

        Iterator<Map<String, Object>> resultListRows;

        TableFieldValueIterator(Iterator<Map<String, Object>> resultListRows) {
            this.resultListRows = resultListRows;
        }

        public boolean hasNext() {
            // ask rl if a next thing is available
            return resultListRows.hasNext();
        }

        public  Map<String, Object> next() throws NoSuchElementException {
            Map<String, Object> resultListRow = resultListRows.next();
            Set<String> columnNames = resultListRow.keySet();
            // the map contains <attributeFieldName, AttributeFieldValue> tuples
            Map<String, Object> fieldRow = new LinkedHashMap<String, Object>();
            
            AttributeField[] attributeFields = tableField.getAttributeFields();
            try {
                for (AttributeField attributeField : attributeFields) {
                    String name = attributeField.getName();
                    Object fieldValue = null;
                    if (attributeField instanceof ColumnAttributeField) {
                        // create an AttributeFieldValue
                        fieldValue = new AttributeFieldValue(attributeField,
                                resultListRow.get(name));
                    } else if (attributeField instanceof LinkAttributeField) {
                        LinkAttributeField linkField = (LinkAttributeField) attributeField;
                        // resolve the *macro* in the URL/visual string
                        String instantiatedVisible = instantiateText(
                                linkField.getVisible(), resultListRow,
                                columnNames);
                        String instantiatedUrl = instantiateText(
                                linkField.getUrl(), resultListRow, columnNames);
                        fieldValue = new LinkValue(instantiatedVisible,
                                instantiatedUrl);
                    } else if (attributeField instanceof TextAttributeField) {
                        TextAttributeField textField = (TextAttributeField) attributeField;
                        // resolve the macro too
                        fieldValue = instantiateText(textField.getText(),
                                resultListRow, columnNames);
                    }
                    fieldRow.put(name, fieldValue);
                }
                return fieldRow;
            } catch (WdkModelException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

