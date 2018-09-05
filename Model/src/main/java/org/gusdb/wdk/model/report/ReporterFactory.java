package org.gusdb.wdk.model.report;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
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
    Reporter reporter = createReporterInstance(answerValue, reporterName);
    reporter.configure(config);
    return reporter;
  }

  public static Reporter getReporter(AnswerValue answerValue, String reporterName, JSONObject config)
      throws WdkModelException, WdkUserException {
    Reporter reporter = createReporterInstance(answerValue, reporterName);
    reporter.configure(config);
    return reporter;
  }

  private static Reporter createReporterInstance(AnswerValue answerValue, String reporterName) throws WdkModelException {

    Question question = answerValue.getQuestion();
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
      // find class and try to instantiate with AnswerValue argument
      Class<?> reporterClass = Class.forName(implClassName);
      Class<?>[] paramClasses = { AnswerValue.class };
      Constructor<?> constructor = reporterClass.getConstructor(paramClasses);

      Object[] params = { answerValue };
      Reporter reporter = (Reporter) constructor.newInstance(params);
      reporter.setProperties(reporterRef);
      return reporter;
    }
    catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException |
        IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new WdkModelException(ex);
    }
  }
}
