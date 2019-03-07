package org.gusdb.wdk.model.answer.spec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidObjectWrappingException;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.FilterOption.FilterOptionBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

public class AnswerSpecBuilder {

  private final WdkModel _wdkModel;
  private String _questionName = "";
  private Optional<String> _legacyFilterName = Optional.empty();
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
    this(validAnswerSpec.get());
  }

  public AnswerSpecBuilder setDbParamFiltersJson(JSONObject paramFiltersJson) {
    setQueryInstanceSpec(ParamAndFiltersFormat.parseParamsJson(paramFiltersJson));
    setFilterOptions(ParamAndFiltersFormat.parseFiltersJson(paramFiltersJson));
    setViewFilterOptions(ParamAndFiltersFormat.parseViewFiltersJson(paramFiltersJson));
    // caller may have already called setAssignedWeight on answer spec builder
    _queryInstanceSpec.setAssignedWeight(_assignedWeight);
    return this;
  }

  public AnswerSpec build(User user, StepContainer stepContainer,
      ValidationLevel level) throws WdkModelException {
    return build(user, stepContainer, level, FillStrategy.NO_FILL);
  }

  public AnswerSpec build(User user, StepContainer stepContainer,
      ValidationLevel level, FillStrategy fillStrategy) throws WdkModelException {
    return new AnswerSpec(user, _wdkModel, _questionName, _queryInstanceSpec,
        _legacyFilterName, _filters, _viewFilters, level, stepContainer, fillStrategy);
  }

  /**
   * Builds a Runnable answer spec.  Should only be called when caller has a legitimate reason to believe
   * the answer spec constructed will be runnable, since this will throw a ValidObjectWrappingException
   * (a runtime exception) if the answer spec constructed is not runnable 
   * @param user
   * @param stepContainer
   * @return
   * @throws WdkModelException
   * @throws ValidObjectWrappingException
   */
  public RunnableObj<AnswerSpec> buildRunnable(User user, StepContainer stepContainer) throws WdkModelException {
    return ValidObjectFactory.getRunnable(build(user, stepContainer, ValidationLevel.RUNNABLE));
  }

  public AnswerSpecBuilder setQuestionName(String questionName) {
    _questionName = questionName;
    return this;
  }

  public String getQuestionName() {
    return _questionName;
  }

  public AnswerSpecBuilder setLegacyFilterName(Optional<String> legacyFilterName) {
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

  public String getParamValue(String name) {
    return _queryInstanceSpec.get(name);
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

  public AnswerSpecBuilder nullifyAnswerParams() {
    List<AnswerParam> answerParams = _wdkModel.getQuestionByName(_questionName)
        .map(q -> q.getQuery().getAnswerParams()).orElse(null);
    // if question name not set, do nothing
    if (answerParams != null) {
      for (AnswerParam param : answerParams) {
        setParamValue(param.getName(), AnswerParam.NULL_VALUE);
      }
    }
    return this;
  }

}
