package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.ParamValuesInvalidException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;

/**
 * Validated version of the ParamStableValues class.  The validation happens in the
 * constructor which is called indirectly from a factory method.
 * @author crisl-adm
 *
 */
public class ValidatedParamStableValues extends ParamStableValues {
  
  private static final Logger LOG = Logger.getLogger(ValidatedParamStableValues.class);
  
  private ValidatedParamStableValues(User user, Query query, Map<String,String> paramStableValues) throws WdkUserException, WdkModelException {
	super(user, query, paramStableValues);
    validateParamStableValues();
  }

  public static ValidatedParamStableValues createFromCompleteParamValuesMap(User user, Query query, Map<String,String> paramStableValues) throws WdkUserException, WdkModelException {
    	return new ValidatedParamStableValues(user, query, paramStableValues);
  }
  
  private void validateParamStableValues() throws WdkUserException, WdkModelException {
    Map<String, Param> params = _query.getParamMap();
    Map<String, String> errors = null;

    // then check that all params have supplied values
    for(String paramName : _paramStableValues.keySet()) {
      String errMsg = null;
      String dependentValue = _paramStableValues.get(paramName);
      String prompt = paramName;
      try {
        if (!params.containsKey(paramName)) {
          // LOG.warn("The parameter '" + paramName + "' doesn't exist in query " + _query.getFullName());
          continue;
        }

        Param param = params.get(paramName);
        prompt = param.getPrompt();

        // validate param
        param.validate(_user, dependentValue, _paramStableValues);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        errMsg = ex.getMessage();
        if (errMsg == null) errMsg = ex.getClass().getName();
      }
      if (errMsg != null) {
        if (errors == null) errors = new LinkedHashMap<String, String>();
        errors.put(prompt, errMsg);
      }
    }
    if (errors != null) {
      WdkUserException ex = new ParamValuesInvalidException("In query " + _query.getFullName() + " some of the input parameters are invalid or missing.", errors);
      LOG.error(ex);
      throw ex;
    }
  }

}
