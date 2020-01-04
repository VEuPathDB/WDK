package org.gusdb.wdk.model.question;

import org.gusdb.wdk.model.WdkModelBase;

public class QuestionReference extends WdkModelBase {

  private String _questionName;

  public void setQuestionRef(String questionName) {
    _questionName = questionName;
  }

  public String getQuestionRef() {
    return _questionName;
  }
}
