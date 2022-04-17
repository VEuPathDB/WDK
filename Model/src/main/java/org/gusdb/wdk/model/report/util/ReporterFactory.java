package org.gusdb.wdk.model.report.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.ReporterRef;
import org.json.JSONObject;

/**
 * Provides methods to get a configured reporter using the passed AnswerValue and configuration.  Two options
 * exist for configuration: the (legacy) Map&lt;String,String> or a JSON Object.  You may also specify start
 * and end indexes for the result if a subset of results is desired.
 * 
 * @author rdoherty
 */
public class ReporterFactory {

  @Deprecated
  public static Reporter getReporter(AnswerValue answerValue, String reporterName, Map<String, String> config)
      throws WdkModelException, WdkUserException {
    return createReporterInstance(answerValue, reporterName).configure(config);
  }

  public static Reporter getReporter(AnswerValue answerValue, String reporterName, JSONObject config)
      throws WdkModelException, ReporterConfigException {
    return createReporterInstance(answerValue, reporterName).configure(config);
  }

  private static Reporter createReporterInstance(AnswerValue answerValue, String reporterName) throws WdkModelException {

    Question question = answerValue.getAnswerSpec().getQuestion();
    RecordClass recordClass = question.getRecordClass();
    Map<String, ReporterRef> rptMap = question.getReporterMap();

    ReporterRef reporterRef = rptMap.get(reporterName);
    if (reporterRef == null) {
      throw new WdkModelException("The reporter " + reporterName + " is not registered for " +
          "question " + question.getFullName() + " or record class " + recordClass.getFullName());
    }

    String implClassName = reporterRef.getImplementation();
    if (implClassName == null) {
      throw new WdkModelException("The reporter " + reporterName + " needs an implementation class.");
    }

    try {
      // find class and try to instantiate, then assign answer value
      return ((Reporter)Class.forName(implClassName).getConstructor().newInstance())
          .setProperties(reporterRef)
          .setAnswerValue(answerValue);
    }
    catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException |
        IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new WdkModelException("Unable to instantiate reporter instance for class " + implClassName, ex);
    }
  }
}
