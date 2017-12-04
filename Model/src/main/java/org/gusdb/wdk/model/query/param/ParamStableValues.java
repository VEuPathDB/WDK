package org.gusdb.wdk.model.query.param;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;

/**
 * Wrapper for a map of parameter stable values.  The underlying map cannot be modified via
 * this objects of this class.
 * @author crisl-adm
 *
 */
public class ParamStableValues {
  protected Query _query;
  protected User _user;
  protected Map<String,String> _paramStableValues;

  protected ParamStableValues(User user, Query query, Map<String,String> paramStableValues) throws WdkUserException, WdkModelException {
    _user = user;
    _query = query;
    _paramStableValues = paramStableValues;
    
    // Add user_id into the parameter stable values if the user_id is among the query's parameter keys and
    // is not already set in the incoming map of parameter values.
    Map<String, Param> params = _query.getParamMap();
    String userKey = Utilities.PARAM_USER_ID;
    if (params.containsKey(userKey) && !_paramStableValues.containsKey(userKey)) {
      _paramStableValues.put(userKey, Long.toString(_user.getUserId()));
    }
    
    // Add any additional default values and any depended parameters
    _paramStableValues = fillEmptyValues();
    
  }
  
  public static ParamStableValues createFromCompleteParamValuesMap(User user, Query query, Map<String,String> paramStableValues) throws WdkUserException, WdkModelException {
    	return new ParamStableValues(user, query, paramStableValues);
  }
  
  public Query getQuery() {
    return _query;
  }

  public String get(String key) {
    return _paramStableValues.get(key);
  }
  
  public Set<String> keySet() {
	return Collections.unmodifiableSet(_paramStableValues.keySet());
  }
  
  protected Map<String, String> fillEmptyValues() throws WdkModelException {
    Map<String, String> newParamStableValues = new LinkedHashMap<String, String>(_paramStableValues);
    Map<String, Param> paramMap = _query.getParamMap();

    // iterate through this query's params, filling values
    for (String paramName : paramMap.keySet()) {
      resolveParamValue(paramMap.get(paramName), newParamStableValues);
    }
    return newParamStableValues;
  }
	  
  private void resolveParamValue(Param param, Map<String, String> stableValues) throws WdkModelException {
    String value;
    if (!stableValues.containsKey(param.getName())) {
      // param not provided, determine value
      if (param instanceof AbstractDependentParam && ((AbstractDependentParam) param).isDependentParam()) {
        // special case; must get value of depended param first
        AbstractDependentParam adParam = (AbstractDependentParam) param;
        Map<String, String> dependedValues = new LinkedHashMap<>();
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
