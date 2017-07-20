package org.gusdb.wdk.model.query.param;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

  private List<WdkModelText> regexes;
  private String regex;
  private String minDate;
  private String maxDate;

  public DateParam() {
    regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new DateParamHandler());
  }

  public DateParam(DateParam param) {
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
  
  @Override
  public String getDefault() {
    return this.getMinDate();
  }

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
    return new DateParam(this);
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
	  
	LocalDate dateValue = null;  
	  
	// Insure that the value provided is formatted as a proper iso1806 date.
    try {
      dateValue = LocalDate.parse(stableValue, DateTimeFormatter.ISO_DATE);
    }
    catch(DateTimeParseException dtpe) {
      throw new WdkUserException("value '" + stableValue + "' cannot be parsed " +
        "as an Iso1806 date.  Make sure the data is in the yyyy-mm-dd format");
    }
	  
	// Insure that the value provided matches the regular expression provided.  This could be
	// more restrictive than the iso1806 date test.
    if(regex != null && !stableValue.matches(regex)) {
      throw new WdkUserException("value '" + stableValue + "' is " +
            "invalid and probably contains illegal characters. " + "It must match the regular expression '" +
            regex + "'");
    }

    // Check minimum allowed date
    if(this.minDate != null &&
     dateValue.isBefore(LocalDate.parse(minDate, DateTimeFormatter.ISO_DATE))) {
   	  throw new WdkUserException("The date '" + stableValue + "' should not be earlier than '" + this.minDate + "'");
    }
    
    // Check maximum allowed date
    if(this.maxDate != null && 
     dateValue.isAfter(LocalDate.parse(maxDate, DateTimeFormatter.ISO_DATE))) {
      throw new WdkUserException("The date '" + stableValue + "' should not be after '" + this.maxDate + "'");
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
    String value = (String) rawValue;
    if (value == null) return value;
    if (value.length() > truncateLength)
      value = value.substring(0, truncateLength) + "...";
    return value;
  }

}
