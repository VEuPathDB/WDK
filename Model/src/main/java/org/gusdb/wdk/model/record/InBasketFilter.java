package org.gusdb.wdk.model.record;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.gusdb.wdk.model.filter.StepFilter;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.BasketFactory;
import org.json.JSONObject;

public class InBasketFilter extends StepFilter {

  private static final String FILTER_NAME = "in_basket_filter";
  private static final String FILTER_DESCRIPTION = "Filters out records not currently in your basket.";

  private final WdkModel _wdkModel;

  public InBasketFilter(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _defaultValue = new JSONObject();
    setIsViewOnly(true);
    setDisplay(FILTER_DESCRIPTION);
    setDescription(FILTER_DESCRIPTION);
  }

  @Override
  public String getKey() {
    return FILTER_NAME;
  }

  @Override
  public String getDisplayValue(AnswerValue answer, JSONObject jsValue) throws WdkModelException {
    return FILTER_DESCRIPTION;
  }

  @Override
  public String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException {
    RecordClass recordClass = answer.getAnswerSpec().getQuestion().getRecordClass();
    PrimaryKeyDefinition pkDef = recordClass.getPrimaryKeyDefinition();
    return
      "select baskidq.* from" +
      " ( " + idSql + " ) baskidq," +
      " ( " + BasketFactory.getBasketSelectSql(_wdkModel, recordClass) + " ) baskids" +
      " where " + pkDef.createJoinClause("baskidq", "baskids");
  }

  @Override
  public boolean defaultValueEquals(SimpleAnswerSpec simpleAnswerSpec, JSONObject value)
      throws WdkModelException {
    return false;
  }

  @Override
  public ValidationBundle validate(Question question, JSONObject value, ValidationLevel validationLevel) {
    // No validation needed since this filter has no configuration. Its presence is all that is required.
    return ValidationBundle.builder(validationLevel).build();
  }

}
