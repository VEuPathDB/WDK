package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.RequestParams;

/**
 * A wrapper on a {@link Param} that provides simplified access for consumption by a view
 */
public abstract class ParamBean<T extends Param> {

  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ParamBean.class);

  protected UserBean _userBean;
  protected String _stableValue;
  protected int _truncateLength;
  protected T _param;

  protected Map<String, String> _contextValues;

  public ParamBean(T param) {
    _param = param;
    _truncateLength = Utilities.TRUNCATE_DEFAULT;
    _contextValues = new LinkedHashMap<>();
  }

  public void setUser(UserBean user) {
    _userBean = user;
  }

  public String getName() {
    return _param.getName();
  }

  public String getId() {
    return _param.getId();
  }

  public String getFullName() {
    return _param.getFullName();
  }

  public String getPrompt() {
    return _param.getPrompt();
  }

  public String getHelp() {
    return _param.getHelp();
  }

  public String getVisibleHelp() {
    return _param.getVisibleHelp();
  }

  public String getDefault() {
    return _param.getXmlDefault();
  }

  public boolean getIsReadonly() {
    return _param.isReadonly();
  }

  public boolean getIsAllowEmpty() {
    return _param.isAllowEmpty();
  }

  public String getEmptyValue() {
    return _param.getEmptyValue();
  }

  /**
   * Returns a set of ParamBeans representing params this param depends on
   * @return depended param beans
   * @throws WdkModelException
   */
  public Set<ParamBean<?>> getDependedParams() throws WdkModelException {
    return null;
  }

  /**
   * Returns a comma-delimited list of names of params this param depends on
   * @return depended param names as string
   * @throws WdkModelException
   */
  public String getDependedParamNames() throws WdkModelException {
    return null;
  }

  public boolean getIsVisible() {
    return _param.isVisible();
  }

  public GroupBean getGroup() {
    return new GroupBean(_param.getGroup());
  }

  /**
   * for controller
   */
  public void validate(UserBean user, String rawOrDependentValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    _param.validate(rawOrDependentValue, contextValues);
  }

  /**
   * @throws WdkModelException
   *           if unable to set stable value
   */
  public void setStableValue(String stableValue) throws WdkModelException {
    _stableValue = stableValue;
  }

  public String getStableValue() {
    return _stableValue;
  }

  public Object getRawValue() throws WdkModelException {
    return _param.getRawValue(_userBean.getUser(), _stableValue);
  }

  public String getBriefRawValue() throws WdkModelException {
    Object rawValue = getRawValue();
    return _param.getBriefRawValue(rawValue, _truncateLength);
  }

  public void setTruncateLength(int truncateLength) {
    if (truncateLength >= 0) {
      _truncateLength = truncateLength;
    }
  }

  /**
   * @param user
   * @param stableValue
   * @return
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String getSignature(UserBean user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    return _param.getSignature(user.getUser(), stableValue, contextValues);
  }

  /**
   * @param user
   * @param independentValue
   * @return
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String getStableValue(UserBean user, RequestParams requestParams) throws WdkModelException,
      WdkUserException {
    return _param.getStableValue(user.getUser(), requestParams);
  }

  /**
   * @param user
   * @param rawValue
   * @return
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  public String getStableValue(UserBean user, Object rawValue)
      throws WdkModelException, WdkUserException {
    return _param.toStableValue(user.getUser(), rawValue);
  }

  public UserBean getUser() {
    return _userBean;
  }

  public String getType() {
    return _param.getClass().getSimpleName();
  }

  public Set<String> getAllValues() throws WdkModelException {
    return _param.getAllValues();
  }

  /**
   * @return the contextValues
   */
  public Map<String, String> getContextValues() {
    return _contextValues;
  }

  /**
   * @param contextValues
   *          the contextValues to set
   */
  public void setContextValues(Map<String, String> contextValues) {
    _contextValues = contextValues;
  }

  public void prepareDisplay(UserBean user, RequestParams requestParams)
      throws WdkModelException, WdkUserException {
    _param.prepareDisplay(user.getUser(), requestParams, _contextValues);
  }

  public void prepareDisplay(UserBean user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    _param.prepareDisplay(user.getUser(), requestParams, contextValues);
  }

  public String getDisplayValue() throws WdkModelException {
    return _param.getDisplayValue(_userBean.getUser(), _stableValue, _contextValues);
  }

}
