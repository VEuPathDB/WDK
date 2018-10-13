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

  public static enum ContentDisposition {
    INLINE, ATTACHMENT;
  }

  // NOTE: implementing classes MUST have a constructor which takes a single AnswerValue argument
  // public Reporter(AnswerValue answerValue);

  /**
   * Pass on any required information contained in reporter reference to the reporter
   * 
   * @param reporterRef reference to this reporter
   * @throws WdkModelException
   */
  public void setProperties(ReporterRef reporterRef) throws WdkModelException;

  /**
   * A legacy method to gather user configurations for the reporter.  Called by struts actions.
   * @param config
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public Reporter configure(Map<String, String> config) throws ReporterConfigException, WdkModelException;

  /**
   * Gather end-user configurations for this reporter.  Uses the builder pattern (returns this reporter). 
   * @param config
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException;

  // methods used to deliver report
  public String getHttpContentType();
  public ContentDisposition getContentDisposition();
  public String getDownloadFileName();
  public void report(OutputStream stream) throws WdkModelException;

  // dumps information about 
  public String getHelp();
}
