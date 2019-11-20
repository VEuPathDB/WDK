package org.gusdb.wdk.model.dbms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.client.WsfResponseListener;

/**
 * @author Jerric Gao
 * 
 */
public class ArrayResultList implements ResultList, WsfResponseListener {

  private final Map<String, Integer> _columns;

  private final List<String[]> _rows;
  private final Map<String, String> _attachments;
  private String _message;
  private int _rowIndex;
  private boolean _hasWeight;
  private int _assignedWeight;

  /**
   * @param columns
   * @throws WdkModelException
   *           if the result has fewer columns than the column definition
   */
  public ArrayResultList(Map<String, Integer> columns) throws WdkModelException {
    _columns = new LinkedHashMap<String, Integer>(columns);
    _rows = new ArrayList<>();
    _attachments = new LinkedHashMap<>();
    _rowIndex = -1;
  }
  
  public String getMessage() {
    return _message;
  }
  
  public Map<String, String> getAttachments() {
    return _attachments;
  }

  public boolean isHasWeight() {
    return _hasWeight;
  }

  public void setHasWeight(boolean hasWeight) {
    _hasWeight = hasWeight;
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

  public void setAssignedWeight(int assignedWeight) {
    _assignedWeight = assignedWeight;
  }

  @Override
  public void close() {
    _rows.clear();
  }

  @Override
  public boolean contains(String columnName) {
    if (_columns.containsKey(columnName)) {
      return true;
    }
    else if (_hasWeight && Utilities.COLUMN_WEIGHT.equals(columnName)) {
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public Object get(String columnName) throws WdkModelException {
    if (!contains(columnName) && !Utilities.COLUMN_WEIGHT.equals(columnName)) {
      throw new WdkModelException("The column does not exist in ResultList: " + columnName);
    }
    if (!hasNext())
      throw new WdkModelException("No more rows in the resultList.");

    if (contains(columnName)) {
      int columnIndex = _columns.get(columnName);
      return _rows.get(_rowIndex)[columnIndex];
    }
    else {
      // must be a weight column, and no value available, use assignedWeight.
      return Integer.valueOf(_assignedWeight);
    }
  }

  @Override
  public boolean next() throws WdkModelException {
    _rowIndex++;
    return hasNext();
  }

  private boolean hasNext() {
    return (_rowIndex < _rows.size());
  }

  @Override
  public synchronized void onRowReceived(String[] row) {
    _rows.add(row);
  }

  @Override
  public void onAttachmentReceived(String key, String content) {
    _attachments.put(key, content);
  }

  @Override
  public void onMessageReceived(String message) {
    _message = message;
  }

  public int getSize() {
    return _rows.size();
  }
}
