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

  private final List<Column> columns;
  private final PreparedStatement psInsert;
  private final Integer[] bindTypes;
  private final Map<String, String> attachments;
  private final int batchSize;

  private int rowCount;
  private String message;

  public ProcessQueryResponseListener(List<Column> columns, PreparedStatement psInsert, int batchSize) {
    this.psInsert = psInsert;
    this.columns = columns;
    this.attachments = new LinkedHashMap<>();
    this.batchSize = batchSize;

    Integer[] bindTypes = ResultListArgumentBatch.getBindTypes(columns);
    // add row id column
    this.bindTypes = ArrayUtil.insert(bindTypes, 0, Types.INTEGER);
  }

  @Override
  public void onRowReceived(String[] row) throws ClientModelException {
    Object[] objects = getObjects(row);
    try {
      // add rowCount into values
      rowCount++;
      objects = ArrayUtil.insert(objects, 0, rowCount);

      SqlUtils.bindParamValues(psInsert, bindTypes, objects);
      psInsert.addBatch();
      if (rowCount % batchSize == 0) {
        psInsert.executeBatch();
        LOG.debug(rowCount + " rows inserted.");
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
  private Object[] getObjects(String[] row) {
    Object[] objects = new Object[columns.size()];
    int colIndex = 0;
    for (int i = 0; i < columns.size(); i++) {
      Column column = columns.get(i);
      ColumnType type = column.getType();
      String value = row[i];

      // have to move clobs to the end
      if (type == ColumnType.CLOB)
        continue;

      // truncate string value if it won't fit into column
      if (type == ColumnType.STRING && value != null && value.length() > column.getWidth()) {
        LOG.warn("Column [" + column.getName() + "] value truncated.");
        value = value.substring(0, column.getWidth() - 3) + "...";
      }

      if (!type.equals(ColumnType.STRING) && value != null && value.isEmpty()) {
        // non-string type is being passed an empty string; treat as null
        value = null;
      }

      // allow null values to be directly added to result; will be stored as nulls in DB
      objects[colIndex] = (value == null) ? null : type.convertStringToTypedValue(value);
      colIndex++;
    }

    // add CLOB values last
    for (int i = 0; i < columns.size(); i++) {
      Column column = columns.get(i);
      if (column.getType().equals(ColumnType.CLOB)) {
        objects[colIndex] = row[i];
        colIndex++;
      }
    }
    return objects;
  }

  @Override
  public void onAttachmentReceived(String key, String content) {
    attachments.put(key, content);
  }

  @Override
  public void onMessageReceived(String message) {
    this.message = message;
  }

  public Map<String, String> getAttachments() {
    return attachments;
  }

  public String getMessage() {
    return message;
  }
}
