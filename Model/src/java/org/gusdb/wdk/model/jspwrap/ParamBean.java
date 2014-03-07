package org.gusdb.wdk.model.jspwrap;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.RequestParams;

/**
 * A wrapper on a {@link Param} that provides simplified access for consumption
 * by a view
 */
public abstract class ParamBean<T extends Param> {

  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ParamBean.class.getName());

  protected UserBean user;
  protected String stableValue;
  protected int truncateLength;
  protected T param;

  protected Map<String, String> contextValues;

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

  /**
   * @throws WdkModelException if unable to set stable value 
   */
  public void setStableValue(String stabletValue) throws WdkModelException {
    this.stableValue = stabletValue;
  }
  
  public String getStableValue() {
    return stableValue;
  }

  public Object getRawValue() throws WdkModelException {
    return param.getRawValue(user.getUser(), stableValue, contextValues);
  }

  public String getBriefRawValue() throws WdkModelException {
    Object rawValue = getRawValue();
    return param.getBriefRawValue(rawValue, truncateLength);
  }

  public void setTruncateLength(int truncateLength) {
    if (truncateLength >= 0) {
      this.truncateLength = truncateLength;
    }
  }

  /**
   * @param user
   * @param stableValue
   * @return
   * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String getSignature(UserBean user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    return param.getSignature(user.getUser(), stableValue, contextValues);
  }

  /**
   * @param user
   * @param independentValue
   * @return
   * @throws WdkUserException 
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String getStableValue(UserBean user, RequestParams requestParams)
      throws WdkModelException, WdkUserException {
    return param.getStableValue(user.getUser(), requestParams);
  }

  /**
   * @param user
   * @param rawValue
   * @return
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String getStableValue(UserBean user, Object rawValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    return param.getStableValue(user.getUser(), rawValue, contextValues);
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

  /**
   * @return the contextValues
   */
  public Map<String, String> getContextValues() {
    return contextValues;
  }

  /**
   * @param contextValues
   *          the contextValues to set
   */
  public void setContextValues(Map<String, String> contextValues) {
    this.contextValues = contextValues;
  }

  public void prepareDisplay(UserBean user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    param.prepareDisplay(user.getUser(), requestParams, contextValues);
  }
  
  
}
