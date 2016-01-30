package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONException;

/**
 * For now this is a dummy class that can be referenced in the model XML to
 * "pretend" to handle WDK service JSON reporter output.  In truth, this
 * reporter doesn't do anything; a request for standard JSON will be intercepted
 * by the service and returned by service functionality.  We may refactor in
 * the future.
 * 
 * @author rdoherty
 */
public class WdkServiceJsonReporter extends Reporter {

  public WdkServiceJsonReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }

  @Override public String getConfigInfo() { return null; }
  @Override protected void initialize() throws WdkModelException { }
  @Override protected void complete() { }
  @Override public void write(OutputStream out) throws WdkModelException,
      NoSuchAlgorithmException, SQLException, JSONException, WdkUserException { }

}
