package org.gusdb.wdk.model.answer.spec;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.question.Question;

public class SimpleAnswerSpec extends TwoTuple<Question, ParamValueSet> {

  SimpleAnswerSpec(Question question, ParamValueSet params) {
    super(question, params);
  }

  public Question getQuestion() { return getFirst(); }
  public ParamValueSet getParams() { return getSecond(); }

}
