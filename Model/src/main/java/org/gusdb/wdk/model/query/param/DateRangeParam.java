package org.gusdb.wdk.model.query.param;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * The DateRangeParam is strictly a web service parameter
 * 
 * 
 *         raw value: a stringified json object containing a min and max date,
 *         both in iso1806 format (yyyy-mm-dd);
 * 
 *         stable value: same as raw value;
 * 
 *         signature: a checksum of the stable value;
 * 
 *         internal value: same as stable value;
 */
public class DateRangeParam extends Param {

  private List<WdkModelText> regexes;
  private String regex;
  private String minDate;
  private String maxDate;

  public DateRangeParam() {
    regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new DateRangeParamHandler());
  }

  public DateRangeParam(DateRangeParam param) {
    super(param);
    if (param.regexes != null)
      this.regexes = new ArrayList<WdkModelText>();
    this.regex = param.regex;
    this.minDate = param.minDate;
    this.maxDate = param.maxDate;
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

  public String getMinDate() {
    return minDate;
  }

  /**
   * Setter for min date that includes a check to insure that the
   * model's min date is in proper format
   * @param minDate
   * @throws WdkModelException
   */
  public void setMinDate(String minDate) throws WdkModelException {
	if(maxDate != null) {  
	  try {  
        LocalDate.parse(minDate, DateTimeFormatter.ISO_DATE);
	  }
	  catch(DateTimeParseException dtpe) {
	    throw new WdkModelException(dtpe);
	  }
	}
	this.minDate = minDate;
  }

  public String getMaxDate() {
    return this.maxDate;
  }

  /**
   * Setter for max date that includes a check to insure that the
   * model's max date is in proper format
   * @param maxDate
   * @throws WdkModelException
   */
  public void setMaxDate(String maxDate) throws WdkModelException {
	if(minDate != null) { 
	  try {  
	    LocalDate.parse(minDate, DateTimeFormatter.ISO_DATE);
	  }
	  catch(DateTimeParseException dtpe) {
	    throw new WdkModelException(dtpe);
	  }
	}  
	this.maxDate = maxDate;
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
      regex = "\\d{4}-\\d{2}-\\d{2}";
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Param clone() {
    return new DateRangeParam(this);
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
	
	LocalDate values[] = new LocalDate[2];
	JSONObject stableValueJson = null;
	
	// Insure that the JSON Object format is valid.
	try {
	  stableValueJson = new JSONObject(stableValue);
	  values[0] = LocalDate.parse(stableValueJson.getString("min"), DateTimeFormatter.ISO_DATE);
	  values[1] = LocalDate.parse(stableValueJson.getString("max"), DateTimeFormatter.ISO_DATE);
	}
	catch(JSONException je) {
	  throw new WdkUserException("Could not parse '" + stableValue + "'. "
	  		+ "The range should be is the format {'min':'min value','max':'max value'}");
	}
	  
	// Validate each value in the range against regex.
	if(regex != null) {
     if(!stableValueJson.getString("min").matches(regex)) {
       throw new WdkUserException("value '" + stableValueJson.getString("min") + "' is invalid. " +
         "It must match the regular expression '" + regex + "'");
     }
 	 if(!stableValueJson.getString("max").matches(regex)) {
       throw new WdkUserException("value '" + stableValueJson.getString("max") + "' is invalid. " +
         "It must match the regular expression '" + regex + "'");
      }
	}
	
	if(!values[0].isBefore(values[1])) {
	  throw new WdkUserException("The minimum date '" + values[0] + "' should " +
	    "come before the maximum date '" + values[1] + "'");
	}

    // Check minimum allowed date
    if(this.minDate != null &&
     values[0].isBefore(LocalDate.parse(minDate, DateTimeFormatter.ISO_DATE))) {
   	  throw new WdkUserException("The date '" + values[0] + "' should not be earlier than '" + this.minDate + "'");
    }
    
    // Check maximum allowed date
    if(this.maxDate != null && 
     values[1].isAfter(LocalDate.parse(maxDate, DateTimeFormatter.ISO_DATE))) {
      throw new WdkUserException("The date '" + values[1] + "' should not be after '" + this.maxDate + "'");
    }
    
  }
  
  /**
   * Need to alter sql replacement to accommodate fact that internal value
   * is really a JSON string containing min and max ends of range.
   */
  public String replaceSql(String sql, String internalValue) {
	JSONObject valueJson = new JSONObject(internalValue);
	LocalDate values[] = new LocalDate[2];
	values[0] = LocalDate.parse(valueJson.getString("min"), DateTimeFormatter.ISO_DATE);
	values[1] = LocalDate.parse(valueJson.getString("max"), DateTimeFormatter.ISO_DATE);
	String regex = "\\$\\$" + name + ".min\\$\\$";
	String replacedSql = sql.replaceAll(regex, Matcher.quoteReplacement("'" + values[0].toString() + "'"));
	regex = "\\$\\$" + name + ".max\\$\\$";
	replacedSql = replacedSql.replaceAll(regex, Matcher.quoteReplacement("'" + values[1].toString() + "'"));
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
    String value = (String) rawValue;
    if (value == null) return value;
    if (value.length() > truncateLength)
      value = value.substring(0, truncateLength) + "...";
    return value;
  }

}
