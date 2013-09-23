/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.ProcessResponse;
import org.gusdb.wsf.plugin.WsfServiceException;

/**
 * @author Jerric Gao
 * 
 */
public class ArrayResultList implements ResultList {

  private final Map<String, Integer> columns;
  private final ProcessResponse response;
  private String[][] result;
  private int pageIndex = 0;
  private int rowIndex = -1;

  private boolean hasWeight;
  private int assignedWeight;

  /**
   * @param columns
   * @throws WdkModelException
   *           if the result has fewer columns than the column definition
   */
  public ArrayResultList(ProcessResponse response, Map<String, Integer> columns)
      throws WdkModelException {
    this.response = response;
    this.columns = new LinkedHashMap<String, Integer>(columns);
    try {
      this.result = response.getResult(pageIndex);
    } catch (WsfServiceException ex) {
      throw new WdkModelException(ex);
    }

    // verify the columns and result
    if (result.length > 0 && result[0].length < columns.size())
      throw new WdkModelException(
          "The result has fewer columns than the column definition");
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
    // move the current page index out of the boundary
    pageIndex = response.getPageCount();
    rowIndex = -1;
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
    } else if (hasWeight && Utilities.COLUMN_WEIGHT.equals(columnName)) {
      return true;
    } else {
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
      throw new WdkModelException("The column does not exist in ResultList: "
          + columnName);
    }
    if (!hasNext())
      throw new WdkModelException("No more rows in the resultList.");

    if (contains(columnName)) {
      int columnIndex = columns.get(columnName);
      return result[rowIndex][columnIndex];
    } else {
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

    // check if we need to advance to next page
    if (rowIndex >= result.length) {
      pageIndex++;
      rowIndex = 0;
      if (pageIndex != response.getCurrentPage()) {
        try {
          result = response.getResult(pageIndex);
        } catch (WsfServiceException ex) {
          throw new WdkModelException();
        }
      }
    }
    return hasNext();
  }

  private boolean hasNext() {
    return (pageIndex < response.getPageCount() && rowIndex < result.length);
  }
}
