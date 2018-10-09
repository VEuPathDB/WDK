package org.gusdb.wdk.cache;

import java.util.Date;

import org.gusdb.wdk.model.answer.spec.AnswerFormatting;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;

public class AnswerRequest {

  private final Date _creationDate;
  private final AnswerSpec _answerSpec;
  private final AnswerFormatting _formatting;

  public AnswerRequest(AnswerSpec answerSpec, AnswerFormatting formatting) {
    _creationDate = new Date();
    _answerSpec = answerSpec;
    _formatting = formatting;
  }

  public Date getCreationDate() {
    return _creationDate;
  }

  public AnswerSpec getAnswerSpec() {
    return _answerSpec;
  }

  public AnswerFormatting getFormatting() {
    return _formatting;
  }
}
