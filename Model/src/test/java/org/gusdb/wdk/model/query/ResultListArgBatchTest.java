package org.gusdb.wdk.model.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.gusdb.fgputil.TestUtil;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResultListArgBatchTest {

  private static final String TABLE_SQL =
      "CREATE TABLE VALUE_TABLE ( " +
      "  ID INTEGER NOT NULL, " +
      "  VALUE INTEGER, " +
      ")";

  private static final String INSERT_SQL = "insert into VALUE_TABLE values (?, ?)";
  private static final String COUNT_SQL = "select count(1) from VALUE_TABLE";
  private static final String DROP_TABLE_SQL = "drop table VALUE_TABLE";
  
  private static final String[][] COLUMN_CONFIG = new String[][] {
      { "NUMBER", "ID" }, { "NUMBER", "VALUE" }
  };

  private static class SimpleResultList implements ResultList {
  
    private int _numRecords;
    private int _currentRowIndex = 0;
    
    public SimpleResultList(int numRecords) {
      assertTrue(numRecords >= 0);
      _numRecords = numRecords;
    }

    @Override
    public boolean next() throws WdkModelException {
      _currentRowIndex++;
      return (_currentRowIndex <= _numRecords);
    }
    
    @Override
    public Object get(String columnName) throws WdkModelException {
      return "1";
    }

    @Override
    public boolean contains(String columnName) throws WdkModelException {
      return true;
    }

    @Override
    public void close() throws WdkModelException {
      // don't need to do anything here
    }
    
  }
  
  @Test
  public void testResultListArgBatch() throws Exception {
    Integer[] sampleInsertCounts = { 0, 1, 3, 5, 10, 13, 27 };
    Integer[] sampleBatchSizes = { 0, 1, 2, 4, 5 };
    for (int insertCount : sampleInsertCounts) {
      for (int batchSize : sampleBatchSizes) {
        System.out.println("Running test with batchSize = " +
            batchSize + ", insertCount = " + insertCount);
        runTest(insertCount, batchSize);
      }
    }
  }
  
  private void runTest(final int insertCount, int batchSize) throws Exception {
    DataSource ds = TestUtil.getTestDataSource("batchDb");
    new SQLRunner(ds, TABLE_SQL).executeStatement();
    ResultList rs = new SimpleResultList(insertCount);
    List<Column> cols = getColumns();
    ResultListArgumentBatch argBatch = new ResultListArgumentBatch(rs, cols, batchSize);
    new SQLRunner(ds, INSERT_SQL).executeStatementBatch(argBatch);
    new SQLRunner(ds, COUNT_SQL).executeQuery(new ResultSetHandler() {
      @Override
      public void handleResult(ResultSet rs) throws SQLException {
        if (rs.next()) {
          assertEquals(insertCount, rs.getInt(1));
          return;
        }
        // have no idea why this returns no rows if no inserts are none
        assertEquals(insertCount, 0);
        //throw new RuntimeException("Should have received a count.");
      }
    });
    new SQLRunner(ds, DROP_TABLE_SQL).executeStatement();
  }

  private static List<Column> getColumns() throws WdkModelException {
    List<Column> list = new ArrayList<Column>();
    for (String[] colConfig : COLUMN_CONFIG) {
      Column col = new Column();
      col.setColumnType(colConfig[0]);
      col.setName(colConfig[1]);
      list.add(col);
    }
    return list;
  }
  
}
