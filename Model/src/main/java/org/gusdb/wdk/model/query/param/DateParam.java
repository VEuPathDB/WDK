package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.FormatUtil.STANDARD_DATE_FORMAT;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;

/**
 * The DateParam is strictly a web service parameter
 *
 *
 *         raw value: a date in iso1806 format (yyyy-mm-dd);
 *
 *         stable value: same as raw value;
 *
 *         signature: a checksum of the stable value;
 *
 *         internal value: same as stable value;
 */
public class DateParam extends Param {

  private List<WdkModelText> _regexes;
  private String _regex;
  private String _minDate;
  private String _maxDate;

  public DateParam() {
    _regexes = new ArrayList<>();

    // register handler
    setHandler(new DateParamHandler());
  }

  public DateParam(DateParam param) {
    super(param);
    if (param._regexes != null)
      _regexes = new ArrayList<>();
    _regex = param._regex;
    _minDate = param._minDate;
    _maxDate = param._maxDate;
  }

  /////////////////////////////////////////////////////////////////////
  ///////////// Public properties /////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  public void addRegex(WdkModelText regex) {
    _regexes.add(regex);
  }

  public void setRegex(String regex) {
    _regex = regex;
  }

  public String getRegex() {
    return _regex;
  }

  public String getMinDate() {
    return _minDate;
  }

  public void setMinDate(String minDate) throws WdkModelException {
    if(_maxDate != null) {
      try {
        LocalDate.parse(minDate, STANDARD_DATE_FORMAT);
      }
      catch(DateTimeParseException dtpe) {
        throw new WdkModelException(dtpe);
      }
    }
    _minDate = minDate;
  }

  public String getMaxDate() {
    return _maxDate;
  }

  @Override
  public String getXmlDefault() {
    String defaultValue = super.getXmlDefault();
    if (defaultValue == null || defaultValue.isEmpty()) {
      defaultValue = _minDate;
    }
    return defaultValue;
  }

  public void setMaxDate(String maxDate) throws WdkModelException {
    if(_minDate != null) {
      try {
        LocalDate.parse(_minDate, STANDARD_DATE_FORMAT);
      }
      catch(DateTimeParseException dtpe) {
        throw new WdkModelException(dtpe);
      }
    }
    _maxDate = maxDate;
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    return new StringBuilder(super.toString())
    .append("  regex='").append(_regex).append("'").append(newline).toString();
  }

  // ///////////////////////////////////////////////////////////////
  // protected methods
  // ///////////////////////////////////////////////////////////////

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);
    if (_regex == null)
      _regex = model.getModelConfig().getParamRegex();
    if (_regex == null) {
      _regex = "\\d{4}-\\d{2}-\\d{2}";
    }
  }

  @Override
  public Param clone() {
    return new DateParam(this);
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues contextParamValues, ValidationLevel level) {

    final String name = getName();
    final String value = contextParamValues.get(name);
    final LocalDate dateValue;

    // Ensure that the value provided is formatted as a proper iso1806 date.
    try {
      dateValue = LocalDate.parse(value, STANDARD_DATE_FORMAT);
    }
    catch(DateTimeParseException dtpe) {
      return contextParamValues.setInvalid(name, level, "'" + value + "' must be in yyyy-mm-dd format");
    }

    // Ensure that the value provided matches the regular expression provided.
    // This could be more restrictive than the iso1806 date test.
    if(_regex != null && !value.matches(_regex)) {
      return contextParamValues.setInvalid(name, level, "'" + value + "' has an invalid format (it might contain illegal characters). It must match "
          + "the regular expression '" + _regex + "'");
    }

    // Check minimum allowed date
    if(_minDate != null &&
        dateValue.isBefore(LocalDate.parse(_minDate, STANDARD_DATE_FORMAT))) {
      return contextParamValues.setInvalid(name, level, "'" + value + " 'must not be earlier than '" + _minDate + "'");
    }

    // Check maximum allowed date
    if(_maxDate != null &&
     dateValue.isAfter(LocalDate.parse(_maxDate, STANDARD_DATE_FORMAT))) {
      return contextParamValues.setInvalid(name, level, "'" + value + " 'must not be after '" + _maxDate + "'");
    }

    return contextParamValues.setValid(name, level);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    boolean hasRegex = false;
    for (WdkModelText regex : _regexes) {
      if (regex.include(projectId)) {
        if (hasRegex) {
          throw new WdkModelException("The param " + getFullName() + " has more than one regex for project " +
              projectId);
        }
        else {
          _regex = regex.getText();
          hasRegex = true;
        }
      }
    }
    _regexes = null;
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {
    // do nothing
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    String value = (String) rawValue;
    if (value == null) return value;
    if (value.length() > truncateLength)
      value = value.substring(0, truncateLength) + "...";
    return value;
  }

}
