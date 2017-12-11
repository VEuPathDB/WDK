package org.gusdb.wdk.model.query.param;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.ParamValuesInvalidException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;

// TODO - CWL Verify
public class ValidatedParamStableValues {

  private static final Logger LOG = Logger.getLogger(ValidatedParamStableValues.class);

  private final User _user;
  private ParamStableValues _paramStableValues;

  private ValidatedParamStableValues(User user, ParamStableValues paramStableValues) {
    _user = user;
    _paramStableValues = paramStableValues;
  }

  /**
   * Creates a ValidatedParamStableValues object that has parameters populated with defaults and validates it.
   * 
   * @param user
   * @param query
   * @return
   * @throws WdkModelException
   */
  public static ValidatedParamStableValues createDefault(User user, Query query)
      throws WdkModelException, WdkUserException {
    ParamStableValues paramStableValues = new ParamStableValues(query, new HashMap<String, String>());
    if (query.getParamMap().get(Utilities.PARAM_USER_ID) != null) {
      paramStableValues.put(Utilities.PARAM_USER_ID, Long.toString(user.getUserId()));
    }
    ValidatedParamStableValues defaultValues = new ValidatedParamStableValues(user, paramStableValues);
    defaultValues.fillEmptyValues();
    defaultValues.validate();
    return defaultValues;
  }

  /**
   * Creates a ValidatedParamStableValues object that copies an existing ValidatedParamStableValues with a
   * value change to a single parameter. Any dependent parameters affected by the change are altered as needed
   * and finally the object is validated. and re-validates it.
   * 
   * @param changedParamName
   * @param changedParamValue
   * @param originalValues
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static ValidatedParamStableValues createFromChangedValue(String changedParamName,
      String changedParamValue, ValidatedParamStableValues originalValues)
      throws WdkModelException, WdkUserException {
    String originalParamValue = originalValues.get(changedParamName);
    if (changedParamValue != null && !changedParamValue.equals(originalParamValue)) {
      // pass new obj out via constructor
      return originalValues;
    }
    ParamStableValues paramStableValues = new ParamStableValues(originalValues._paramStableValues);
    User user = originalValues._user;
    Map<String, Param> paramMap = paramStableValues.getQuery().getParamMap();
    Param changedParam = paramMap.get(changedParamName);
    if (changedParam == null) {
      throw new WdkUserException("Query: " + paramStableValues.getQuery().getFullName() +
          " does not have a parameter with the name '" + changedParamName + "'");
    }
    paramStableValues.put(changedParamName, changedParamValue);

    // find all dependencies of the changed param, and remove them
    for (Param dependentParam : changedParam.getAllDependentParams()) {
      paramStableValues.remove(dependentParam.getName());
    }
    ValidatedParamStableValues updatedValues = new ValidatedParamStableValues(user, paramStableValues);
    for (Param dependentParam : changedParam.getDependentParams()) {
      updatedValues.resolveParamValue(dependentParam, updatedValues._paramStableValues);
    }
    changedParam.validate(user, changedParamValue, updatedValues);
    for (Param dependentParam : changedParam.getAllDependentParams()) {
      String dependentValue = updatedValues.get(dependentParam.getName());
      dependentParam.validate(user, dependentValue, updatedValues);
    }
    return updatedValues;
  }

  /**
   * Creates a ValidatedParamStableValues from a possibly complete set of parameters. The one way the
   * parameters might not be complete is if the query associated with the parameters requires a userId
   * parameter. Once added, if necessary, the object is validated.
   * 
   * @param user
   * @param query
   * @param paramStableValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public static ValidatedParamStableValues createFromCompleteValues(User user,
      ParamStableValues paramStableValues) throws WdkUserException, WdkModelException {
    ValidatedParamStableValues completeValues = new ValidatedParamStableValues(user, paramStableValues);
    Map<String, Param> paramMap = completeValues._paramStableValues.getQuery().getParamMap();
    if (paramMap.containsKey(Utilities.PARAM_USER_ID)) {
      completeValues._paramStableValues.put(Utilities.PARAM_USER_ID, Long.toString(user.getUserId()));
    }
    completeValues.validate();
    return completeValues;
  }

  public String get(String key) {
    return _paramStableValues.get(key);
  }

  public boolean isEmpty() {
    return _paramStableValues.isEmpty();
  }

  public Set<String> keySet() {
    return Collections.unmodifiableSet(_paramStableValues.keySet());
  }

  public String prettyPrint() {
    return FormatUtil.prettyPrint(_paramStableValues, Style.SINGLE_LINE);
  }

  public int size() {
    return _paramStableValues.size();
  }

  public boolean containsKey(Object key) {
    return _paramStableValues.containsKey(key);
  }

  protected void fillEmptyValues() throws WdkModelException {
    Map<String, Param> paramMap = _paramStableValues.getQuery().getParamMap();
    // iterate through this query's params, filling values
    for (Entry<String, Param> entry : paramMap.entrySet()) {
      resolveParamValue(entry.getValue(), _paramStableValues);
    }
  }

  protected void validate() throws WdkUserException, WdkModelException {
    Query query = _paramStableValues.getQuery();
    Map<String, Param> params = query.getParamMap();
    Map<String, String> errors = null;

    for (String paramName : _paramStableValues.keySet()) {
      if (!params.containsKey(paramName)) {
        // LOG.warn("The parameter '" + paramName + "' doesn't exist in query " + _query.getFullName());
        continue;
      }
      Param param = params.get(paramName);
      String errMsg = validate(param);
      if (errMsg != null) {
        if (errors == null)
          errors = new LinkedHashMap<String, String>();
        errors.put(param.getPrompt(), errMsg);
      }
    }
    if (errors != null) {
      WdkUserException ex = new ParamValuesInvalidException(
          "In query " + query.getFullName() + " some of the input parameters are invalid or missing.",
          errors);
      LOG.error(ex);
      throw ex;
    }
  }

  protected String validate(Param param) {
    String errMsg = null;
    try {
      String value = _paramStableValues.get(param.getName());
      param.validate(_user, value, this);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      errMsg = ex.getMessage();
      if (errMsg == null)
        errMsg = ex.getClass().getName();
    }
    return errMsg;
  }

  protected void resolveParamValue(Param param, ParamStableValues stableValues) throws WdkModelException {
    String value;
    if (!stableValues.containsKey(param.getName())) {
      // param not provided, determine value
      if (param instanceof AbstractDependentParam && ((AbstractDependentParam) param).isDependentParam()) {
        // special case; must get value of depended param first
        AbstractDependentParam adParam = (AbstractDependentParam) param;
        ParamStableValues dependedValues = new ParamStableValues(stableValues.getQuery(), new HashMap<>());
        for (Param dependedParam : adParam.getDependedParams()) {
          resolveParamValue(dependedParam, stableValues);
          String dependedName = dependedParam.getName();
          dependedValues.put(dependedName, stableValues.get(dependedName));
        }
        value = adParam.getDefault(_user, dependedValues);
      }
      else {
        value = param.getDefault();
      }
    }
    else { // param provided, but it can be empty
      value = stableValues.get(param.getName());
      if (value == null || value.length() == 0) {
        value = param.isAllowEmpty() ? param.getEmptyValue() : null;
      }
    }
    stableValues.put(param.getName(), value);
  }

}
