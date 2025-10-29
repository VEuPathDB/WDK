package org.gusdb.wdk.model.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.ArrayUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wsf.client.ClientModelException;
import org.gusdb.wsf.client.WsfResponseListener;

public class ProcessQueryResponseListener implements WsfResponseListener {

  private static final Logger LOG = Logger.getLogger(ProcessQueryResponseListener.class);

  private final List<Column> _columns;
  private final PreparedStatement _psInsert;
  private final Integer[] _bindTypes;
  private final Map<String, String> _attachments;
  private final int _batchSize;

  private int _rowCount;
  private String _message;

  public ProcessQueryResponseListener(List<Column> columns, PreparedStatement psInsert, int batchSize) {
    _psInsert = psInsert;
    _columns = columns;
    _attachments = new LinkedHashMap<>();
    _batchSize = batchSize;

    Integer[] columnBindTypes = ResultListArgumentBatch.getBindTypes(columns);
    // add row id column
    _bindTypes = ArrayUtil.insert(columnBindTypes, 0, Types.INTEGER);
  }

  @Override
  public void onRowReceived(String[] row) throws ClientModelException {
    Object[] objects = getObjects(row);
    try {
      // add rowCount into values
      _rowCount++;
      objects = ArrayUtil.insert(objects, 0, _rowCount);

      SqlUtils.bindParamValues(_psInsert, _bindTypes, objects);
      _psInsert.addBatch();
      if (_rowCount % _batchSize == 0) {
        _psInsert.executeBatch();
        LOG.debug(_rowCount + " rows inserted.");
      }
    }
    catch (SQLException ex) {
      throw new ClientModelException(ex);
    }
  }

  /**
   * TODO - need to add assigned weight to the values when needed.
   * @param row
   * @return
   */
  private Object[] getObjects(String[] row) throws ClientModelException {
    Object[] objects = new Object[_columns.size()];
    int colIndex = 0;
    for (int i = 0; i < _columns.size(); i++) {
      Column column = _columns.get(i);
      ColumnType type = column.getType();
      String value = row[i];

      // have to move clobs to the end
      if (type == ColumnType.CLOB)
        continue;

      // truncate string value if it won't fit into column
      if (type == ColumnType.STRING && value != null && value.length() > column.getWidth()) {
        throw new ClientModelException("Actual value is too big for column [" + column.getName() + "]: " + value);
        // value = value.substring(0, column.getWidth() - 3) + "...";
      }

      if (value != null && value.isEmpty()) {
        // treat empty strings as null; this will ensure compatibility with both PG and Oracle
        value = null;
      }

      // allow null values to be directly added to result; will be stored as nulls in DB
      objects[colIndex] = (value == null) ? null : type.convertStringToTypedValue(value);
      colIndex++;
    }

    // add CLOB values last
    for (int i = 0; i < _columns.size(); i++) {
      Column column = _columns.get(i);
      if (column.getType().equals(ColumnType.CLOB)) {
        objects[colIndex] = row[i];
        colIndex++;
      }
    }
    return objects;
  }

  @Override
  public void onAttachmentReceived(String key, String content) {
    _attachments.put(key, content);
  }

  @Override
  public void onMessageReceived(String message) {
    _message = message;
  }

  public Map<String, String> getAttachments() {
    return _attachments;
  }

  public String getMessage() {
    return _message;
  }
}
