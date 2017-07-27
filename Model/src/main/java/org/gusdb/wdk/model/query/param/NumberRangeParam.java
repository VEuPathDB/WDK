package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The NumberRangeParam is used to accept a min and a max numerical user
 * input via a web service only.  The raw and stable values are equivalent and
 * expected to be numerical.  The internal value will have any exponentiation removed
 * and for non-integers, rounded according to the number of decimal places specified.
 */
public class NumberRangeParam extends Param {
	
  private Integer numDecimalPlaces = new Integer(1);
  private Double min;
  private Double max;
  private Double step;
  private boolean integer;

  private List<WdkModelText> regexes;
  private String regex;
 
  public NumberRangeParam() {
    regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new NumberRangeParamHandler());
  }

  public NumberRangeParam(NumberRangeParam param) {
    super(param);
    if (param.regexes != null)
      this.regexes = new ArrayList<WdkModelText>();
    this.regex = param.regex;
    this.numDecimalPlaces = param.numDecimalPlaces;
    this.numDecimalPlaces = param.numDecimalPlaces == null ? this.numDecimalPlaces : param.numDecimalPlaces;
    this.integer = param.integer;
    this.min = param.min;
    this.max = param.max;
    this.setStep(param.step);
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
    return new NumberRangeParam(this);
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
   * Verifies that the stringified JSONObject holding the range is properly formatted, matches
   * the supplied regex, adheres to all imposed property restrictions and that the range is
   * properly ordered.
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(java.lang.String)
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkUserException, WdkModelException {
	  
	Double values[] = new Double[2];
	
	// Insure that the JSON Object format is valid.
	try {
	  JSONObject stableValueJson = new JSONObject(stableValue);
	  values[0] = stableValueJson.getDouble("min");
	  values[1] = stableValueJson.getDouble("max");
	}
	catch(JSONException je) {
	  throw new WdkUserException("Could not parse '" + stableValue + "'. "
	  		+ "The range should be is the format {'min':'min value','max':'max value'}");
	}
	
	// Validate each value in the range against regex.  The regex could be a more
	// restrictive test.
	for(Double value : values) {
	  String stringValue = String.valueOf(value);
      if (regex != null && !stringValue.matches(regex)) {
        throw new WdkUserException("value '" + value + "' is invalid. " +
        	  "It must match the regular expression '" + regex + "'");
      }
	}

    // By convention, the first value of the range should be less than the second value.
    if(values[0] >= values[1]) {
      throw new WdkUserException("The miniumum value, '" + values[0] +  "', in the range"
      		+ " must be less than the maximum value, '" + values[1] + "'");
    }
    
    // Verify both ends of the range are integers if such is specified.
    for(Double value : values) {
      if(this.integer && value.doubleValue() % 1 != 0) {
        throw new WdkUserException("value '" + value + "' must be an integer.");
      }
    }  
      
    // Verify the given range in within any required limits
    if(this.min != null && values[0] < new Double(this.min)) {
        throw new WdkUserException("value '" + values[0] + "' must be greater than or equal to '" + this.min + "'" );
    }
    if(this.max != null && values[1] > new Double(this.max)) {
      throw new WdkUserException("value '" + values[1] + "' must be less than or equal to '" + this.max + "'" );
    }
  }
  
  /**
   * Need to alter sql replacement to accommodate fact that internal value
   * is really a JSON string containing min and max ends of range.  The convention is
   * that the minimum value replace $$name.min$$ and the maximum value replace $$name.max$$
   * in the query.
   */
  public String replaceSql(String sql, String internalValue) {
	JSONObject valueJson = new JSONObject(internalValue);
	Double values[] = new Double[2];
	values[0] = valueJson.getDouble("min");
	values[1] = valueJson.getDouble("max");
	String regex = "\\$\\$" + name + ".min\\$\\$";
	String replacedSql = sql.replaceAll(regex, Matcher.quoteReplacement(values[0].toString()));
	regex = "\\$\\$" + name + ".max\\$\\$";
	replacedSql = replacedSql.replaceAll(regex, Matcher.quoteReplacement(values[1].toString()));
	return replacedSql;
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
      JSONObject json = new JSONObject().put("min", getMin()).put("max", getMax());
      defaultValue = json.toString();
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
