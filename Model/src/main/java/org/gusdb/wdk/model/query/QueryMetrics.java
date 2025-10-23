package org.gusdb.wdk.model.query;

import java.util.Optional;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleIntResultSetHandler;
import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;
import org.gusdb.wdk.model.WdkModelException;

import io.prometheus.client.Histogram;

/**
 * Provides standard query metrics (reported via a prometheus metrics endpoint) for
 * all queries run through the QueryInstance class.  These metrics include:
 *
 * - project ID
 * - query full name
 * - query duration (time to insert into WDK cache, binned)
 * - query size (number of rows inserted to cache, binned)
 *
 */
public class QueryMetrics {

  // represent the upper bound of duration bins in milliseconds
  private static final double[] QUERY_DURATION_MS_BINS = new double[] {
      250, 1000, 3000, 10000, 25000, 60000, 120000, 300000, Double.POSITIVE_INFINITY
  };

  private static final Histogram QUERY_DURATION = Histogram.build()
      .name("wdk_query_duration")
      .help("WDK query duration in milliseconds")
      .labelNames("project_id", "query_name", "result_size_magnitude")
      .buckets(QUERY_DURATION_MS_BINS)
      .register();

  public static Optional<String> observeCacheInsertion(
      Query query,
      String cacheSchema,
      String cacheTableName,
      SupplierWithException<Optional<String>> cacheCreator) throws WdkModelException {
    try {

      // time creation and population of the cache table
      long timerStart = System.currentTimeMillis();
      Optional<String> resultMessage = cacheCreator.get();
      long duration = System.currentTimeMillis() - timerStart;

      // get number of inserted rows
      int resultSize = new SQLRunner(
          query.getWdkModel().getAppDb().getDataSource(),
          "select count(*) from " + cacheSchema + cacheTableName,
          "table_count_for_metrics"
      ).executeQuery(new SingleIntResultSetHandler());

      // register an observation in the histogram
      QUERY_DURATION
        .labels(
          query.getWdkModel().getProjectId(),
          query.getFullName(),
          String.valueOf(orderOfMagnitude(resultSize)))
        .observe(duration);

      // return the message associated with this query execution
      return resultMessage;
    }
    catch (WdkModelException | RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      // should not happen; this is an API problem
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a logarithm-like value to show order of magnitude of a result size.  If size is
   * zero, returns zero; else returns the floor(log10(resultSize)) + 1, yielding results like:
   *
   * f(0) = 0
   * f(3) = 1
   * f(12) = 2
   * f(463) = 3
   *
   * This could also be done by converting the int to a String and returning length; but numeric is probably faster.
   *
   * @param resultSize
   * @return
   */
  private static int orderOfMagnitude(int resultSize) {
    return resultSize == 0 ? 0 :
      Double.valueOf(Math.floor(Math.log10(Integer.valueOf(resultSize).doubleValue()))).intValue() + 1;
  }
}
