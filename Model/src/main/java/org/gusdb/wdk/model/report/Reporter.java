package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONObject;

/**
 * A Reporter is used to download the content of the AnswerValue passed to its constructor.  Typically any
 * paging data in the passed AnswerValue is ignored; otherwise the AnswerValue is used to generate a result
 * list as-is.
 *
 * @author rdoherty
 */
public interface Reporter {

  enum ContentDisposition {
    INLINE, ATTACHMENT
  }

  // NOTE: implementing classes MUST have a constructor which takes a single AnswerValue argument
  // public Reporter(AnswerValue answerValue);

  /**
   * Pass on any properties contained in reporter reference to the reporter
   *
   * @param properties properties of the reference to this reporter
   * @throws WdkModelException
   */
  void setProperties(ReporterInfo reporterInfo) throws WdkModelException;

  /**
   * A legacy method to gather user configurations for the reporter.  Called by struts actions.
   * @param config
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  Reporter configure(Map<String, String> config) throws ReporterConfigException, WdkModelException;

  /**
   * Gather end-user configurations for this reporter.  Uses the builder pattern (returns this reporter).
   * @param config
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException;

  // methods used to deliver report
  String getHttpContentType();
  ContentDisposition getContentDisposition();
  String getDownloadFileName();
  void report(OutputStream stream) throws WdkModelException;

  // dumps information about
  String getHelp();
}
