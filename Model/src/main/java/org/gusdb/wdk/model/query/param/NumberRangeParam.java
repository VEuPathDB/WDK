package org.gusdb.wdk.model.query.param;

import java.math.BigDecimal;
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
 * The NumberRangeParam is used to accept a min and a max numerical user input via a web service only.
 * 
 */
public class NumberRangeParam extends Param {
	
  private Integer precision = new Integer(1);
  private Long min;
  private Long max;
  private boolean integer;
  private Double[] numericalValues = new Double[2];

  private List<WdkModelText> regexes;
  private String regex;
 
  public NumberRangeParam() {
    regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new NumberParamHandler());
  }

  public NumberRangeParam(NumberRangeParam param) {
    super(param);
    if (param.regexes != null)
      this.regexes = new ArrayList<WdkModelText>();
    this.regex = param.regex;
    this.precision = param.precision;
    this.precision = param.precision == null ? this.precision : param.precision;
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
	Double numericalValue = null;
	
    // strip off the comma, if any
    String value = stableValue.replaceAll(",", "");
    String[] stableValues = stableValue.split(":");
    
    // A range contains two items
    if(stableValues.length != 2) {
      throw new WdkUserException("A range must consist of 2 values separated by a colon unlike '" + stableValue + "'");
    }
    
    // Verify both sides of the range are numerical
    try {  
      this.numericalValues[0] = Double.valueOf(stableValues[0].trim());
      this.numericalValues[1] = Double.valueOf(stableValues[1].trim());
    }
    catch (NumberFormatException ex) {
      throw new WdkUserException("Both range values must be numerical; '" + stableValue + "' is invalid.");
    }

    // By convention, the first value of the range should be less than the second value.
    if(this.numericalValues[0] >= this.numericalValues[1]) {
      throw new WdkUserException("The first value in the range must be less than the second value unlike '" + stableValue + "'");
    }
    
    // Validate each value in turn.    
    for(int i = 0 ; i < 2 ; i++) {
      if (regex != null && !stableValues[i].matches(regex)) {
        throw new WdkUserException("value '" + stableValues[i] + "' is " +
              "invalid and probably contains illegal characters. " + "It must match the regular expression '" +
              regex + "'");
      }
      if(this.integer && this.numericalValues[i].doubleValue() % 1 != 0) {
        throw new WdkUserException("value '" + stableValues[i] + "' must be an integer.");
      }
      
      // Insure that precision does not exceed specified limits
      BigDecimal bigDecimal = new BigDecimal(stableValues[i]).stripTrailingZeros();
      int scale = bigDecimal.scale();
      int precision = bigDecimal.precision();    
      if (scale < 0) {
          precision -= scale;
          scale = 0;        
      }
      if(this.precision != null && precision > this.precision) {
        throw new WdkUserException("value '" + stableValues[i] + "' must not have a precision exceeding '" + this.precision + "'");
      }
    }
    if(this.min != null && this.numericalValues[0] < new Double(this.min)) {
        throw new WdkUserException("value '" + stableValues[0] + "' must be greater than or equal to '" + this.min + "'" );
    }
    if(this.max != null && this.numericalValues[1] > new Double(this.max)) {
      throw new WdkUserException("value '" + stableValues[1] + "' must be less than or equal to '" + this.max + "'" );
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

  public Integer getPrecision() {
	return precision;
  }

  public void setPrecision(Integer precision) {
	this.precision = precision;
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
