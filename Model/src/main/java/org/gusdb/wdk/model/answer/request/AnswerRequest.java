package org.gusdb.wdk.model.answer.request;

import java.util.Date;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;

public class AnswerRequest {

  private final Date _creationDate;
  private final RunnableObj<AnswerSpec> _answerSpec;
  private final AnswerFormatting _formatting;

  public AnswerRequest(RunnableObj<AnswerSpec> answerSpec, AnswerFormatting formatting) {
    _creationDate = new Date();
    _answerSpec = answerSpec;
    _formatting = formatting;
  }

  public Date getCreationDate() {
    return _creationDate;
  }

  public RunnableObj<AnswerSpec> getAnswerSpec() {
    return _answerSpec;
  }

  public AnswerFormatting getFormatting() {
    return _formatting;
  }
}
