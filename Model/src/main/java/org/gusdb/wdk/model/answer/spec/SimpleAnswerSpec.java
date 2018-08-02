package org.gusdb.wdk.model.answer.spec;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.question.Question;

public class SimpleAnswerSpec extends TwoTuple<Question, QueryInstanceSpec> {

  SimpleAnswerSpec(Question question, QueryInstanceSpec params) {
    super(question, params);
  }

  public Question getQuestion() { return getFirst(); }
  public QueryInstanceSpec getQueryInstanceSpec() { return getSecond(); }

}
