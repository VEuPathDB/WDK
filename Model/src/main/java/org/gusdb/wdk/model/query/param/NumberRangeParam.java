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
 * The NumberRangeParam is used to accept a min and a max numerical user input via a web service only.
 * 
 */
public class NumberRangeParam extends Param {
	
  private Integer numDecimalPlaces = new Integer(1);
  private Long min;
  private Long max;
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

  /*
   * (non-Javadoc)
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
	
	// Remove excess space and thousands separators, if any and validate each value in the
	// range against regex.
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
   * Need to alter sql replacement to accomodate fact that internal value
   * is really a JSON string containing min and max ends of range.
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

  public Long getMin() {
	return min;
  }

  public void setMin(Long min) {
	this.min = min;
  }

  public Long getMax() {
	return max;
  }

  public void setMax(Long max) {
	this.max = max;
  }

  public boolean isInteger() {
	return integer;
  }

  public void setInteger(boolean integer) {
	this.integer = integer;
  }

}
