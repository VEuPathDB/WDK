package org.gusdb.wdk.model.query;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.dbms.ResultList;

public class ResultListArgumentBatch implements ArgumentBatch {

  private static final Logger LOG = Logger.getLogger(ResultListArgumentBatch.class);
  
  private final ResultList _resultList;
  private final Map<String,Column> _columns;
  private final int _batchSize;
  private final Integer[] _bindTypes;
  
  public ResultListArgumentBatch(ResultList resultList,
      Map<String, Column> columns, int batchSize) {
    _resultList = resultList;
    _columns = columns;
    _batchSize = batchSize;
    _bindTypes = getBindTypes(columns);
  }
        
  @Override
  public Iterator<Object[]> iterator() {

    return new Iterator<Object[]>() {
                  
      private Object[] _nextItem = null;
    
      @Override
      public boolean hasNext() {
        try {
          if (_nextItem == null) {
            // try to find next item
            boolean nextFound = _resultList.next();
            if (nextFound) {
              _nextItem = getNextRecordValues(_columns, _resultList);
            }
          }
          return (_nextItem != null);
        }
        catch (WdkModelException e) {
          throw new WdkRuntimeException("Could not get next set of insertion values.");
        }
      }
    
      @Override
      public Object[] next() {
        hasNext();
        Object[] next = _nextItem;
        _nextItem = null;
        return next;
      }
    
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Read-only iterator.");
      }
    };
  }
    
  @Override
  public int getBatchSize() {
    return _batchSize;
  }
    
  @Override
  public Integer[] getParameterTypes() {
    return _bindTypes;
  }
    
  public static Object[] getNextRecordValues(Map<String, Column> columns,
        ResultList resultList) throws WdkModelException {
    List<Object> recordValues = new ArrayList<Object>();
    for (Column column : columns.values()) {
      ColumnType type = column.getType();
    
      // have to move clobs to the end
      if (type == ColumnType.CLOB) continue;
        
      String value = (String) resultList.get(column.getName());
        
      // truncate string value if it won't fit into column
      if (type == ColumnType.STRING && value != null && value.length() > column.getWidth()) {
        LOG.warn("Column [" + column.getName() + "] value truncated.");
        value = value.substring(0, column.getWidth() - 3) + "...";
      }
        
      recordValues.add(type.convertStringToTypedValue(value));
    }
    // add CLOB values last
    for (Column column : columns.values()) {
      if (column.getType().equals(ColumnType.CLOB)) {
        recordValues.add(resultList.get(column.getName()));
      }
    }
    return recordValues.toArray();
  }
    
  public static Integer[] getBindTypes(Map<String, Column> columns) {
      List<Integer> types = new ArrayList<Integer>();
    int numClobs = 0;
    for (Column col : columns.values()) {
      if (col.getType() == ColumnType.CLOB) {
        numClobs++;
      }
      else {
        types.add(col.getType().getSqlType());
      }
    }
    for (int i=0; i < numClobs; i++) {
      types.add(Types.CLOB);
    }
    return types.toArray(new Integer[columns.size()]);
  }
}
