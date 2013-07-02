package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;

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
 * The author can provide regex to limit the content of user's input. The input
 * will be rejected if regex match fails. Furthermore, you can also define a
 * default regex in the model-config.xml, which will be used by all StringParams
 * who don't have their own regex defined.
 * 
 * If the number flag is set to true, only numbers are allowed (integers,
 * floats, or doubles). You can also provide regex to limit the allowed value
 * for numbers.
 * 
 * @author xingao
 * 
 *         raw data: same as internal data, a raw string;
 * 
 *         user-dependent data: same as user-independent data, can be either a
 *         raw string or a compressed checksum;
 * 
 *         user-independent data: same as user-dependent data;
 * 
 *         internal data: similar to raw data, but the single quotes are
 *         escaped, and the outer quotes are added if necessary;
 */
public class StringParam extends Param {

  private List<WdkModelText> regexes = new ArrayList<WdkModelText>();
  private String regex;
  private int length = 0;
  /**
     * 
     */
  private boolean number = false;
  private boolean multiLine = false;

  public StringParam() {}

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
   * This property controls the display of the input box. If multiLine is false,
   * a normal single-line input box will be used, and if true, a multi-line text
   * area will be used.
   * 
   * @return
   */
  public boolean getMultiLine() {
    return multiLine;
  }

  public String toString() {
    String newline = System.getProperty("line.separator");
    return new StringBuilder(super.toString())
      //.append("  sample='").append(sample).append("'").append(newline)
      .append("  regex='").append(regex).append("'").append(newline)
      .append("  length='").append(length).append("'").append(newline)
      .append("  multiLine='").append(multiLine).append("'")
      .toString();
  }

  // ///////////////////////////////////////////////////////////////
  // protected methods
  // ///////////////////////////////////////////////////////////////

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
  public Param clone() {
    return new StringParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra)
      throws JSONException {
    // nothing to be added
  }

  /**
   * the dependent value is the same as the independent value
   * 
   * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  @Override
  public String dependentValueToIndependentValue(User user,
      String dependentValue) {
    return dependentValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
   * (java.lang.String)
   */
  @Override
  public String dependentValueToInternalValue(User user, String dependentValue)
      throws WdkModelException {
    String rawValue = decompressValue(dependentValue);
    if (rawValue == null || rawValue.length() == 0)
      rawValue = emptyValue;
    if (isNoTranslation())
      return rawValue;

    rawValue = rawValue.replaceAll("'", "''");
    if (!number)
      rawValue = "'" + rawValue + "'";
    return rawValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(java
   * .lang.String)
   */
  @Override
  public String dependentValueToRawValue(User user, String dependentValue)
      throws WdkModelException {
    return decompressValue(dependentValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(java
   * .lang.String)
   */
  @Override
  public String rawOrDependentValueToDependentValue(User user, String rawValue)
      throws WdkModelException {
    return compressValue(rawValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(java.lang.String)
   */
  @Override
  protected void validateValue(User user, String dependentValue)
      throws WdkUserException, WdkModelException {
    String rawValue = decompressValue(dependentValue);
    if (number) {
      try {
        // strip off the comma, if any
        String value = rawValue.replaceAll(",", "");
        Double.valueOf(value);
      } catch (NumberFormatException ex) {
        throw new WdkUserException("stringParam " + getFullName()
            + " is declared as a number, but the Value '" + rawValue
            + "' is invalid number format.");
      }
    }
    if (regex != null && !rawValue.matches(regex)) {
      if (rawValue.equals("*"))
        throw new WdkUserException("stringParam " + getFullName() + ": value '"
            + rawValue
            + "' cannot be used on its own, it needs to be part of a word.");
      else
        throw new WdkUserException("stringParam " + getFullName() + " value '"
            + rawValue + "' does not match regular " + "expression '" + regex
            + "'");
    }
    if (length != 0 && rawValue.length() > length)
      throw new WdkModelException("stringParam " + getFullName()
          + " value cannot be longer than " + length + " characters."
          + " (It is " + rawValue.length() + ".)");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    boolean hasRegex = false;
    for (WdkModelText regex : regexes) {
      if (regex.include(projectId)) {
        if (hasRegex) {
          throw new WdkModelException("The param " + getFullName()
              + " has more than one regex for project " + projectId);
        } else {
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

}
