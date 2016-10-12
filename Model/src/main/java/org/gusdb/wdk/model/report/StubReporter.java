package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONObject;

public class StubReporter extends AbstractReporter {

  public StubReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override public void configure(Map<String, String> config) throws WdkUserException, WdkModelException { }
  @Override public void configure(JSONObject config) throws WdkUserException, WdkModelException { }
  @Override protected void write(OutputStream out) throws WdkModelException { }

}
