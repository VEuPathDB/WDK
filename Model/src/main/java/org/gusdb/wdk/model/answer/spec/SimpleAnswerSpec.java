package org.gusdb.wdk.model.answer.spec;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class SimpleAnswerSpec extends TwoTuple<Question, QueryInstanceSpec> {

  SimpleAnswerSpec(Question question, QueryInstanceSpec params) {
    super(question, params);
  }

  public Question getQuestion() { return getFirst(); }
  public QueryInstanceSpec getQueryInstanceSpec() { return getSecond(); }

  @Override
  public String toString() {
    return new JSONObject()
        .put("question", getFirst().getFullName())
        .put("params", FormatUtil.prettyPrint(getSecond().toMap(), Style.SINGLE_LINE))
        .toString(2);
  }
}
