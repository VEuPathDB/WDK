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

  // NOTE: implementing classes must have a constructor which takes a single AnswerValue argument
  // public Reporter(AnswerValue answerValue);

  // methods to configure reporter
  public void setProperties(Map<String, String> properties) throws WdkModelException;
  public void configure(Map<String, String> config) throws WdkUserException, WdkModelException;
  public void configure(JSONObject config) throws WdkUserException, WdkModelException;

  // methods used to deliver report
  public String getHttpContentType();
  public ContentDisposition getContentDisposition();
  public String getDownloadFileName();
  public void report(OutputStream stream) throws WdkModelException;

  // dumps information about 
  public String getHelp();
}
