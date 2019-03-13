package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The NumberRangeParam is used to accept a min and a max numerical user
 * input via a web service only.  The raw and stable values are equivalent and
 * expected to be numerical.  The internal value will have any exponentiation removed
 * and for non-integers, rounded according to the number of decimal places specified.
 */
public class NumberRangeParam extends Param {

  private Integer _numDecimalPlaces = new Integer(1);
  private Double _min;
  private Double _max;
  private Double _increment;
  private boolean _isInteger;

  private List<WdkModelText> _regexes;
  private String _regex;

  public NumberRangeParam() {
    _regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new NumberRangeParamHandler());
  }

  public NumberRangeParam(NumberRangeParam param) {
    super(param);
    if (param._regexes != null) {
      _regexes = new ArrayList<WdkModelText>();
    }
    _regex = param._regex;
    _numDecimalPlaces = param._numDecimalPlaces;
    _numDecimalPlaces = param._numDecimalPlaces == null ? _numDecimalPlaces : param._numDecimalPlaces;
    _isInteger = param._isInteger;
    _min = param._min;
    _max = param._max;
    setIncrement(param._increment);
  }

  /////////////////////////////////////////////////////////////////////
  ///////////// Public properties ////////////////////////////////////
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

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    return new StringBuilder(super.toString())
      .append("  regex='").append(_regex).append("'").append(newline).toString();
  }

  /////////////////////////////////////////////////////////////////
  // protected methods
  /////////////////////////////////////////////////////////////////

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
    return new NumberRangeParam(this);
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    // nothing to be added
  }

  /**
   * Verifies that the stringified JSONObject holding the range is properly
   * formatted, matches the supplied regex, adheres to all imposed property
   * restrictions and that the range is properly ordered.
   */
  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues ctxParamVals, ValidationLevel level) {

    final String name = getName();
    final String value = ctxParamVals.get(name);
    final double min, max;

    // Insure that the JSON Object format is valid.
    try {
      JSONObject stableValueJson = new JSONObject(value);
      min = stableValueJson.getDouble("min");
      max = stableValueJson.getDouble("max");
    }
    catch(JSONException je) {
      return ctxParamVals.setInvalid(name, "Could not parse '" + value + "'. "
        + "The range should be is the format "
        + "{'min':'min value','max':'max value'}");
    }

    // Validate each value in the range against regex.
    if (_regex != null)
      if (!String.valueOf(min).matches(_regex)
          || String.valueOf(max).matches(_regex))
        return ctxParamVals.setInvalid(name, "value '" + value + "' is invalid."
          + " It must match the regular expression '" + _regex + "'");

    // By convention, the first value of the range should be less than or equal
    // to the second value.
    if(min > max)
      return ctxParamVals.setInvalid(name, "The miniumum value, '" + min +  "',"
        + "in the range must be less than the maximum value, '" + max + "'");

    // Verify both ends of the range are integers if such is specified.
    if (_isInteger && (min % 1 != 0 || max % 1 != 0))
      return ctxParamVals.setInvalid(name, "Minimum and maximum values (" + min
          + ", " + max + ") must both be integers");

    // Verify the given range in within any required limits
    if(_min != null && min < _min)
      return ctxParamVals.setInvalid(name, "value '" + min + "' must be "
        + "greater than or equal to '" + _min + "'" );

    if(_max != null && max > _max)
      return ctxParamVals.setInvalid(name, "value '" + max + "' must be less "
        + "than or equal to '" + _max + "'" );

    return ctxParamVals.setValid(name);
  }

  /**
   * Need to alter sql replacement to accommodate fact that internal value
   * is really a JSON string containing min and max ends of range.  The convention is
   * that the minimum value replace $$name.min$$ and the maximum value replace $$name.max$$
   * in the query.
   */
  @Override
  public String replaceSql(String sql, String internalValue) {
    JSONObject valueJson = new JSONObject(internalValue);
    Double values[] = new Double[2];
    values[0] = valueJson.getDouble("min");
    values[1] = valueJson.getDouble("max");
    String regex = "\\$\\$" + _name + ".min\\$\\$";
    String replacedSql = sql.replaceAll(regex, Matcher.quoteReplacement(values[0].toString()));
    regex = "\\$\\$" + _name + ".max\\$\\$";
    replacedSql = replacedSql.replaceAll(regex, Matcher.quoteReplacement(values[1].toString()));
    return replacedSql;
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
    try {
      return (defaultValue == null || defaultValue.isEmpty()) ?
          // if default not provided, default is the entire range
          new JSONObject().put("min", getMin()).put("max", getMax()).toString() :
          // incoming value may be using single quotes around keys; allow, but translate to proper JSON
          new JSONObject(defaultValue).toString();
    }
    catch (JSONException e) {
      throw new WdkModelException("Supplied default value (" + defaultValue + ") is not valid JSON.", e);
    }
  }

  public boolean isInteger() {
    return _isInteger;
  }

  public void setInteger(boolean integer) {
    _isInteger = integer;
  }

  public Double getIncrement() {
    return _increment;
  }

  public void setIncrement(Double increment) {
    if(increment == null) {
      increment = _isInteger ? 1 : 0.01;
    }
    _increment = increment;
  }

}
