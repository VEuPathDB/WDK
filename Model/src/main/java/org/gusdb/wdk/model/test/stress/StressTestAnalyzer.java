package org.gusdb.wdk.model.test.stress;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.BiFunctionWithException;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.test.CommandHelper;
import org.gusdb.wdk.model.test.stress.StressTestTask.ResultType;

/**
 * @author Jerric
 */
public class StressTestAnalyzer {

    private static final Logger logger = Logger.getLogger(StressTestAnalyzer.class);

    private long testTag;
    private DataSource dataSource;

    public StressTestAnalyzer(long testTag, WdkModel wdkModel) {
        this.testTag = testTag;
        dataSource = wdkModel.getAppDb().getDataSource();
    }

    public long getTaskCount() throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count");
    }

    public long getTaskCount(String taskType) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND task_type = '" + taskType + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public long getSucceededTaskCount() throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND result_type = '" + ResultType.Succeeded.name() + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public long getSucceededTaskCount(String taskType) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND result_type = '" + ResultType.Succeeded.name() + "'");
        sb.append(" AND task_type = '" + taskType + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public long getFailedTaskCount() throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND result_type != '" + ResultType.Succeeded.name() + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public long getFailedTaskCount(String taskType) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND result_type != '" + ResultType.Succeeded.name() + "'");
        sb.append(" AND task_type = '" + taskType + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public long getTaskCount(ResultType resultType) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND result_type = '" + resultType.name() + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public long getTaskCount(ResultType resultType, String taskType)
            throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM " + StressTester.TABLE_STRESS_RESULT);
        sb.append(" WHERE test_tag = " + testTag);
        sb.append(" AND result_type = '" + resultType.name() + "'");
        sb.append(" AND task_type = '" + taskType + "'");
        return (Long) SqlUtils.executeScalar(dataSource,
                sb.toString(), "wdk-stress-result-count-by-type");
    }

    public float getTaskSuccessRatio() throws SQLException {
        long total = getTaskCount();
        long succeeded = getSucceededTaskCount();
        return ((float) succeeded / total);
    }

    public float getTaskSuccessRatio(String taskType) throws SQLException {
        long total = getTaskCount(taskType);
        long succeeded = getSucceededTaskCount(taskType);
        return (total == 0) ? 0 : ((float) succeeded / total);
    }

    private float getDurationValue(String sql, String sqlName, BiFunctionWithException<ResultSet,Integer,Number> valueExtractor) {
      return new SQLRunner(dataSource, sql, sqlName).executeQuery(rs -> {
        try {
          if (rs.next()) {
            Number average = valueExtractor.apply(rs, 1);
            return ((float)average / 1000F);
          }
          else {
            throw new SQLRunnerException("SQL did not return a single row: " + sql);
          }
        }
        catch (Exception e) {
          throw new SQLRunnerException("Could not get time duration value from sql: " + sql, e);
        }
      });
    }

    public float getTotalResponseTime() {
        String sql = new StringBuilder()
            .append("SELECT sum(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ")
            .append(testTag)
            .toString();
        return getDurationValue(sql, "wdk-stress-response-time", ResultSet::getLong);
    }

    public float getTotalResponseTime(String taskType) {
        String sql = new StringBuilder()
            .append("SELECT sum(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ").append(testTag)
            .append(" AND task_type = '").append(taskType).append("'")
            .toString();
        return getDurationValue(sql, "wdk-stress-response-time-by-type", ResultSet::getLong);
    }

    public float getAverageResponseTime() {
        String sql = new StringBuilder()
            .append("SELECT avg(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ").append(testTag)
            .toString();
        return getDurationValue(sql, "wdk-stress-response", ResultSet::getFloat);
    }

    public float getAverageResponseTime(String taskType) {
        String sql = new StringBuilder()
          .append("SELECT avg(end_time - start_time) FROM ")
          .append(StressTester.TABLE_STRESS_RESULT)
          .append(" WHERE test_tag = ").append(testTag)
          .append(" AND task_type = '").append(taskType).append("'")
          .toString();
        return getDurationValue(sql, "wdk-stress-response-time-by-type", ResultSet::getFloat);
    }

    public float getTotalSucceededResponseTime() {
        String sql = new StringBuilder()
            .append("SELECT sum(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ").append(testTag)
            .append(" AND result_type = '").append(ResultType.Succeeded.name()).append("'")
            .toString();
        return getDurationValue(sql, "wdk-stress-response-time-by-type", ResultSet::getLong);
    }

    public float getTotalSucceededResponseTime(String taskType) {
        String sql = new StringBuilder()
            .append("SELECT sum(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ").append(testTag)
            .append(" AND result_type = '").append(ResultType.Succeeded.name()).append("'")
            .append(" AND task_type = '").append(taskType).append("'")
            .toString();
        return getDurationValue(sql, "wdk-stress-response-time-by-type", ResultSet::getLong);
    }

    public float getAverageSucceededResponseTime() {
        String sql = new StringBuilder()
            .append("SELECT avg(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ").append(testTag)
            .append(" AND result_type = '").append(ResultType.Succeeded.name()).append("'")
            .toString();
        return getDurationValue(sql, "wdk-stress-response-time-by-type", ResultSet::getFloat);
    }

    public float getAverageSucceededResponseTime(String taskType) {
        String sql = new StringBuilder()
            .append("SELECT avg(end_time - start_time) FROM ")
            .append(StressTester.TABLE_STRESS_RESULT)
            .append(" WHERE test_tag = ").append(testTag)
            .append(" AND result_type = '").append(ResultType.Succeeded.name()).append("'")
            .append(" AND task_type = '").append(taskType).append("'")
            .toString();
        return getDurationValue(sql, "wdk-stress-response-time-by-type", ResultSet::getFloat);
    }

    public void print() throws SQLException {
        // print out results

        // print out all types results
        System.out.println("------------------ All Types --------------------");
        System.out.println("# of Total Tasks: " + getTaskCount());
        System.out.print("# of Succeeded Tasks: " + getSucceededTaskCount());
        System.out.println("\t (" + (getTaskSuccessRatio() * 100) + "%)");
        System.out.println("# of Failed Tasks: " + getFailedTaskCount());
        System.out.println("# of IO Connection Error: "
                + getTaskCount(ResultType.ConnectionError));
        System.out.println("# of HTTP Error: "
                + getTaskCount(ResultType.HttpError));
        System.out.println("# of Application Error: "
                + getTaskCount(ResultType.ApplicationException));
        System.out.print("Response Time - Total: " + getTotalResponseTime());
        System.out.println("\tAverage: " + getAverageResponseTime());
        System.out.print("SucceededResponse Time - Total: "
                + getTotalSucceededResponseTime());
        System.out.println("\tAverage: " + getAverageSucceededResponseTime());

        // print out specific types results
        String[] types = { StressTester.TYPE_HOME_URL,
                StressTester.TYPE_QUESTION_URL,
                StressTester.TYPE_XML_QUESTION_URL,
                StressTester.TYPE_RECORD_URL };
        for (String type : types) {
            System.out.println();
            System.out.println("------------------ Type: " + type
                    + " --------------------");
            System.out.println("# of Total Tasks: " + getTaskCount(type));
            System.out.print("# of Succeeded Tasks: "
                    + getSucceededTaskCount(type));
            System.out.println("\t (" + (getTaskSuccessRatio(type) * 100)
                    + "%)");
            System.out.println("# of Failed Tasks: " + getFailedTaskCount(type));
            System.out.println("# of Connection Error: "
                    + getTaskCount(ResultType.ConnectionError, type));
            System.out.println("# of HTTP Error: "
                    + getTaskCount(ResultType.HttpError, type));
            System.out.println("# of Application Error: "
                    + getTaskCount(ResultType.ApplicationException, type));
            System.out.print("Response Time - Total: "
                    + getTotalResponseTime(type));
            System.out.println("\tAverage: " + getAverageResponseTime(type));
            System.out.print("SucceededResponse Time - Total: "
                    + getTotalSucceededResponseTime(type));
            System.out.println("\tAverage: "
                    + getAverageSucceededResponseTime(type));
        }
    }

    private static Options declareOptions() {
        String[] names = { "model", "tag" };
        String[] descs = {
                "the name of the model.  This is used to find the Model XML "
                        + "file ($GUS_HOME/config/model_name.xml) the Model "
                        + "property file ($GUS_HOME/config/model_name.prop) "
                        + "and the Model config file "
                        + "($GUS_HOME/config/model_name-config.xml)",
                "The test tag of the stress test to be analyzed." };
        boolean[] required = { true, true };
        int[] args = { 0, 0 };

        return CommandHelper.declareOptions(names, descs, required, args);
    }

    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper.parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");
        String testTagStr = cmdLine.getOptionValue("tag");
        long testTag = Long.parseLong(testTagStr);

        // load WdkModel
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        try (WdkModel wdkModel = WdkModel.construct(modelName, gusHome)) {
          // create tester
          logger.info("Initializing stress test analyzer on " + modelName + " with test_tag=" + testTag);
          StressTestAnalyzer analyzer = new StressTestAnalyzer(testTag, wdkModel);
          // run tester
          analyzer.print();
        }
    }
}
