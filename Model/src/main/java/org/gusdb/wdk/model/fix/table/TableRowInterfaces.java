package org.gusdb.wdk.model.fix.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkModel;

/**
 * Defines interfaces used by the TableRowUpdater and plugins to it
 *
 * @author rdoherty
 */
public class TableRowInterfaces {

  /**
   * Plugin interface for TableRowUpdater.  An updater plugin must choose:
   *
   * 1. A subclass of TableRow.  Each TableRow instance represents a record in a DB table
   * 2. A subclass of TableRowFactory, which provides an interface between TableRows and the DB table they represent
   *
   * The plugin then needs only implement the actual update logic (in Java code) on a particular row.  The
   * limitation is that rows are considered one at a time unless additional queries are done inside the
   * plugin itself.  This can be dangerous since the table may be partially updated when those queries are run.
   *
   * @param <T> Type of table row this plugin supports
   */
  public interface TableRowUpdaterPlugin<T extends TableRow> {

    /**
     * Called by TableRowUpdater's main() method.  Allows configuration of plugin via
     * WdkModel and command line arguments.
     *
     * @param wdkModel WDK model created from the project ID and gus home
     * @param additionalArgs additional arguments meant for this plugin
     */
    void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception;

    /**
     * Return an instance of TableRowUpdater parameterized with the TableRow subclass
     * type supported by this plugin
     *
     * @param wdkModel
     * @return
     */
    TableRowUpdater<T> getTableRowUpdater(WdkModel wdkModel);

    /**
     * Processes a single record (a row in the DB table), updating the record in place and telling the
     * updater whether the record should be written.  This method must be thread-safe as multiple threads may
     * be calling it synchronously with different row objects.
     *
     * @param nextRow object representing a row in the DB
     * @return result of processing- essentially a tuple of <whether object should be written, updated object>
     * @throws Exception if error occurs while processing row
     */
    RowResult<T> processRecord(T nextRow) throws Exception;

    /**
     * Some plugins may choose to collect statistics as they process records.  This method will be called
     * when all processing is complete to allow the plugin to dump those statistics.
     */
    void dumpStatistics();
  }

  /**
   * Provides an interface to a set of records (usually a single table).
   *
   * @param <T> type of TableRow this factory produces and consumes
   */
  public interface TableRowFactory<T extends TableRow> {

    /**
     * Do any pre-run setup required by this TableRowFactory
     *
     * @param wdkModel WDK model that will be used in this run
     */
    void setUp(WdkModel wdkModel) throws Exception;

    /**
     * Do any post-run tear down required by this TableRowFactory
     *
     * @param wdkModel WDK model that will be used in this run
     */
    void tearDown(WdkModel wdkModel) throws Exception;

    /**
     * Provides SQL query that will be called to fetch the record set the plugin will process
     *
     * @param schema schema of this DB
     * @param projectId project ID of the model being used to process the records
     * @return SQL query as string
     */
    String getRecordsSql(String schema, String projectId);

    /**
     * Creates an instance of an object representing a single record from the ResultSet.  This method
     * should probably not call next() on the passed ResultSet as it will already have been called.
     *
     * @param rs ResultSet queued to next record (this method need not call next())
     * @param platform DB platform query was performed against
     * @return instance of TableRow representing the next record read from the result set
     * @throws SQLException if something goes wrong reading the row of data
     */
    T newTableRow(ResultSet rs, DBPlatform platform) throws SQLException;

  }

  public interface TableRowWriter<T extends TableRow> {

    /**
     * Do any pre-run setup required by this TableRowFactory
     *
     * @param wdkModel WDK model that will be used in this run
     */
    void setUp(WdkModel wdkModel) throws Exception;

    /**
     * Do any post-run tear down required by this TableRowFactory
     *
     * @param wdkModel WDK model that will be used in this run
     */
    void tearDown(WdkModel wdkModel) throws Exception;

    /**
     * Provides parameterized SQL to write a single record.  This SQL will be converted to a
     * PreparedStatement, then executed as a batch update operation using the SQL types and values
     * provided by getParameterTypes() and toValues()
     *
     * @param schema schema of this DB
     * @return SQL update statement as string
     */
    String getWriteSql(String schema);

    /**
     * Provides the JDBC SQL types of the parameters passed to the update statement provided by
     * getUpdateRecordSql().  They should match the SQL and the values returned by toUpdateVals()
     *
     * @return array of SQL type integer constants corresponding to the params in the update SQL
     */
    Integer[] getParameterTypes();

    /**
     * Converts a TableRow object to a collection of value arrays representing the passed object.  Each
     * of the returned arrays will be applied to the write operation defined by the SQL above.
     *
     * @param obj instance of Java row object
     * @return collection of value arrays representing the passed object
     */
    Collection<Object[]> toValues(T obj);

  }

  /**
   * Basic interface for a class representing a record in a DB table
   */
  public interface TableRow {

    /**
     * Provide human-readable ID for this particular record.  Used to provide a reference value if an update
     * to a particular record fails, and other logging.
     *
     * @return human-readable ID for this record
     */
    String getDisplayId();

  }

  /**
   * Represents the result of an update performed on an object representing a single record.  Provides a
   * boolean value telling whether the value should be written back to the database), and the record itself.
   * If shouldWrite value is false, the record can be null with no ill effects.
   *
   * @param <T> type of record this result is for
   */
  public static class RowResult<T> extends TwoTuple<Boolean, T> {

    /**
     * Creates a result for the passed record with initial write value 'false'
     *
     * @param record record this result is for
     */
    public RowResult(T record) {
      super(false, record);
    }

    /**
     * @return whether the updater plugin has determined that this record should be written using the
     * configured writers
     */
    public boolean shouldWrite() { return getFirst(); }

    /**
     * Sets shouldWrite value of this result to the passed value
     */
    public RowResult<T> setShouldWrite(boolean shouldWrite) { set(shouldWrite, getSecond()); return this; }

    /**
     * @return the (possibly modified) record this result is for
     */
    public T getRow() { return getSecond(); }
  }

}
