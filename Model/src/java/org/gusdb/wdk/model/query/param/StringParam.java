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
 * The StringParam is used to accept user inputs from a input box.
 * 
 * The author can provide regex to limit the content of user's input. The input will be rejected if regex
 * match fails. Furthermore, you can also define a default regex in the model-config.xml, which will be used
 * by all StringParams who don't have their own regex defined.
 * 
 * If the number flag is set to true, only numbers are allowed (integers, floats, or doubles). You can also
 * provide regex to limit the allowed value for numbers.
 * 
 * @author xingao
 * 
 *         raw value: a raw string;
 * 
 *         stable value: same as raw value;
 * 
 *         signature: a checksum of the stable value;
 * 
 *         internal value: if number is true, the internal is a string representation of a parsed Double;
 *         otherwise, quotes are properly applied; If noTranslation is true, the raw value is used without any
 *         change.
 */
public class StringParam extends Param {

  private List<WdkModelText> regexes;
  private String regex;
  private int length = 0;
  /**
     * 
     */
  private boolean number = false;
  private boolean multiLine = false;

  public StringParam() {
    regexes = new ArrayList<WdkModelText>();

    // register handler
    setHandler(new StringParamHandler());
  }

  public StringParam(StringParam param) {
    super(param);
    if (param.regexes != null)
      this.regexes = new ArrayList<WdkModelText>();
    this.regex = param.regex;
    this.length = param.length;
    this.number = param.number;
    this.multiLine = param.multiLine;
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

  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * @param length
   *          the length to set
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * @return the isNumber
   */
  public boolean isNumber() {
    return number;
  }

  /**
   * @param isNumber
   *          the isNumber to set
   */
  public void setNumber(boolean isNumber) {
    this.number = isNumber;
  }

  /**
   * Whether this param will render as a textarea instead of a textbox
   * 
   * @param multiLine
   *          set to true if textarea is desired (default false)
   */
  public void setMultiLine(boolean multiLine) {
    this.multiLine = multiLine;
  }

  /**
   * This property controls the display of the input box. If multiLine is false, a normal single-line input
   * box will be used, and if true, a multi-line text area will be used.
   * 
   * @return
   */
  public boolean getMultiLine() {
    return multiLine;
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    return new StringBuilder(super.toString())
    // .append("  sample='").append(sample).append("'").append(newline)
    .append("  regex='").append(regex).append("'").append(newline).append("  length='").append(length).append(
        "'").append(newline).append("  multiLine='").append(multiLine).append("'").toString();
  }

  // ///////////////////////////////////////////////////////////////
  // protected methods
  // ///////////////////////////////////////////////////////////////

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);
    if (regex == null)
      regex = model.getModelConfig().getParamRegex();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Param clone() {
    return new StringParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra) throws JSONException {
    // nothing to be added
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(java.lang.String)
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkUserException, WdkModelException {
    if (number) {
      try {
        // strip off the comma, if any
        String value = stableValue.replaceAll(",", "");
        Double.valueOf(value);
      }
      catch (NumberFormatException ex) {
        throw new WdkUserException("value must be numerical; '" + stableValue + "' is invalid.");
      }
    }
    if (regex != null && !stableValue.matches(regex)) {
      if (stableValue.equals("*"))
        throw new WdkUserException("value '" + stableValue +
            "' cannot be used on its own; it needs to be part of a word.");
      else
        throw new WdkUserException("value '" + stableValue + "' is " +
            "invalid and probably contains illegal characters. " + "It must match the regular expression '" +
            regex + "'");
    }
    if (length != 0 && stableValue.length() > length)
      throw new WdkUserException("value cannot be longer than " + length + " characters (it is " +
          stableValue.length() + ").");
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
  protected void applySuggection(ParamSuggestion suggest) {
    // do nothing
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    String value = (String) rawValue;
    if (value.length() > truncateLength)
      value = value.substring(0, truncateLength) + "...";
    return value;
  }

}
