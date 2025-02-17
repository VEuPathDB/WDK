package org.gusdb.wdk.model.filter;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class AnswerSpecFilter extends StepFilter {

  public static final String FILTER_NAME = "AnswerSpecFilter";

  public static final String KEY_ANSWER_SPEC = "answerSpec";

  public static StepFilterDefinition getDefinition() {
    StepFilterDefinition definition = new StepFilterDefinition();
    definition.setName(FILTER_NAME);
    definition.setDisplay("Another Search");
    definition.setDescription("Filter these results by the results of another search.");
    definition.setImplementation(AnswerSpecFilter.class.getName());
    definition.setView(null); // TBD?
    return definition;
  }

  @Override
  public String getKey() {
    return FILTER_NAME;
  }

  @Override
  public String getDisplayValue(AnswerValue answer, JSONObject jsValue)
      throws WdkModelException {
    return answer.getQuestion().getName() + ", with parameters { " +
      join(mapToList(answer.getParamDisplays().entrySet(),
          entry -> entry.getKey() + ": "  + entry.getValue()), ", ");
  }

  @Override
  public String getSql(AnswerValue answer, String idSql, JSONObject jsValue)
      throws WdkModelException {
    // TODO: parse answer spec and write SQL for this filter;
    //   since AnswerSpecFactory lives in wdk-service on trunk, this is much
    //   easier on the strategy-loading branch, where we can use AnswerSpecBuilder
    return idSql;
  }

  @Override
  public void setDefaultValue(JSONObject defaultValue) {
    throw new UnsupportedOperationException("Not supported until the defaultValueEquals() method is fully implemented");
  }

  /**
   * Not fully implemented yet.
   */
  @Override
  public boolean defaultValueEquals(SimpleAnswerSpec answerSpec, JSONObject value)  throws WdkModelException {
    return false;
  }

  @Override
  public ValidationBundle validate(Question question, JSONObject value, ValidationLevel validationLevel) {
    // TODO: write validation on the incoming answer spec
    return ValidationBundle.builder(ValidationLevel.SEMANTIC).build();
  }

}
