package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModelText;

/**
 * Holds the SQL to augment an answer with additional rows.  If present, the cache table will have an extra dynamic column
 * named as specified.  Rows that are extra will have the specified True value set, otherwise the False value.
 * @author steve
 *
 */
public class ExtraAnswerRowsProducer {
	
	/**
	 * SQL that produces extra rows.  
	 */
	private WdkModelText sql;
	private String dynamicColumnName;
	private String dynamicColumnDisplayName;
	private String columnValueForExtraRows;
	private String columnValueForOriginalRows;
	
	public String getDynamicColumnName() {
		return dynamicColumnName;
	}

	public void setDynamicColumnName(String dynamicColumnName) {
		this.dynamicColumnName = dynamicColumnName;
	}

	public String getDynamicColumnDisplayName() {
		return dynamicColumnDisplayName;
	}

	public void setDynamicColumnDisplayName(String dynamicColumnDisplayName) {
		this.dynamicColumnDisplayName = dynamicColumnDisplayName;
	}	

	public String getSql() {
		return sql.getText();
	}
	
	public void setSql(WdkModelText sql) {
		this.sql = sql;
	}

	public String getColumnValueForExtraRows() {
		return columnValueForExtraRows;
	}

	public void setColumnValueForExtraRows(String columnValueForExtraRows) {
		this.columnValueForExtraRows = columnValueForExtraRows;
	}

	public String getColumnValueForOriginalRows() {
		return columnValueForOriginalRows;
	}

	public void setColumnValueForOriginalRows(String columnValueForOriginalRows) {
		this.columnValueForOriginalRows = columnValueForOriginalRows;
	}

	public int getExtraColumnWidth() {
		return Math.max(columnValueForOriginalRows.length(), columnValueForExtraRows.length());
	}

	

}
