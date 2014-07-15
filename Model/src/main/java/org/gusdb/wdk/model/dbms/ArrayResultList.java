package org.gusdb.wdk.model.dbms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.client.WsfResponseListener;
import org.gusdb.wsf.common.WsfException;

/**
 * @author Jerric Gao
 * 
 */
public class ArrayResultList implements ResultList, WsfResponseListener {

  private final Map<String, Integer> columns;

  private final List<String[]> rows;
  private final Map<String, String> attachments;
  private String message;
  private int rowIndex;
  private boolean hasWeight;
  private int assignedWeight;

  /**
   * @param columns
   * @throws WdkModelException
   *           if the result has fewer columns than the column definition
   */
  public ArrayResultList(Map<String, Integer> columns) throws WdkModelException {
    this.columns = new LinkedHashMap<String, Integer>(columns);
    this.rows = new ArrayList<>();
    this.attachments = new LinkedHashMap<>();
    this.rowIndex = -1;
  }
  
  public String getMessage() {
    return message;
  }
  
  public Map<String, String> getAttachments() {
    return attachments;
  }

  public boolean isHasWeight() {
    return hasWeight;
  }

  public void setHasWeight(boolean hasWeight) {
    this.hasWeight = hasWeight;
  }

  public int getAssignedWeight() {
    return assignedWeight;
  }

  public void setAssignedWeight(int assignedWeight) {
    this.assignedWeight = assignedWeight;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.ResultList#close()
   */
  @Override
  public void close() {
    rows.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.ResultList#contains(java.lang.String)
   */
  @Override
  public boolean contains(String columnName) {
    if (columns.containsKey(columnName)) {
      return true;
    }
    else if (hasWeight && Utilities.COLUMN_WEIGHT.equals(columnName)) {
      return true;
    }
    else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.ResultList#get(java.lang.String)
   */
  @Override
  public Object get(String columnName) throws WdkModelException {
    if (!contains(columnName) && !Utilities.COLUMN_WEIGHT.equals(columnName)) {
      throw new WdkModelException("The column does not exist in ResultList: " + columnName);
    }
    if (!hasNext())
      throw new WdkModelException("No more rows in the resultList.");

    if (contains(columnName)) {
      int columnIndex = columns.get(columnName);
      return rows.get(rowIndex)[columnIndex];
    }
    else {
      // must be a weight column, and no value available, use assignedWeight.
      return new Integer(assignedWeight);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.ResultList#next()
   */
  @Override
  public boolean next() throws WdkModelException {
    rowIndex++;
    return hasNext();
  }

  private boolean hasNext() {
    return (rowIndex < rows.size());
  }

  @Override
  public synchronized void onRowReceived(String[] row) throws WsfException {
    rows.add(row);
  }

  @Override
  public void onAttachmentReceived(String key, String content) throws WsfException {
    attachments.put(key, content);
  }

  @Override
  public void onMessageReceived(String message) throws WsfException {
    this.message = message;
  }

  public int getSize() {
    return rows.size();
  }
}
