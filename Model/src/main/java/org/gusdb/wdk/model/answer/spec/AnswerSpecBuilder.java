package org.gusdb.wdk.model.answer.spec;

import java.util.Map;
import java.util.function.Function;

import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.answer.spec.FilterOption.FilterOptionBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.user.StepContainer;
import org.json.JSONObject;

public class AnswerSpecBuilder {

  private final WdkModel _wdkModel;
  private String _questionName = "";
  private String _legacyFilterName;
  private int _assignedWeight = 0; // will be reconciled with one in _queryInstanceSpec
  private QueryInstanceSpecBuilder _queryInstanceSpec = QueryInstanceSpec.builder();
  private FilterOptionListBuilder _filters = FilterOptionList.builder();
  private FilterOptionListBuilder _viewFilters = FilterOptionList.builder();

  public AnswerSpecBuilder(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  public AnswerSpecBuilder(AnswerSpec answerSpec) {
    _wdkModel = answerSpec.getWdkModel();
    _questionName = answerSpec.getQuestionName();
    _queryInstanceSpec = QueryInstanceSpec.builder().fromQueryInstanceSpec(answerSpec.getQueryInstanceSpec());
    _assignedWeight = _queryInstanceSpec.getAssignedWeight();
    _legacyFilterName = answerSpec.getLegacyFilterName();
    _filters = FilterOptionList.builder().fromFilterOptionList(answerSpec.getFilterOptions());
    _viewFilters = FilterOptionList.builder().fromFilterOptionList(answerSpec.getViewFilterOptions());
  }

  public AnswerSpecBuilder(SemanticallyValid<AnswerSpec> validAnswerSpec) {
    // TODO optimize- we want to save off the semantically valid pieces of the valid spec so
    //      that if a user changes one of them, the others don't need to be revalidated; for
    //      now, however, just pull the spec out <- means we will completely revalidate even
    //      for just a filter addition
    this(validAnswerSpec.getObject());
  }

  public AnswerSpecBuilder setDbParamFiltersJson(JSONObject paramFiltersJson) {
    setQueryInstanceSpec(ParamFiltersClobFormat.parseParamsJson(paramFiltersJson));
    setFilterOptions(ParamFiltersClobFormat.parseFiltersJson(paramFiltersJson));
    setViewFilterOptions(ParamFiltersClobFormat.parseViewFiltersJson(paramFiltersJson));
    // caller may have already called setAssignedWeight on answer spec builder
    _queryInstanceSpec.setAssignedWeight(_assignedWeight);
    return this;
  }

  public AnswerSpec build(ValidationLevel level) {
    return build(level, null);
  }

  public AnswerSpec build(ValidationLevel level, StepContainer stepContainer) {
    return new AnswerSpec(_wdkModel, _questionName, _queryInstanceSpec, _legacyFilterName, _filters, _viewFilters, level, stepContainer);
  }

  public RunnableObj<AnswerSpec> buildRunnable() {
    return ValidObjectFactory.getRunnable(build(ValidationLevel.RUNNABLE));
  }

  public AnswerSpecBuilder setQuestionName(String questionName) {
    _questionName = questionName;
    return this;
  }

  public AnswerSpecBuilder setLegacyFilterName(String legacyFilterName) {
    _legacyFilterName = legacyFilterName;
    return this;
  }

  public AnswerSpecBuilder setQueryInstanceSpec(QueryInstanceSpecBuilder queryInstanceSpec) {
    _queryInstanceSpec = queryInstanceSpec;
    _assignedWeight = queryInstanceSpec.getAssignedWeight();
    return this;
  }

  public AnswerSpecBuilder setParamValue(String paramName, String stableValue) {
    _queryInstanceSpec.put(paramName, stableValue);
    return this;
  }

  /**
   * Adds the param values represented by the entries in the passed map to the query instance spec,
   * but does NOT clear the current param values first.
   * 
   * @param params map representing a set of param values to add
   * @return this builder
   */
  public AnswerSpecBuilder setParamValues(Map<String, String> params) {
    _queryInstanceSpec.putAll(params);
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
    _queryInstanceSpec.setAssignedWeight(assignedWeight);
    return this;
  }

  public FilterOptionListBuilder getFilterOptions() {
    return _filters;
  }

  public FilterOptionListBuilder getViewFilterOptions() {
    return _viewFilters;
  }

  /**
   * Finds the first instance of a filter with the passed name and replaces its value with the passed value.
   * If no filters are found, nothing will be replaced.
   * 
   * @param filterName
   * @param newValue
   * @return
   */
  public AnswerSpecBuilder replaceFirstFilterOption(String filterName, Function<FilterOptionBuilder,FilterOptionBuilder> modifier) {
    for (int i = 0; i < _filters.size(); i++) {
      if (_filters.get(i).getFilterName().equals(filterName)) {
        _filters.set(i, modifier.apply(_filters.get(i)));
        break; // only replace first filter found
      }
    }
    return this;
  }

}
