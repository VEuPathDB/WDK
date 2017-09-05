package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

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

  private Integer _numDecimalPlaces = new Integer(1);
  private Double _min;
  private Double _max;
  private Double _step;
  private boolean _isInteger;

  private List<WdkModelText> _regexes;
  private String _regex;

  public NumberParam() {
    _regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new NumberParamHandler());
  }

  public NumberParam(NumberParam param) {
    super(param);
    if (param._regexes != null)
      _regexes = new ArrayList<WdkModelText>();
    _regex = param._regex;
    _numDecimalPlaces = param._numDecimalPlaces;
    _numDecimalPlaces = param._numDecimalPlaces == null ? _numDecimalPlaces : param._numDecimalPlaces;
    _isInteger = param._isInteger;
    _min = param._min;
    _max = param._max;
    this.setStep(param._step);
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
   * Insure that the value provided by the user conforms to the parameter's requirements
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(java.lang.String)
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkUserException, WdkModelException {

    Double numericalValue = null; 

	// Insure that the value provided can be converted into a proper number
    try {
      numericalValue = Double.valueOf(stableValue);
    }
    catch (NumberFormatException ex) {
      throw new WdkUserException("value must be numerical; '" + stableValue + "' is invalid.");
    }

    // Insure that the value provided matches the regular expression provided.  This could be
    // more restrictive than the number test above.
    if (_regex != null && !stableValue.matches(_regex)) {
      throw new WdkUserException("value '" + stableValue + "' is invalid. "
         + "It must match the regular expression '" + _regex + "'");
    }

    // Verify the value provided is an integer if that property is specified.
    if(_isInteger && numericalValue.doubleValue() % 1 != 0) {
      throw new WdkUserException("value '" + stableValue + "' must be an integer.");
    }

    // Verify the value provided is greater than the minimum allowed value, if that property
    // is specified.
    if(_min != null && numericalValue.doubleValue() < _min) {
      throw new WdkUserException("value '" + stableValue + "' must be greater than or equal to '" + _min + "'" );
    }

    // Verify the value provided is no greater than the maximum allowed value, if that property
    // is specified.
    if(_max != null && numericalValue.doubleValue() > _max) {
      throw new WdkUserException("value '" + stableValue + "' must be less than or equal to '" + _max + "'" );
    }
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
  public String getDefault() throws WdkModelException {
    String defaultValue = super.getDefault();
    if(defaultValue == null || defaultValue.isEmpty()) {
      defaultValue = _min.toString();
    }
    return defaultValue;
  }

  public boolean isInteger() {
    return _isInteger;
  }

  public void setInteger(boolean integer) {
    _isInteger = integer;
  }

  public Double getStep() {
    return _step;
  }

  public void setStep(Double step) {
    if(step == null) {
      step = _isInteger ? 1 : 0.01;
    }
    _step = step;
  }

}
