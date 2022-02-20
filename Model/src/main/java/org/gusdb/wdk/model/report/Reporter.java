package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONObject;

/**
 * A Reporter is used to convert the results of assigned AnswerValue into a custom format. This
 * could include reduction, generating statistics, various output types (JSON/XML/tabular), etc.
 *
 * A reporter is configured in the following order:
 *
 * 1. Assignment of XML properties associated with the implementation class assignment (i.e. in XML)
 * 2. Assignment of the AnswerValue
 * 3. Assignment of the configuration for this particular report (e.g. requested attributes)
 *
 * Thus, implementing classes should not rely on the config when processing the answer value.
 *
 * Once the reporter is configured, its report() method will be called, which will stream the
 * formatted result to the client.  Default methods are provided 
 * @author rdoherty
 */
public interface Reporter {

  enum ContentDisposition {
    INLINE, ATTACHMENT
  }

  /**
   * Pass on any properties assigned in the model XML for this reporter
   *
   * @param properties properties set in the reference to this reporter
   * @throws WdkModelException if properties invalid
   */
  Reporter setProperties(PropertiesProvider properties) throws WdkModelException;


  /**
   * Assigns the answer value for this reporter
   * @param answerValue
   * @return
   * @throws WdkModelException
   */
  Reporter setAnswerValue(AnswerValue answerValue) throws WdkModelException;

  /**
   * Assign instance-level configuration for this reporter.
   * @param config configuration for this reporter
   * @return this
   * @throws ReporterConfigException if config is invalid
   * @throws WdkModelException if unable to consume config
   */
  Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException;

  /**
   * Streams the report to the client 
   *
   * @param stream stream to which report should be written
   * @throws WdkModelException if something goes wrong
   */
  void report(OutputStream stream) throws WdkModelException;

  /**
   * A legacy method to assign user configuration for the reporter.  This method is
   * deprecated and any new Reporter implementations should use configure(JSONObject)
   *
   * Default behavior if not overridden is a no-op.
   *
   * @param config configuration for this reporter
   * @return this
   * @throws ReporterConfigException if config is invalid
   * @throws WdkModelException if unable to consume config
   */
  @Deprecated
  default Reporter configure(Map<String,String> config) throws ReporterConfigException, WdkModelException {
    // default action is a no-op; no reason for new reporters to implement this method
    return this;
  }

  /**
   * @return value to assign to the Content-Type HTTP response header
   */
  default String getHttpContentType() {
    return "application/json";
  }

  /**
   * @return file name to assign if client should download the response as an attachment
   */
  default String getDownloadFileName() {
    // by default, display the result in the browser, by setting the file name as null
    return null;
  }

  /**
   * @return content disposition value; used in combination with getDownloadFileName()
   * to construct the Content-Disposition response header value
   */
  default ContentDisposition getContentDisposition() {
    return (getDownloadFileName() == null ?
        ContentDisposition.INLINE : ContentDisposition.ATTACHMENT);
  }

  /**
   * @return help text describing the use of this reporter
   */
  @Deprecated
  default public String getHelp() {
    return "This reporter is not documented.";
  }

}
