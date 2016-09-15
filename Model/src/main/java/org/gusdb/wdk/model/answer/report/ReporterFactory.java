package org.gusdb.wdk.model.answer.report;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.report.Reporter;
import org.json.JSONObject;

/**
 * Provides methods to get a configured reporter using the passed AnswerValue and configuration.  Two options
 * exist for configuration: the (legacy) Map<String,String> or a JSON Object.  You may also specify start
 * and end indexes for the result if a subset of results is desired.
 * 
 * @author rdoherty
 */
public class ReporterFactory {

  public static Reporter getReporter(AnswerValue answerValue, String reporterName, Map<String, String> config)
      throws WdkModelException, WdkUserException {
    // get the full answer
    int endI = answerValue.getResultSize();
    return getReporter(answerValue, reporterName, config, 1, endI);
  }

  public static Reporter getReporter(AnswerValue answerValue, String reporterName, Map<String, String> config, int startI, int endI)
      throws WdkModelException {
    Reporter reporter = createReporterInstance(answerValue, reporterName, startI, endI);
    reporter.configure(config);
    return reporter;
  }

  public static Reporter getReporter(AnswerValue answerValue, String reporterName, JSONObject config)
      throws WdkModelException, WdkUserException {
    // get the full answer
    int endI = answerValue.getResultSize();
    return getReporter(answerValue, reporterName, config, 1, endI);
  }

  // set private until needed; currently not called from anywhere
  private static Reporter getReporter(AnswerValue answerValue, String reporterName, JSONObject config, int startI, int endI)
      throws WdkModelException {
    Reporter reporter = createReporterInstance(answerValue, reporterName, startI, endI);
    reporter.configure(config);
    return reporter;
  }

  private static Reporter createReporterInstance(AnswerValue answerValue, String reporterName, int startI, int endI)
      throws WdkModelException {
    // get Reporter
    RecordClass recordClass = answerValue.getQuestion().getRecordClass();
    Map<String, ReporterRef> rptMap = recordClass.getReporterMap();
    ReporterRef rptRef = rptMap.get(reporterName);
    if (rptRef == null)
      throw new WdkModelException("The reporter " + reporterName + " is " + "not registered for " +
          recordClass.getFullName());
    String rptImp = rptRef.getImplementation();
    if (rptImp == null)
      throw new WdkModelException("The reporter " + reporterName + " is " + "not registered for " +
          recordClass.getFullName());

    try {
      Class<?> rptClass = Class.forName(rptImp);
      Class<?>[] paramClasses = { AnswerValue.class, int.class, int.class };
      Constructor<?> constructor = rptClass.getConstructor(paramClasses);

      Object[] params = { answerValue, startI, endI };
      Reporter reporter = (Reporter) constructor.newInstance(params);
      reporter.setProperties(rptRef.getProperties());
      reporter.setWdkModel(rptRef.getWdkModel());
      return reporter;
    }
    catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException |
        IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new WdkModelException(ex);
    }
  }
}
