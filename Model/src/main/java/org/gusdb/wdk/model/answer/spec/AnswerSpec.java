package org.gusdb.wdk.model.answer.spec;

import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.swallowAndGet;

import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.answer.spec.ParamValueSet.ParamValueSetBuilder;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class AnswerSpec implements Validateable {

  public static AnswerSpecBuilder builder(WdkModel wdkModel) {
    return new AnswerSpecBuilder(wdkModel);
  }

  public static AnswerSpecBuilder builder(AnswerSpec answerSpec) {
    return new AnswerSpecBuilder(answerSpec);
  }

  private final WdkModel _wdkModel;

  private final String _questionName;
  private final Question _question;

  // Map of param name (without set name) to stable value (always a string), which are:
  // StringParam: unquoted raw value
  // TimestampParam: millisecs since 1970 (or whatever)
  // DatasetParam: Dataset ID (PK int column in Datasets table in apicomm)
  // AbstractEnumParam: unsorted string representation of term list (comma-delimited)
  // EnumParam: (inherited)
  // FlatVocabParam: (inherited)
  // FilterParam: JSON string representing all filters applied (see FilterParam)
  // AnswerParam: Step ID
  private final ParamValueSet _params;

  // LEGACY!!  Any filtering code mods should be applied to the parameterized
  //     filter framework.  TODO: remove this code and migrade the DB
  // Name of (non-parameterized) filter instance applied to this step (if any), DB value of null = no filter
  // if any filters exist on a recordclass, model must have a "default" filter; usually this is
  // a filter that simply returns all the results. The default filter is automatically applied to a step.
  // This affects the UI- if no filter OR the default filter is applied, the filter icon does not appear
  private final String _legacyFilterName;
  private final AnswerFilterInstance _legacyFilter;

  // filters applied to this step
  private final FilterOptionList _filters;

  // view filters applied to this step
  private final FilterOptionList _viewFilters;

  // only applied to leaf steps, user-defined
  // during booleans, weights of records are modified (per boolean-specific logic, see BooleanQuery)
  private final int _assignedWeight;

  // validation-related values
  private final ValidationBundle _validationBundle;

  public AnswerSpec(WdkModel wdkModel, String questionName, ParamValueSetBuilder paramValues,
      String legacyFilterName, FilterOptionListBuilder filters, FilterOptionListBuilder viewFilters,
      int assignedWeight, ValidationLevel validationLevel) {
    _wdkModel = wdkModel;
    _questionName = questionName;
    _legacyFilterName = legacyFilterName;
    _assignedWeight = assignedWeight;
    ValidationBundleBuilder validation = ValidationBundle.builder(validationLevel);
    if (!wdkModel.hasQuestion(questionName)) {
      // invalid question name; cannot validate other data
      validation.addError("Question '" + questionName + "' is not supported.");
      _question = null;
      _legacyFilter = null;
      _params = paramValues.buildInvalid();
      _filters = filters.buildInvalid();
      _viewFilters = viewFilters.buildInvalid();
    }
    else {
      _question = swallowAndGet(() -> wdkModel.getQuestion(questionName));
      _legacyFilter = getAssignedLegacyFilter(validation);
      _params = paramValues.buildValidated(_question.getQuery(), validationLevel);
      if (_params.isValid()) {
        // replace passed filter lists with new ones that have always-on filters applied
        SimpleAnswerSpec simpleSpec = new SimpleAnswerSpec(_question, _params);
        filters = applyAlwaysOnFilters(filters, _question.getFilters(), simpleSpec);
        viewFilters = applyAlwaysOnFilters(viewFilters, _question.getViewFilters(), simpleSpec);
      }
      _filters = filters.buildValidated(_question, Filter.FilterType.STANDARD, validationLevel);
      _viewFilters = viewFilters.buildValidated(_question, Filter.FilterType.VIEW_ONLY, validationLevel);
      validation.aggregateStatus(_params, _filters, _viewFilters);
    }
    _validationBundle = validation.build();
  }

  private AnswerFilterInstance getAssignedLegacyFilter(ValidationBundleBuilder validation) {
    if (_legacyFilterName == null || _legacyFilterName.isEmpty()) return null;
    AnswerFilterInstance filterInstance = _question.getRecordClass().getFilterInstance(_legacyFilterName);
    if (filterInstance == null) {
      validation.addError("Legacy answer filter with name '" + _legacyFilterName + "' does not exist.");
    }
    return filterInstance;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public String getQuestionName() {
    return _questionName;
  }

  public Question getQuestion() {
    return _question;
  }

  public boolean hasValidQuestion() {
    return _question != null;
  }

  public ParamValueSet getParamValues() {
    return _params;
  }

  public int getAnswerParamCount() {
    return _question == null ? 0 : _question.getQuery().getAnswerParamCount();
  }

  public String getLegacyFilterName() {
    return _legacyFilterName;
  }

  public AnswerFilterInstance getLegacyFilter() {
    return _legacyFilter;
  }

  public FilterOptionList getFilterValues() {
    return _filters;
  }

  public FilterOptionList getViewFilterValues() {
    return _viewFilters;
  }

  public int getWeight() {
    return _assignedWeight;
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  // New for GUS4: apply any always-on filter values to this step that are automatically applied to
  //   new steps, but may not have been applied to steps already in the DB.  This allows model XML
  //   authors to add always-on filters to the model without worrying about existing steps in the DB (as
  //   long as they override and correctly implement the getIsAlwaysApplied() and getDefaultValue() methods
  //   in their Filter implementation/configuration.
  private static FilterOptionListBuilder applyAlwaysOnFilters(FilterOptionListBuilder incomingFilters,
      Map<String, Filter> allFilters, SimpleAnswerSpec simpleSpec) {
    // typically always-on filters should be applied before other filters, so put them up front
    FilterOptionListBuilder newFilters = FilterOptionList.builder();
    List<Filter> alwaysOnFilters = filter(allFilters.values(), filter -> filter.getIsAlwaysApplied());
    for (Filter filter : alwaysOnFilters) {
      if (!incomingFilters.hasFilter(filter.getKey())) {
        JSONObject defaultValue = filter.getDefaultValue(simpleSpec);
        if (defaultValue != null) {
          newFilters.addFilterOption(FilterOption.builder()
              .setFilterName(filter.getKey())
              .setValue(defaultValue));
        }
      }
    }
    // now add back any original incoming filters
    newFilters.fromFilterOptionList(incomingFilters.buildInvalid());
    return newFilters;
  }

  public SimpleAnswerSpec toSimpleAnswerSpec() {
    return new SimpleAnswerSpec(_question, _params);
  }

}
