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

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.RowResult;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRow;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowFactory;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowUpdaterPlugin;

public class TableRowUpdater<T extends TableRow> {

  private static final Logger LOG = Logger.getLogger(TableRowUpdater.class);

  // constants controlling behavior
  private static final int MAX_QUEUE_SIZE = 30;
  private static final int NUM_THREADS = 20;
  private static final int BATCH_COMMIT_SIZE = 100;

  private static class Config {
    public String projectId;
    public TableRowUpdaterPlugin<?> plugin;
  }

  public static void main(String[] args) throws WdkModelException {
    Config config = parseArgs(args);
    WdkModel wdkModel = null;
    try {
      wdkModel = WdkModel.construct(config.projectId, GusHome.getGusHome());
      TableRowUpdater<?> updater = config.plugin.getTableRowUpdater(wdkModel);
      updater.run();
    }
    finally {
      if (wdkModel != null) wdkModel.releaseResources();
    }
  }

  private static Config parseArgs(String[] args) {
    if (args.length != 2) {
      System.err.println(NL +
          "USAGE: TableRowUpdater <projectId> <plugin_class_name>" + NL + NL +
          "  projectId: Name of project in XML/config dir (e.g. PlasmoDB)" + NL +
          "  plugin_class_name: Name of plugin's Java class " +
          "(must implement " + TableRowUpdaterPlugin.class.getName() + ")" + NL);
      System.exit(1);
    }
    Config config = new Config();
    config.projectId = args[0];
    String pluginClassName = args[1];
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

  private final TableRowFactory<T> _factory;
  private final TableRowUpdaterPlugin<T> _plugin;
  private final WdkModel _wdkModel;
  private final ExecutorService _exec = Executors.newFixedThreadPool(NUM_THREADS);

  public TableRowUpdater(TableRowFactory<T> factory, TableRowUpdaterPlugin<T> plugin, WdkModel wdkModel) {
    _factory = factory;
    _plugin = plugin;
    _wdkModel = wdkModel;
  }

  @SuppressWarnings("serial")
  private static class RecordQueue<T> extends ConcurrentLinkedDeque<T> {
    public void pushRow(T row) {
      add(row);
    }
    public T popRow() {
      return pollFirst();
    }
  }

  private void run() {
    long startTime = System.currentTimeMillis();
    List<RowHandler<T>> threads = new ArrayList<>();
    List<Future<Stats>> results = new ArrayList<>();
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
      new SQLRunner(userDb.getDataSource(), _factory.getAllRecordsSql(getUserSchema(_wdkModel)))
          .executeQuery(new RecordDistributor<T>(_wdkModel, _factory, recordQueue));
  
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
        Stats aggregate = new Stats();
        int numThreadProblems = 0;
        for (Future<Stats> result : results) {
          try {
            aggregate.incorporate(result.get());
          }
          catch (ExecutionException | InterruptedException e) {
            numThreadProblems++;
          }
        }
        LOG.info(numThreadProblems + " threads exited abnormally.");
        LOG.info("Duration: " + getDuration(startTime));
        LOG.info("Aggregate results: " + aggregate);
      }
    }
  }

  private String getDuration(long startTime) {
    long totalMillis = System.currentTimeMillis() - startTime;
    long millis = totalMillis % 1000;
    long totalSeconds = totalMillis / 1000;
    if (totalSeconds == 0) return totalSeconds + "." + millis;
    long seconds = totalSeconds % 60;
    long totalMinutes = totalSeconds / 60;
    if (totalMinutes == 0) return totalMinutes + ":" + seconds + "." + millis;
    long minutes = totalMinutes % 60;
    long hours = totalMinutes / 60;
    return hours + ":" + minutes + ":" + seconds + "." + millis;
  }

  private static String getUserSchema(WdkModel wdkModel) {
    return wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  private boolean allThreadsFinished(List<RowHandler<T>> threads) {
    for (RowHandler<T> thread : threads) {
      if (!thread.isFinished()) {
        return false;
      }
    }
    return true;
  }

  private static class RecordDistributor<T extends TableRow> implements ResultSetHandler {

    private final WdkModel _wdkModel;
    private final TableRowFactory<T> _factory;
    private final RecordQueue<T> _recordQueue;

    public RecordDistributor(WdkModel wdkModel, TableRowFactory<T> factory, RecordQueue<T> recordQueue) {
      _wdkModel = wdkModel;
      _factory = factory;
      _recordQueue = recordQueue;
    }

    @Override
    public void handleResult(ResultSet rs) throws SQLException {
      DBPlatform platform = _wdkModel.getUserDb().getPlatform();
      while (rs.next()) {
        while (_recordQueue.size() >= MAX_QUEUE_SIZE) { /* wait */ }
        _recordQueue.pushRow(_factory.newTableRow(rs, platform));
      }
    }
  }

  private static class Stats {

    public int numProcessed = 0;
    public int numModified = 0;
    public int numRecordErrors = 0;

    public void incorporate(Stats result) {
      numProcessed += result.numProcessed;
      numModified += result.numModified;
      numRecordErrors += result.numRecordErrors;
    }

    @Override
    public String toString() {
      return "Processed " + numProcessed + " records" +
          " (" + numModified + " modified, " + numRecordErrors + " errors)";
    }
  }

  private static class RowHandler<T extends TableRow> implements Callable<Stats> {

    private final int _threadId;
    private final RecordQueue<T> _recordQueue;
    private final TableRowUpdaterPlugin<T> _plugin;
    private final TableRowFactory<T> _factory;
    private final WdkModel _wdkModel;
    private final DatabaseInstance _userDb;

    private final AtomicBoolean _isFinished = new AtomicBoolean(false);
    private final AtomicBoolean _commitAndFinishFlag = new AtomicBoolean(false);

    public RowHandler(int threadId, RecordQueue<T> recordQueue, TableRowUpdaterPlugin<T> plugin, TableRowFactory<T> factory, WdkModel wdkModel) {
      _threadId = threadId;
      _recordQueue = recordQueue;
      _plugin = plugin;
      _factory = factory;
      _wdkModel = wdkModel;
      _userDb = _wdkModel.getUserDb();
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
      if (e == null) LOG.error(message);
      else LOG.error(message, e);
    }

    @Override
    public Stats call() {
      log("Ready");
      Stats stats = new Stats();
      List<T> modifiedRecords = new ArrayList<>();
      while (!_commitAndFinishFlag.get()) {
        T nextRecord = _recordQueue.popRow();
        if (nextRecord != null) {
          stats.numProcessed++;
          RowResult<T> result = null;
          try {
            result = _plugin.processRecord(nextRecord, _wdkModel);
          }
          catch (Exception e) {
            error("Exception processing record " + nextRecord.getDisplayId(), e);
            stats.numRecordErrors++;
          }
          if (result != null) {
            if (result.isModified()) {
              // record has been modified
              modifiedRecords.add(result.getTableRow());
              stats.numModified++;
            }
            if (modifiedRecords.size() >= BATCH_COMMIT_SIZE) {
              update(modifiedRecords);
            }
          }
          if (stats.numProcessed % 1000 == 0) {
            log(stats.toString());
          }
        }
      }
      update(modifiedRecords);
      log("Shutting down. " + stats.toString());
      _isFinished.set(true);
      return stats;
    }

    private void update(final List<T> modifiedRows) {
      // need to construct an argument batch around modified record list
      ArgumentBatch modifiedRecordBatch = new ArgumentBatch() {

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
          return _factory.getUpdateParameterTypes();
        }

      };

      new SQLRunner(_userDb.getDataSource(), _factory.getUpdateRecordSql(
          getUserSchema(_wdkModel)), true).executeUpdateBatch(modifiedRecordBatch);

      modifiedRows.clear();
    }
  }
}
