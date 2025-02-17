package org.gusdb.wdk.model.answer.spec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.ColumnFilterConfigSet.ColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOption.FilterOptionBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

public class AnswerSpecBuilder {

  private static final Logger LOG = Logger.getLogger(AnswerSpecBuilder.class);

  private final WdkModel _wdkModel;
  private String _questionName = "";
  private Optional<String> _legacyFilterName = Optional.empty();
  private QueryInstanceSpecBuilder _queryInstanceSpec = QueryInstanceSpec.builder();
  private FilterOptionListBuilder _filters = FilterOptionList.builder();
  private FilterOptionListBuilder _viewFilters = FilterOptionList.builder();
  private ColumnFilterConfigSetBuilder _columnFilters;

  public AnswerSpecBuilder(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _columnFilters = new ColumnFilterConfigSetBuilder();
  }

  public AnswerSpecBuilder(AnswerSpec answerSpec) {
    _wdkModel = answerSpec.getWdkModel();
    _questionName = answerSpec.getQuestionName();
    _queryInstanceSpec = QueryInstanceSpec.builder().fromQueryInstanceSpec(answerSpec.getQueryInstanceSpec());
    _legacyFilterName = answerSpec.getLegacyFilterName();
    _filters = FilterOptionList.builder().addAllFilters(answerSpec.getFilterOptions());
    _viewFilters = FilterOptionList.builder().addAllFilters(answerSpec.getViewFilterOptions());
    _columnFilters = new ColumnFilterConfigSetBuilder(answerSpec.getColumnFilterConfig());
  }

  public AnswerSpecBuilder(SemanticallyValid<AnswerSpec> validAnswerSpec) {
    // TODO optimize- we want to save off the semantically valid pieces of the valid spec so
    //      that if a user changes one of them, the others don't need to be revalidated; for
    //      now, however, just pull the spec out <- means we will completely revalidate even
    //      for just a filter addition
    this(validAnswerSpec.get());
  }

  public AnswerSpecBuilder setDbParamFiltersJson(JSONObject paramFiltersJson, int assignedWeight) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Parsing display_params JSON Object into its parts: " + paramFiltersJson.toString(2));
    }
    QueryInstanceSpecBuilder qiSpec = ParamsAndFiltersDbColumnFormat.parseParamsJson(paramFiltersJson);
    qiSpec.setAssignedWeight(assignedWeight);
    setQueryInstanceSpec(qiSpec);
    setFilterOptions(ParamsAndFiltersDbColumnFormat.parseFiltersJson(paramFiltersJson));
    // TODO: As of 8/20/19 we do not read view filters from the database; should purge their existence at some point
    //setViewFilterOptions(ParamsAndFiltersDbColumnFormat.parseViewFiltersJson(paramFiltersJson));
    setColumnFilters(ParamsAndFiltersDbColumnFormat.parseColumnFilters(paramFiltersJson));
    return this;
  }

  public AnswerSpec build(User requestingUser, StepContainer stepContainer,
      ValidationLevel validationLevel) throws WdkModelException {
    return build(requestingUser, stepContainer, validationLevel, FillStrategy.NO_FILL);
  }

  public AnswerSpec build(User requestingUser, StepContainer stepContainer,
      ValidationLevel validationLevel, FillStrategy fillStrategy) throws WdkModelException {
    return new AnswerSpec(_wdkModel, _questionName, _queryInstanceSpec,
      _legacyFilterName, _filters, _viewFilters, requestingUser, validationLevel, stepContainer,
      fillStrategy, _columnFilters.build());
  }

  /**
   * Builds a Runnable answer spec.
   *
   * Should only be called when caller has a legitimate reason to believe the
   * answer spec constructed will be runnable, since this will throw a
   * ValidObjectWrappingException (a runtime exception) if the answer spec
   * constructed is not runnable
   */
  public RunnableObj<AnswerSpec> buildRunnable(User requestingUser, StepContainer stepContainer) throws WdkModelException {
    return ValidObjectFactory.getRunnable(build(requestingUser, stepContainer, ValidationLevel.RUNNABLE, FillStrategy.NO_FILL));
  }

  public AnswerSpecBuilder setQuestionFullName(String questionName) {
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
    return this;
  }

  public QueryInstanceSpecBuilder getQueryInstanceSpecBuilder() {
    return _queryInstanceSpec;
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

  public FilterOptionListBuilder getFilterOptions() {
    return _filters;
  }

  public FilterOptionListBuilder getViewFilterOptions() {
    return _viewFilters;
  }

  /**
   * Finds the first instance of a filter with the passed name and replaces its
   * value with the passed value. If no filters are found, nothing will be
   * replaced.
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
    List<AnswerParam> answerParams = _wdkModel.getQuestionByFullName(_questionName)
        .map(q -> q.getQuery().getAnswerParams()).orElse(null);
    // if question name not set, do nothing
    if (answerParams != null) {
      for (AnswerParam param : answerParams) {
        setParamValue(param.getName(), AnswerParam.NULL_VALUE);
      }
    }
    return this;
  }

  public AnswerSpecBuilder setColumnFilters(ColumnFilterConfigSetBuilder build) {
    _columnFilters = build;
    return this;
  }

  public ColumnFilterConfigSetBuilder getColumnFilters() {
    return _columnFilters;
  }
}
