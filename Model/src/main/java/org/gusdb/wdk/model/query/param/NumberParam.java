package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The NumberParam is used to accept numerical user inputs from a web service only.
 *
 * The author can provide regex to limit the content of user's input. The input will be rejected if regex
 * match fails. Furthermore, you can also define a default regex in the model-config.xml, which will be used
 * by all NumberParams that don't have their own regex defined.
 *
 *
 *         raw value: a raw string;
 *
 *         stable value: same as raw value;
 *
 *         signature: a checksum of the stable value;
 *
 *         internal value: The internal is a string representation of a parsed Double with
 *         exponentiation removed and rounding applied if needed;
 *         If noTranslation is true, the raw value is used without any
 *         change.
 */
public class NumberParam extends Param {

  private Integer _numDecimalPlaces = 1;
  private Double _min;
  private Double _max;
  private Double _increment;
  private boolean _isInteger;

  private List<WdkModelText> _regexes;
  private String _regex;

  public NumberParam() {
    _regexes = new ArrayList<>();

    // register handler
    setHandler(new NumberParamHandler());
  }

  public NumberParam(NumberParam param) {
    super(param);
    if (param._regexes != null) {
      _regexes = new ArrayList<>();
    }
    _regex = param._regex;
    _numDecimalPlaces = param._numDecimalPlaces == null ? _numDecimalPlaces : param._numDecimalPlaces;
    _isInteger = param._isInteger;
    _min = param._min;
    _max = param._max;
    setStep(param._increment);
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public void addRegex(WdkModelText regex) {
    _regexes.add(regex);
  }

  public void setRegex(String regex) {
    _regex = regex;
  }

  public String getRegex() {
    return _regex;
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
      _regex = "[+-]?\\d+(\\.\\d+)?([eE][+-]?\\d+)?";
    }
  }

  @Override
  public Param clone() {
    return new NumberParam(this);
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    // nothing to be added
  }

  /**
   * @inheritDoc
   */
  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues contextParamValues, ValidationLevel level) {

    final String name  = getName();
    final String value = contextParamValues.get(name);
    final Double numericalValue;

    // Insure that the value provided can be converted into a proper number
    try {
      numericalValue = Double.valueOf(name);
    }
    catch (NumberFormatException ex) {
      return contextParamValues.setInvalid(name, "value must be numerical; '" +
          value + "' is invalid.");
    }

    // Insure that the value provided matches the regular expression provided.
    // This could be more restrictive than the number test above.
    if (_regex != null && !value.matches(_regex)) {
      return contextParamValues.setInvalid(name, "value '" + value
          + "' is invalid. It must match the regular expression '"
          + _regex + "'");
    }

    // Verify the value provided is an integer if that property is specified.
    if(_isInteger && numericalValue % 1 != 0) {
      return contextParamValues.setInvalid(name, "value '" + value +
          "' must be an integer.");
    }

    // Verify the value provided is greater than the minimum allowed value, if that property
    // is specified.
    if(_min != null && numericalValue < _min) {
      return contextParamValues.setInvalid(name, "value '" + value
          + "' must be greater than or equal to '" + _min + "'" );
    }

    // Verify the value provided is no greater than the maximum allowed value, if that property
    // is specified.
    if(_max != null && numericalValue > _max) {
      return contextParamValues.setInvalid(name, "value '" + value
          + "' must be less than or equal to '" + _max + "'" );
    }

    return contextParamValues.setValid(name);
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
  public String getBriefRawValue(Object rawValue, int truncateLength) {
    return (String)rawValue;
  }

  public Integer getNumDecimalPlaces() {
    return _numDecimalPlaces;
  }

  public void setNumDecimalPlaces(Integer numDecimalPlaces) {
    _numDecimalPlaces = numDecimalPlaces;
  }

  public Double getMin() {
    return _min;
  }

  public void setMin(Double min) {
    _min = min;
  }

  public Double getMax() {
    return _max;
  }

  public void setMax(Double max) {
    _max = max;
  }

  @Override
  public String getDefault(PartiallyValidatedStableValues stableValues) throws WdkModelException {
    String defaultValue = super.getDefault(stableValues);
    return defaultValue == null || defaultValue.isEmpty()
        ? _min.toString()
        : defaultValue;
  }

  public boolean isInteger() {
    return _isInteger;
  }

  public void setInteger(boolean integer) {
    _isInteger = integer;
  }

  public Double getStep() {
    return _increment;
  }

  public void setStep(Double increment) {
    if(increment == null)
      _increment = _isInteger ? 1 : 0.01;
    else
      _increment = increment;
  }
}
