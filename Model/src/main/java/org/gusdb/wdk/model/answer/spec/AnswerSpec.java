package org.gusdb.wdk.model.answer.spec;

import static org.gusdb.fgputil.functional.Functions.filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSet;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

public class AnswerSpec implements Validateable<AnswerSpec> {

  public static AnswerSpecBuilder builder(WdkModel wdkModel) {
    return new AnswerSpecBuilder(wdkModel);
  }

  public static AnswerSpecBuilder builder(AnswerSpec answerSpec) {
    return new AnswerSpecBuilder(answerSpec);
  }

  public static AnswerSpecBuilder builder(SemanticallyValid<AnswerSpec> validAnswerSpec) {
    return new AnswerSpecBuilder(validAnswerSpec);
  }

  public static SemanticallyValid<QueryInstanceSpec> getValidQueryInstanceSpec(SemanticallyValid<AnswerSpec> validSpec) {
    // we know we can simply return the query instance spec because the answer spec is valid
    return validSpec.get().getQueryInstanceSpec().getSemanticallyValid().getLeft();
  }

  public static DisplayablyValid<QueryInstanceSpec> getValidQueryInstanceSpec(DisplayablyValid<AnswerSpec> validSpec) {
    // we know we can simply return the query instance spec because the answer spec is valid
    return validSpec.get().getQueryInstanceSpec().getDisplayablyValid().getLeft();
  }

  private final WdkModel _wdkModel;

  private final String _questionName;
  private final Question _question;

  /**
   * Map of param name (without set name) to stable value (always a string),
   * which are:
   * <dl>
   * <dt>StringParam</dt>
   *   <dd>unquoted raw value</dd>
   * <dt>TimestampParam</dt>
   *   <dd>millisecs since 1970 (or whatever)</dd>
   * <dt>DatasetParam</dt>
   *   <dd>Dataset ID (PK int column in Datasets table in apicomm)</dd>
   * <dt>AbstractEnumParam</dt>
   *   <dd>unsorted string representation of term list (comma-delimited)</dd>
   * <dt>EnumParam</dt>
   *   <dd>(inherited)</dd>
   * <dt>FlatVocabParam</dt>
   *   <dd>(inherited)</dd>
   * <dt>FilterParam</dt>
   *   <dd>JSON string representing all filters applied (see FilterParam)</dd>
   * <dt>AnswerParam</dt>
   *   <dd>Step ID</dd>
   * </dl>
  */
  private final QueryInstanceSpec _queryInstanceSpec;

  // LEGACY!!  Any filtering code mods should be applied to the parameterized
  //     filter framework.  TODO: remove this code and migrade the DB
  // Name of (non-parameterized) filter instance applied to this step (if any), DB value of null = no filter
  // if any filters exist on a recordclass, model must have a "default" filter; usually this is
  // a filter that simply returns all the results. The default filter is automatically applied to a step.
  // This affects the UI- if no filter OR the default filter is applied, the filter icon does not appear
  private final Optional<String> _legacyFilterName;
  private final Optional<AnswerFilterInstance> _legacyFilter;

  // filters applied to this step
  private final FilterOptionList _filters;

  // view filters applied to this step
  private final FilterOptionList _viewFilters;

  // validation-related values
  private final ValidationBundle _validationBundle;

  // resource to look up steps referred to by answer param values
  private final StepContainer _stepContainer;

  private final ColumnFilterConfigSet _columnFilterConfig;

  AnswerSpec(User user, WdkModel wdkModel, String questionName, QueryInstanceSpecBuilder queryInstanceSpec,
      Optional<String> legacyFilterName, FilterOptionListBuilder filters, FilterOptionListBuilder viewFilters,
      ValidationLevel validationLevel, StepContainer stepContainer, FillStrategy fillStrategy, ColumnFilterConfigSet columnFilters) throws WdkModelException {
    _wdkModel = wdkModel;
    _questionName = questionName;
    _legacyFilterName = legacyFilterName;
    _stepContainer = stepContainer;
    _columnFilterConfig = columnFilters;
    ValidationBundleBuilder validation = ValidationBundle.builder(validationLevel);
    if (wdkModel.getQuestionByFullName(questionName).isEmpty()) {
      // invalid question name; cannot validate other data
      validation.addError("Question '" + questionName + "' is not supported.");
      _question = null;
      _legacyFilter = Optional.empty();
      _queryInstanceSpec = queryInstanceSpec.buildInvalid();
      _filters = filters.buildInvalid();
      _viewFilters = viewFilters.buildInvalid();
    }
    else {
      _question = wdkModel.getQuestionByFullName(questionName).get(); // we know this will not throw
      _queryInstanceSpec = queryInstanceSpec.buildValidated(user, _question.getQuery(),
          stepContainer, validationLevel, fillStrategy);
      if (_queryInstanceSpec.isValid()) {
        // replace passed filter lists with new ones that have always-on filters applied
        SimpleAnswerSpec simpleSpec = new SimpleAnswerSpec(_question, _queryInstanceSpec);
        filters = applyAlwaysOnFilters(filters, _question.getFilters(), simpleSpec);
        // note: view filters unaffected; filters cannot be view-only and always-on
      }
      _legacyFilter = getAssignedLegacyFilter(validation);
      _filters = filters.buildValidated(_question, Filter.FilterType.STANDARD, validationLevel);
      _viewFilters = viewFilters.buildValidated(_question, Filter.FilterType.VIEW_ONLY, validationLevel);
      validation.aggregateStatus(_queryInstanceSpec, _filters, _viewFilters);
    }
    _validationBundle = validation.build();
  }

  private Optional<AnswerFilterInstance> getAssignedLegacyFilter(ValidationBundleBuilder validation) {
    if (_legacyFilterName.isEmpty()) return Optional.empty();
    Function<String,Optional<AnswerFilterInstance>> blah = name -> _question.getRecordClass().getFilterInstance(name);
    Optional<AnswerFilterInstance> filterInstance = _legacyFilterName.flatMap(blah);
    if (filterInstance.isEmpty()) {
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

  public QueryInstanceSpec getQueryInstanceSpec() {
    return _queryInstanceSpec;
  }

  public int getAnswerParamCount() {
    return hasValidQuestion() ? _question.getQuery().getAnswerParamCount() : 0;
  }

  public Optional<String> getLegacyFilterName() {
    return _legacyFilterName;
  }

  public Optional<AnswerFilterInstance> getLegacyFilter() {
    return _legacyFilter;
  }

  public FilterOptionList getFilterOptions() {
    return _filters;
  }

  public FilterOptionList getViewFilterOptions() {
    return _viewFilters;
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
      Map<String, Filter> allFilters, SimpleAnswerSpec simpleSpec) throws WdkModelException {
    // typically always-on filters should be applied before other filters, so put them up front
    FilterOptionListBuilder newFilters = FilterOptionList.builder();
    List<Filter> alwaysOnFilters = filter(allFilters.values(), Filter::getIsAlwaysApplied);
    for (Filter filter : alwaysOnFilters) {
      if (!incomingFilters.hasFilter(filter.getKey())) {
        try {
          JSONObject defaultValue = filter.getDefaultValue(simpleSpec);
          if (defaultValue != null) {
            newFilters.addFilterOption(FilterOption.builder()
                .setFilterName(filter.getKey())
                .setValue(defaultValue));
          }
        }
        catch (Exception e) {
          throw new WdkModelException("Could not apply always-on filter '" +
              filter.getKey() + "'. The answer spec may be missing information " +
                  "needed to fetch a default value for this filter. " +
                  "AnswerSpec: " + simpleSpec, e);
        }
      }
    }
    // now add back any original incoming filters
    newFilters.addAllFilters(incomingFilters.buildInvalid());
    return newFilters;
  }

  public SimpleAnswerSpec toSimpleAnswerSpec() {
    return new SimpleAnswerSpec(_question, _queryInstanceSpec);
  }

  public StepContainer getStepContainer() {
    return _stepContainer;
  }

  public ColumnFilterConfigSet getColumnFilterConfig() {
    return _columnFilterConfig;
  }
}
