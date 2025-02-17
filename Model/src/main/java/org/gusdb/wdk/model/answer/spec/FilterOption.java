package org.gusdb.wdk.model.answer.spec;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

/**
 * Holds the user's choice of values for a filter.
 * The value is a weakly typed JSONObject because it is passed to a plugged in filter.
 */
public class FilterOption implements Validateable<FilterOption>, NamedObject {

  private static final Logger LOG = Logger.getLogger(FilterOption.class);

  public static class FilterOptionBuilder {

    private String _filterName = "";
    private JSONObject _value = new JSONObject();
    private boolean _isDisabled = false;

    private FilterOptionBuilder() {}

    private FilterOptionBuilder(FilterOption filterOption) {
      _filterName = filterOption.getKey();
      _value = JsonUtil.clone(filterOption.getValue());
      _isDisabled = filterOption.isDisabled();
    }

    public String getFilterName() {
      return _filterName;
    }

    public FilterOptionBuilder setFilterName(String filterName) {
      _filterName = filterName;
      return this;
    }

    public FilterOptionBuilder setValue(JSONObject value) {
      _value = value;
      return this;
    }

    public FilterOptionBuilder setDisabled(boolean isDisabled) {
      _isDisabled = isDisabled;
      return this;
    }

    public FilterOption buildInvalid() {
      return new FilterOption(_filterName, _value, _isDisabled, null, ValidationLevel.NONE);
    }

    public FilterOption buildValidated(Question question, ValidationLevel level) {
      return new FilterOption(_filterName, _value, _isDisabled, question, level);
    }
  }

  public static FilterOptionBuilder builder() {
    return new FilterOptionBuilder();
  }

  public static FilterOptionBuilder builder(FilterOption filterOption) {
    return new FilterOptionBuilder(filterOption);
  }

  // basic information about this filter option
  private final String _filterName;
  private final JSONObject _value;
  private final boolean _isDisabled;

  // referenced objects; set to null if this filter option is non-validated or invalid
  private final Filter _filter;

  // validation bundle describing validity of this filter option
  private final ValidationBundle _validationBundle;

  // standard constructor to create enabled filter option
  private FilterOption(String filterName, JSONObject value, boolean isDisabled, Question question, ValidationLevel validationLevel) {
    _filterName = filterName;
    _value = value;
    _isDisabled = isDisabled;
    if (validationLevel.isNone()) {
      _filter = null;
      _validationBundle = ValidationBundle.builder(validationLevel).build();
    }
    else {
      // attempt to validate the filter name and value
      Optional<Filter> filterOpt = question.getFilter(filterName);
      if (filterOpt.isPresent()) {
        // filter valid; validate the value using it
        _filter = filterOpt.get();
        _validationBundle = _filter.validate(question, _value, validationLevel);
        LOG.debug("FilterOption created for filter '" + _filterName +  "' on question '" +
            question.getFullName()  + "', valid? " + _validationBundle.getStatus().isValid() +
            ", isDisabled? " + isDisabled );
      }
      else { 
        // not a valid filter name for this question; no need to validate further
        _filter = null;
        _validationBundle = ValidationBundle.builder(validationLevel)
            .addError("Filter name '" + filterName + "' is not valid for search '" + question.getName() + "'.")
            .build();
      }
    }
  }

  public Optional<Filter> getFilter() {
    return Optional.ofNullable(_filter);
  }

  public String getKey() {
    return _filterName;
  }

  public JSONObject getValue() {
    return _value;
  }

  public boolean isDisabled() {
    return _isDisabled;
  }

  public boolean isSetToDefaultValue(SimpleAnswerSpec simpleAnswerSpec) throws WdkModelException {
    return _filter == null ? false : _filter.defaultValueEquals(simpleAnswerSpec, _value);
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  @Override
  public String getName() {
    return getKey();
  }
}
