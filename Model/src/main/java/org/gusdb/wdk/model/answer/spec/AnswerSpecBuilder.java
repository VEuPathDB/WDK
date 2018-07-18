package org.gusdb.wdk.model.answer.spec;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.answer.spec.ParamValueSet.ParamValueSetBuilder;
import org.json.JSONObject;

public class AnswerSpecBuilder {

  private final WdkModel _wdkModel;
  private String _questionName;
  private String _legacyFilterName;
  private ParamValueSetBuilder _paramValues;
  private FilterOptionListBuilder _filters;
  private FilterOptionListBuilder _viewFilters;
  private int _assignedWeight = 0;

  public AnswerSpecBuilder(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  public AnswerSpecBuilder(AnswerSpec answerSpec) {
    _wdkModel = answerSpec.getWdkModel();
    _questionName = answerSpec.getQuestionName();
    _paramValues = ParamValueSet.builder().fromParamValueSet(answerSpec.getParamValues());
    _legacyFilterName = answerSpec.getLegacyFilterName();
    _filters = FilterOptionList.builder().fromFilterOptionList(answerSpec.getFilterValues());
    _viewFilters = FilterOptionList.builder().fromFilterOptionList(answerSpec.getViewFilterValues());
    _assignedWeight = answerSpec.getWeight();
  }

  public AnswerSpecBuilder setDbParamFiltersJson(JSONObject paramFiltersJson) {
    setParamValues(ParamFiltersClobFormat.parseParamsJson(paramFiltersJson));
    setFilterOptions(ParamFiltersClobFormat.parseFiltersJson(paramFiltersJson));
    setViewFilterOptions(ParamFiltersClobFormat.parseViewFiltersJson(paramFiltersJson));
    return this;
  }

  public AnswerSpec build(ValidationLevel level) {
    return new AnswerSpec(_wdkModel, _questionName, _paramValues, _legacyFilterName, _filters, _viewFilters, _assignedWeight, level);
  }

  public AnswerSpecBuilder setQuestionName(String questionName) {
    _questionName = questionName;
    return this;
  }

  public AnswerSpecBuilder setLegacyFilterName(String legacyFilterName) {
    _legacyFilterName = legacyFilterName;
    return this;
  }

  public AnswerSpecBuilder setParamValues(ParamValueSetBuilder paramValues) {
    _paramValues = paramValues;
    return this;
  }

  public AnswerSpecBuilder setParamValue(String paramName, String stableValue) {
    _paramValues.put(paramName, stableValue);
    return this;
  }

  public AnswerSpecBuilder setFilterOptions(FilterOptionListBuilder filterOptions) {
    _filters = filterOptions;
    return this;
  }

  public AnswerSpecBuilder setViewFilterOptions(FilterOptionListBuilder viewFilterOptions) {
    _viewFilters = viewFilterOptions;
    return this;
  }

  public AnswerSpecBuilder setAssignedWeight(int assignedWeight) {
    _assignedWeight = assignedWeight;
    return this;
  }
}
