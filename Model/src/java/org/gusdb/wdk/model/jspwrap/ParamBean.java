package org.gusdb.wdk.model.jspwrap;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;

/**
 * A wrapper on a {@link Param} that provides simplified access for consumption
 * by a view
 */
public abstract class ParamBean<T extends Param> {

  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ParamBean.class.getName());

  protected UserBean user;
  protected String dependentValue;
  protected int truncateLength;
  protected T param;

  public ParamBean(T param) {
    this.param = param;
    truncateLength = Utilities.TRUNCATE_DEFAULT;
  }

  public void setUser(UserBean user) {
    this.user = user;
  }

  public String getName() {
    return param.getName();
  }

  public String getId() {
    return param.getId();
  }

  public String getFullName() {
    return param.getFullName();
  }

  public String getPrompt() {
    return param.getPrompt();
  }

  public String getHelp() {
    return param.getHelp();
  }

  public String getDefault() throws WdkModelException {
    return param.getDefault();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#isReadonly()
   */
  public boolean getIsReadonly() {
    return this.param.isReadonly();
  }

  public boolean getIsAllowEmpty() {
    return this.param.isAllowEmpty();
  }

  public String getEmptyValue() {
    return this.param.getEmptyValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#isVisible()
   */
  public boolean getIsVisible() {
    return this.param.isVisible();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.Param#getGroup()
   */
  public GroupBean getGroup() {
    return new GroupBean(param.getGroup());
  }

  /**
   * for controller
   */
  public void validate(UserBean user, String rawOrDependentValue,
      Map<String, String> contextValues) throws WdkModelException,
      WdkUserException {
    param.validate(user.getUser(), rawOrDependentValue, contextValues);
  }

  public void setDependentValue(String dependentValue) {
    this.dependentValue = dependentValue;
  }

  public String getRawValue() throws WdkModelException {
    return param.dependentValueToRawValue(user.getUser(), dependentValue);
  }

  public String getBriefRawValue() throws WdkModelException {
    String rawValue = getRawValue();
    if (rawValue != null) {
      rawValue = rawValue.replaceAll("\\,", ", ");
      if (rawValue.length() > truncateLength)
        rawValue = rawValue.substring(0, truncateLength) + "...";
    }
    return rawValue;
  }

  public void setTruncateLength(int truncateLength) {
    if (truncateLength >= 0) {
      this.truncateLength = truncateLength;
    }
  }

  /**
   * @param user
   * @param dependentValue
   * @return
   * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String dependentValueToIndependentValue(UserBean user,
      String dependentValue) throws WdkModelException {
    return param.dependentValueToIndependentValue(user.getUser(),
        dependentValue);
  }

  /**
   * @param user
   * @param independentValue
   * @return
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String dependentValueToRawValue(UserBean user, String dependentValue)
      throws WdkModelException {
    return param.dependentValueToRawValue(user.getUser(), dependentValue);
  }

  /**
   * @param user
   * @param rawValue
   * @return
   * @see org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String rawOrDependentValueToDependentValue(UserBean user,
      String rawValue) throws WdkModelException {
    return param.rawOrDependentValueToDependentValue(user.getUser(), rawValue);
  }

  public UserBean getUser() {
    return this.user;
  }

  public String getType() {
    return param.getClass().getSimpleName();
  }

  public Set<String> getAllValues() throws WdkModelException {
    return param.getAllValues();
  }
}
