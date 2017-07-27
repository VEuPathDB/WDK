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
	
  private Integer numDecimalPlaces = new Integer(1);
  private Double min;
  private Double max;
  private Double step;
  private boolean integer;

  private List<WdkModelText> regexes;
  private String regex;
 
  public NumberParam() {
    regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new NumberParamHandler());
  }

  public NumberParam(NumberParam param) {
    super(param);
    if (param.regexes != null)
      this.regexes = new ArrayList<WdkModelText>();
    this.regex = param.regex;
    this.numDecimalPlaces = param.numDecimalPlaces;
    this.numDecimalPlaces = param.numDecimalPlaces == null ? this.numDecimalPlaces : param.numDecimalPlaces;
    this.integer = param.integer;
    this.min = param.min;
    this.max = param.max;
    this.step = param.getStep();
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public void addRegex(WdkModelText regex) {
    this.regexes.add(regex);
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getRegex() {
    return regex;
  }


  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    return new StringBuilder(super.toString())
      .append("  regex='").append(regex).append("'").append(newline).toString();
  }

  // ///////////////////////////////////////////////////////////////
  // protected methods
  // ///////////////////////////////////////////////////////////////

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);
    if (regex == null)
      regex = model.getModelConfig().getParamRegex();
    if (regex == null) {
      regex = "[+-]?\\d+(\\.\\d+)?([eE][+-]?\\d+)?";
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Param clone() {
    return new NumberParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
   */
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
    if (regex != null && !stableValue.matches(regex)) {
      throw new WdkUserException("value '" + stableValue + "' is invalid. "
         + "It must match the regular expression '" + regex + "'");
    }
    
    // Verify the value provided is an integer if that property is specified.
    if(this.integer && numericalValue.doubleValue() % 1 != 0) {
      throw new WdkUserException("value '" + stableValue + "' must be an integer.");
    }
    
    // Verify the value provided is greater than the minimum allowed value, if that property
    // is specified.
    if(this.min != null && numericalValue.doubleValue() < this.min) {
      throw new WdkUserException("value '" + stableValue + "' must be greater than or equal to '" + this.min + "'" );
    }
    
    // Verify the value provided is no greater than the maximum allowed value, if that property
    // is specified.
    if(this.max != null && numericalValue.doubleValue() > this.max) {
      throw new WdkUserException("value '" + stableValue + "' must be less than or equal to '" + this.max + "'" );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    boolean hasRegex = false;
    for (WdkModelText regex : regexes) {
      if (regex.include(projectId)) {
        if (hasRegex) {
          throw new WdkModelException("The param " + getFullName() + " has more than one regex for project " +
              projectId);
        }
        else {
          this.regex = regex.getText();
          hasRegex = true;
        }
      }
    }
    regexes = null;
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
	return numDecimalPlaces;
  }

  public void setNumDecimalPlaces(Integer numDecimalPlaces) {
	this.numDecimalPlaces = numDecimalPlaces;
  }

  public Double getMin() {
	return min;
  }

  public void setMin(Double min) {
	this.min = min;
  }

  public Double getMax() {
	return max;
  }

  public void setMax(Double max) {
	this.max = max;
  }
  
  @Override
  public void setDefault(String defaultValue) {
	if(defaultValue == null || defaultValue.isEmpty()) {
	  defaultValue = this.min.toString();
	}
	super.setDefault(defaultValue);
  }
  

  public boolean isInteger() {
	return integer;
  }

  public void setInteger(boolean integer) {
	this.integer = integer;
  }
  
  public Double getStep() {
    return step;
  }
  
  public void setStep(Double step) {
	if(step == null) {
	  step = this.integer ? 1 : 0.01;
	}
    this.step = step;
  }

}
