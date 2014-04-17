package org.gusdb.wdk.model.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.ColumnType;
import org.gusdb.wsf.util.BaseCLI;

public class TestDBManager extends BaseCLI {

  private static final String ARG_PROJECT_ID = "model";
  private static final String ARG_CREATE = "new";
  private static final String ARG_DROP = "drop";
  private static final String ARG_TABLE_DIR = "tableDir";

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    TestDBManager testDb = new TestDBManager(cmdName);
    try {
      testDb.invoke(args);
    } finally {
      System.exit(0);
    }
  }

  private WdkModel wdkModel;

  protected TestDBManager(String command) {
    super((command == null) ? "wdkTestDb" : command,
        "Create or delete test toy db");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "The ProjectId, which"
        + " should match the directory name under $GUS_HOME, where "
        + "model-config.xml is stored.");

    addNonValueOption(ARG_CREATE, false, "create new WDK ToyDB and load "
        + "test data.");
    addNonValueOption(ARG_DROP, false, "drop existing WDK ToyDB test "
        + "tables.");
    addGroup(true, ARG_CREATE, ARG_DROP);

    addSingleValueOption(ARG_TABLE_DIR, false, null, "give the absolute"
        + " path to the directory where test data are stored.");
  }

  @Override
  protected void execute() {
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);

    boolean newCache = (Boolean) getOptionValue(ARG_CREATE);
    boolean dropCache = (Boolean) getOptionValue(ARG_DROP);

    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String tableDir = (String) getOptionValue(ARG_TABLE_DIR);
    if (tableDir == null)
      tableDir = gusHome + "/data/WDKTemplateSite/Model/testTables";
    String[] tables = getTableNames(tableDir);

    try {
      // read config info
      wdkModel = WdkModel.construct(projectId, gusHome);
      DatabaseInstance appDb = wdkModel.getAppDb();

      long start = System.currentTimeMillis();
      if (newCache) createTables(appDb, tables);
      else if (dropCache) dropTables(appDb, tables);
      long end = System.currentTimeMillis();
      System.out.println("Command succeeded in " + ((end - start) / 1000.0)
          + " seconds");
    } catch (Exception e) {
      System.err.println("FAILED");
      System.err.println("");
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void dropTables(DatabaseInstance database, String[] tables)
      throws WdkModelException {
    try {
      for (int t = 0; t < tables.length; t++) {
        File nextTable = new File(tables[t]);
        if ("CVS".equals(nextTable.getName())) continue;

        String tableName = nextTable.getName();
        System.err.println("Dropping table " + tableName);
        String dropTable = "drop table " + tableName;
        SqlUtils.executeUpdate(database.getDataSource(), dropTable,
            "wdk-drop-test-table");
      }
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  private void createTables(DatabaseInstance database, String[] tables)
      throws Exception {
    for (int t = 0; t < tables.length; t++) {
      File nextTable = new File(tables[t]);
      if (!nextTable.isFile()) continue;

      String tableName = nextTable.getName();
      if ("CVS".equals(tableName) || tableName.startsWith(".")) continue;

      BufferedReader reader = new BufferedReader(new FileReader(nextTable));
      try {
        String firstLine = reader.readLine();
        if (firstLine == null)
          throw new WdkModelException("File should not be empty:"
              + nextTable.getAbsolutePath());

        Map<String, String> columnTypes = createTable(tableName, firstLine,
            database);
        Map<Integer, String> columnIds = new LinkedHashMap<Integer, String>();

        // prepare the statement
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(tableName).append(" (");
        StringBuffer sqlPiece = new StringBuffer();
        int columnId = 0;
        for (String column : columnTypes.keySet()) {
          if (sqlPiece.length() > 0) {
            sql.append(", ");
            sqlPiece.append(", ");
          }
          sql.append(column);
          sqlPiece.append("?");

          columnIds.put(columnId++, column);
        }
        sql.append(") VALUES (").append(sqlPiece).append(")");
        PreparedStatement ps = SqlUtils.getPreparedStatement(
            database.getDataSource(), sql.toString());

        System.err.println("Loading table " + tableName
            + " to database from file\n");
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
          String[] parts = nextLine.split("\t", columnIds.size());
          for (int i = 0; i < parts.length; i++) {
            String nextValue = parts[i];
            String columnName = columnIds.get(i);
            String typeString = columnTypes.get(columnName);
            ColumnType type = ColumnType.parse(typeString);

            if (nextValue.trim().equals("") && !type.isText()) nextValue = "0";

            if (!type.isText()) {
              ps.setObject(i + 1, Integer.parseInt(nextValue));
            } else {
              ps.setObject(i + 1, nextValue);
            }
          }
          ps.executeUpdate();
        }
        SqlUtils.closeStatement(ps);
      } catch (SQLException ex) {
        System.err.println("Create table " + tableName + " failed");
        ex.printStackTrace();
      } finally {
        reader.close();
      }
    }
  }

  private String[] getTableNames(String tableDir) {
    File dir = new File(tableDir);
    File[] files = dir.listFiles();
    String[] tables = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      tables[i] = files[i].getAbsolutePath();
    }
    return tables;
  }

  private Map<String, String> createTable(String tableName, String firstLine,
      DatabaseInstance database) throws Exception {
    DBPlatform platform = database.getPlatform();
    DataSource dataSource = database.getDataSource();

    StringBuffer sql = new StringBuffer("CREATE TABLE ");
    sql.append(tableName).append(" (");

    // parse the first line, which holds the column definition
    String[] columns = firstLine.split(",");
    Map<String, String> columnTypes = new LinkedHashMap<String, String>();
    boolean firstColumn = true;
    for (String column : columns) {
      if (firstColumn) firstColumn = false;
      else sql.append(", ");

      String[] pieces = column.trim().split("\\s+");
      sql.append(pieces[0]).append(" "); // column name
      String type = pieces[1].trim().toLowerCase();
      if (type.startsWith("varchar") || type.startsWith("char")) {
        int quoteStart = type.indexOf('(');
        int quoteEnd = type.indexOf(')');
        int length = Integer.parseInt(type.substring(quoteStart + 1, quoteEnd));
        sql.append(platform.getStringDataType(length));
      } else if (type.startsWith("number")) {
        int quoteStart = type.indexOf('(');
        int quoteEnd = type.indexOf(')');
        int length = Integer.parseInt(type.substring(quoteStart + 1, quoteEnd));
        sql.append(platform.getNumberDataType(length));
      } else if (type.startsWith("clob")) {
        sql.append(platform.getClobDataType());
      } else {
        sql.append(type);
      }
      for (int i = 2; i < pieces.length; i++) {
        sql.append(" ").append(pieces[i]);
      }

      // decide the type
      if (type.startsWith("number")) {
        columnTypes.put(pieces[0], ColumnType.NUMBER.getType());
      } else {
        columnTypes.put(pieces[0], ColumnType.STRING.getType());
      }
    }
    sql.append(")");

    // System.err.println("creating test table with sql " + createTable);

    SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-create-test-table");

    return columnTypes;
  }
}
