package org.gusdb.wdk.model.fix.table;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.functional.Functions.transform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.RowResult;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRow;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowFactory;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowUpdaterPlugin;

/**
 * Provides threading and database logic to update an arbitrary DB table using procedures defined in a plugin.
 * Also provides main() method as a start-point for running updates with a given plugin.
 * 
 * How does it work?
 * 1.  On command line, user provides a plugin class and projectId, plus any args for the plugin
 * 2a. The plugin class tells this class what type of data (subclass of TableRow) it will be processing, AND
 * 2b. The plugin class tells this class how to fetch and update that type of data (subclass of TableRowFactory)
 * 3.  This class runs a SQL query provided by TableRowFactory, then uses the factory to produce a queue of
 *     TableRow objects.
 * 4.  A series of threads pick up the TableRow objects from the queue and send them to the plugin for
 *     processing.
 * 5.  The plugin decides if a given record needs to be updated, makes changes in memory to the Java object,
 *     and returns a boolean telling whether the object has been updated.
 * 6.  Each thread batches up record instances that need to be written back to the DB, then uses
 *        a) Update SQL
 *        b) Update SQL parameter types
 *        c) A [ record object -> update parameter values ] converter method
 *     all of which are provided by the TableRowFactory, to perform a batch update to the DB
 * 7.  Once all records have been removed from the queue, this class tells all threads to shut down after
 *     finishing the current record.
 * 8.  Once all threads have shut down, statistics are written and the program shuts down.
 * 
 * For interfaces needed to create an update implementation, see TableRowInterfaces in this package.
 * 
 * @author rdoherty
 *
 * @param <T> type of TableRow the configured plugin will be processing
 */
public class TableRowUpdater<T extends TableRow> {

  private static final Logger LOG = Logger.getLogger(TableRowUpdater.class);

  // constants controlling behavior
  private static final int MAX_QUEUE_SIZE = 30;
  private static final int NUM_THREADS = 15;
  private static final int BATCH_COMMIT_SIZE = 100;
  private static final boolean UPDATES_DISABLED = false;

  // constants exhibiting various exit statuses
  private static enum ExitStatus {
    SUCCESS, BAD_UPDATER_ARGS, BAD_PLUGIN_ARGS, PROGRAM_ERROR, THREAD_ERROR, RECORD_ERRORS;
  }

  /**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*
   *  Main method and associated CLI processing
   **%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

  public static void main(String[] args) {
    LOG.info(TableRowUpdater.class.getSimpleName() + " started with args: " + FormatUtil.printArray(args));
    Config config = parseArgs(args);
    WdkModel wdkModel = null;
    ExitStatus exitValue = ExitStatus.SUCCESS;
    try {
      wdkModel = WdkModel.construct(config.projectId, GusHome.getGusHome());
      LOG.info("Configuring plugin " + config.plugin.getClass().getSimpleName() +
          " with args " + FormatUtil.arrayToString(config.additionalArgs.toArray()));
      if (config.plugin.configure(wdkModel, config.additionalArgs)) {
        TableRowUpdater<?> updater = config.plugin.getTableRowUpdater(wdkModel);
        exitValue = updater.run();
        config.plugin.dumpStatistics();
      }
      else {
        exitValue = ExitStatus.BAD_PLUGIN_ARGS;
      }
    }
    catch (Exception e) {
      LOG.error(e);
      exitValue = ExitStatus.PROGRAM_ERROR;
    }
    finally {
      if (wdkModel != null) wdkModel.releaseResources();
    }
    LOG.info("Exiting with status: " + exitValue.ordinal() + " (" + exitValue + ").");
    System.exit(exitValue.ordinal());
  }

  private static Config parseArgs(String[] args) {
    if (args.length < 2) {
      System.err.println(NL +
          "USAGE: TableRowUpdater <plugin_class_name> <projectId> <args_to_plugin...>" + NL + NL +
          "  plugin_class_name: Name of plugin's Java class " +
          "(must implement " + TableRowUpdaterPlugin.class.getName() + ")" + NL +
          "  projectId: Name of project in XML/config dir (e.g. PlasmoDB)" + NL +
          "  args_to_plugin: any additional arguments to be passed to your plugin via configure()" + NL);
      System.exit(ExitStatus.BAD_UPDATER_ARGS.ordinal());
    }
    Config config = new Config();
    String pluginClassName = args[0];
    config.projectId = args[1];
    config.additionalArgs = new ArrayList<>();
    for (int i = 2; i < args.length; i++) {
      config.additionalArgs.add(args[i]);
    }
    try {
      @SuppressWarnings("unchecked")
      Class<? extends TableRowUpdaterPlugin<?>> pluginClass =
          (Class<? extends TableRowUpdaterPlugin<?>>) Class.forName(pluginClassName);
      config.plugin = pluginClass.newInstance();
    }
    catch (ClassCastException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to instantiate plugin class '" + pluginClassName + "'", e);
    }
    return config;
  }

  private static class Config {
    public String projectId;
    public TableRowUpdaterPlugin<?> plugin;
    public List<String> additionalArgs;
  }

  /**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*
   *  Instance fields and methods for TableRowUpdater class
   **%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
  
  private final TableRowFactory<T> _factory;
  private final TableRowUpdaterPlugin<T> _plugin;
  private final WdkModel _wdkModel;
  private final ExecutorService _exec = Executors.newFixedThreadPool(NUM_THREADS);

  public TableRowUpdater(TableRowFactory<T> factory, TableRowUpdaterPlugin<T> plugin, WdkModel wdkModel) {
    _factory = factory;
    _plugin = plugin;
    _wdkModel = wdkModel;
  }

  private ExitStatus run() {
    Timer timer = Timer.start();
    List<RowHandler<T>> threads = new ArrayList<>();
    List<Future<Stats>> results = new ArrayList<>();
    Stats aggregate = new Stats();
    int numThreadProblems = 0;
    try {
      DatabaseInstance userDb = _wdkModel.getUserDb();
      RecordQueue<T> recordQueue = new RecordQueue<>();
  
      // create and start threads that will listen to queue and pull off records to process
      for (int i = 0; i < NUM_THREADS; i++) {
        RowHandler<T> thread = new RowHandler<>(i + 1, recordQueue, _plugin, _factory, _wdkModel);
        threads.add(thread);
        results.add(_exec.submit(thread));
      }
      _exec.shutdown();
  
      // execute query to read all records from DB and submit them to handler threads
      new SQLRunner(userDb.getDataSource(), _factory.getRecordsSql(getUserSchema(_wdkModel), _wdkModel.getProjectId()))
          .executeQuery(new RecordQueuer<T>(_wdkModel, _factory, recordQueue));
  
      // wait for queue to empty
      while (!recordQueue.isEmpty()) { /* wait */ }

    }
    finally {
      if (!threads.isEmpty()) {

        // tell threads to commit their changes even if batches not full
        for (RowHandler<T> thread : threads) {
          thread.commitAndFinish();
        }
    
        // wait for threads to finish
        while (!allThreadsFinished(threads)) { /* wait */ }

        // collect stats and display
        for (Future<Stats> result : results) {
          try {
            aggregate.incorporate(result.get());
          }
          catch (ExecutionException | InterruptedException e) {
            numThreadProblems++;
          }
        }
        LOG.info(numThreadProblems + " threads exited abnormally.");
        LOG.info("Duration: " + timer.getElapsedAsString());
        LOG.info("Aggregate results: " + aggregate);
      }
    }
    if (numThreadProblems > 0) return ExitStatus.THREAD_ERROR;
    if (aggregate.numRecordErrors > 0) return ExitStatus.RECORD_ERRORS;
    return ExitStatus.SUCCESS;
  }

  private boolean allThreadsFinished(List<RowHandler<T>> threads) {
    for (RowHandler<T> thread : threads) {
      if (!thread.isFinished()) {
        return false;
      }
    }
    return true;
  }

  private static String getUserSchema(WdkModel wdkModel) {
    return wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  /**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*
   *  Static helper inner classes 
   **%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

  /**
   * Provides a slightly clearer interface over a ConcurrentLinkedDeque, where records are pushed to the
   * tail of the queue and popped from the head (i.e. a queue).  If queue is empty, a null value is returned.
   * 
   * @param <T> type of objects stored in this queue
   */
  @SuppressWarnings("serial")
  private static class RecordQueue<T> extends ConcurrentLinkedDeque<T> {
    public void pushRecord(T row) {
      add(row);
    }
    public T popRecord() {
      return pollFirst();
    }
  }

  /**
   * Provides a ResultSetHandler that appends records read from the result set onto a record queue, but only
   * while the queue is smaller than a maximum size.  This handler will then wait until the queue has
   * emptied before reading further records.
   * 
   * @param <T> type of record that will be created from each row in the ResultSet (and subsequently added to the queue)
   */
  private static class RecordQueuer<T extends TableRow> implements ResultSetHandler {

    private final WdkModel _wdkModel;
    private final TableRowFactory<T> _factory;
    private final RecordQueue<T> _recordQueue;

    public RecordQueuer(WdkModel wdkModel, TableRowFactory<T> factory, RecordQueue<T> recordQueue) {
      _wdkModel = wdkModel;
      _factory = factory;
      _recordQueue = recordQueue;
    }

    @Override
    public void handleResult(ResultSet rs) throws SQLException {
      DBPlatform platform = _wdkModel.getUserDb().getPlatform();
      while (rs.next()) {
        while (_recordQueue.size() >= MAX_QUEUE_SIZE) { /* wait */ }
        _recordQueue.pushRecord(_factory.newTableRow(rs, platform));
      }
    }
  }

  /**
   * Container for statistics gathered about counts of records in various states
   */
  private static class Stats {

    public int numProcessed = 0;
    public int numModified = 0;
    public int numRecordErrors = 0;

    /**
     * Incorporates the stats contained in the passed object into this one.  Used to aggregate results.
     * 
     * @param stats object to incorporate into this stats object
     */
    public void incorporate(Stats stats) {
      numProcessed += stats.numProcessed;
      numModified += stats.numModified;
      numRecordErrors += stats.numRecordErrors;
    }

    @Override
    public String toString() {
      return "Processed " + numProcessed + " total records" +
          " (" + numModified + " modified, " + numRecordErrors + " errors)";
    }
  }

  /**
   * Callable class that continually pops records off a record queue and processes them via the plugin.
   * Modified records are collected into a batch, then written to the DB together in a single transaction
   * When the commitAndFinish() method is called, it will finish the current record and then exit its run
   * loop, print statistics, and return its stats (available in a Future for this Callable).
   * 
   * @param <T> type of records being processed
   */
  private static class RowHandler<T extends TableRow> implements Callable<Stats> {

    private final int _threadId;
    private final RecordQueue<T> _recordQueue;
    private final TableRowUpdaterPlugin<T> _plugin;
    private final BatchUpdater<T> _batchUpdater;

    private final AtomicBoolean _isFinished = new AtomicBoolean(false);
    private final AtomicBoolean _commitAndFinishFlag = new AtomicBoolean(false);

    public RowHandler(int threadId, RecordQueue<T> recordQueue, TableRowUpdaterPlugin<T> plugin, TableRowFactory<T> factory, WdkModel wdkModel) {
      _threadId = threadId;
      _recordQueue = recordQueue;
      _plugin = plugin;
      _batchUpdater = new BatchUpdater<T>(this, factory, wdkModel);
    }

    public void commitAndFinish() {
      _commitAndFinishFlag.set(true);
    }

    public boolean isFinished() {
      return _isFinished.get();
    }

    private void log(String message) {
      LOG.info("Thread " + _threadId + ": " + message);
    }

    private void error(String message, Exception e) {
      if (e == null) LOG.error("Thread " + _threadId + ": " + message);
      else LOG.error("Thread " + _threadId + ": " + message, e);
    }

    @Override
    public Stats call() {
      try {
        log("Ready");
        Stats stats = new Stats();
        List<T> modifiedRecords = new ArrayList<>();
        while (!_commitAndFinishFlag.get()) {
          T nextRecord = _recordQueue.popRecord();
          if (nextRecord != null) {
            stats.numProcessed++;
            RowResult<T> result = null;
            try {
              result = _plugin.processRecord(nextRecord);
            }
            catch (Exception e) {
              error("Exception processing record " + nextRecord.getDisplayId(), e);
              stats.numRecordErrors++;
            }
            if (result != null) {
              if (result.isModified()) {
                // record has been modified
                modifiedRecords.add(result.getRow());
                stats.numModified++;
              }
              if (modifiedRecords.size() >= BATCH_COMMIT_SIZE) {
                _batchUpdater.update(modifiedRecords);
                modifiedRecords.clear();
              }
            }
            if (stats.numProcessed % 1000 == 0) {
              log(stats.toString());
            }
          }
        }
        _batchUpdater.update(modifiedRecords);
        log("Shutting down. " + stats.toString());
        return stats;
      }
      catch (Exception e) {
        error("Ended unexpectedly", e);
        throw e;
      }
      finally {
        _isFinished.set(true);
      }
    }
  }

  /**
   * Handles writing a batch of modified records
   * 
   * @param <T> type of row being update
   */
  private static class BatchUpdater<T extends TableRow> {

    private final RowHandler<T> _parent;
    private final TableRowFactory<T> _factory;
    private final Integer[] _updateTypes;
    private final DataSource _userDs;
    private final String _schema;
    
    public BatchUpdater(RowHandler<T> parent, TableRowFactory<T> factory, WdkModel wdkModel) {
      _parent = parent;
      _factory = factory;
      _updateTypes = factory.getUpdateParameterTypes();
      _userDs = wdkModel.getUserDb().getDataSource();
      _schema = getUserSchema(wdkModel);
    }

    public void update(final List<T> modifiedRows) {

      // need to construct an argument batch around modified record list
      ArgumentBatch batch = getArgumentBatch(modifiedRows);

      // log what we are about to do
      _parent.log("Preparing to write " + modifiedRows.size() + " records to DB. " +
          "First record: " + (modifiedRows.isEmpty() ? "<none>" : modifiedRows.get(0).getDisplayId()));

      // if updates enabled, execute argument batch
      if (!UPDATES_DISABLED) {
        new SQLRunner(_userDs, _factory.getUpdateRecordSql(
            _schema), true).executeUpdateBatch(batch);
      }
    }

    private ArgumentBatch getArgumentBatch(final List<T> modifiedRows) {
      return new ArgumentBatch() {

        @Override
        public Iterator<Object[]> iterator() {
          return transform(modifiedRows.iterator(),
              new Function<T, Object[]>() {
                @Override public Object[] apply(T obj) {
                  return _factory.toUpdateVals(obj);
                }
              });
        }

        @Override
        public int getBatchSize() {
          return BATCH_COMMIT_SIZE;
        }

        @Override
        public Integer[] getParameterTypes() {
          return _updateTypes;
        }
      };
    }
  }
}
