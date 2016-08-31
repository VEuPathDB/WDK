package org.gusdb.wdk.model.fix.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkModel;

public class TableRowInterfaces {

  public static interface TableRowUpdaterPlugin<T extends TableRow> {

    public boolean configure(WdkModel wdkModel, List<String> additionalArgs);

    public TableRowUpdater<T> getTableRowUpdater(WdkModel wdkModel);

    public RowResult<T> processRecord(T nextRow) throws Exception;

    public void dumpStatistics();
  }

  public static interface TableRowFactory<T extends TableRow> {

    public String getRecordsSql(String schema, String projectId);

    public T newTableRow(ResultSet rs, DBPlatform platform) throws SQLException;

    public String getUpdateRecordSql(String schema);

    public Integer[] getUpdateParameterTypes();

    public Object[] toUpdateVals(T obj);

  }

  public static interface TableRow {

    public String getDisplayId();

  }

  public static class RowResult<T> extends TwoTuple<Boolean, T> {

    public RowResult(Boolean isModified, T record) {
      super(isModified, record);
    }

    public boolean isModified() { return getFirst(); }
    public void setModified() { set(true, getSecond()); }
    public T getTableRow() { return getSecond(); }
  }

}
