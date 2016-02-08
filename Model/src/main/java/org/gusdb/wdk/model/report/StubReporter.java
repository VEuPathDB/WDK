package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONException;

public class StubReporter extends Reporter {

  public StubReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }

  @Override public String getConfigInfo() { return null; }
  @Override protected void initialize() throws WdkModelException { }
  @Override protected void complete() { }
  @Override public void write(OutputStream out) throws WdkModelException,
      NoSuchAlgorithmException, SQLException, JSONException, WdkUserException { }

}
