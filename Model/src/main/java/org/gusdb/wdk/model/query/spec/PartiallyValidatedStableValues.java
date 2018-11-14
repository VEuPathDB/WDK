package org.gusdb.wdk.model.query.spec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AbstractDependentParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.DependentParamInstance;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class PartiallyValidatedStableValues extends HashMap<String,String> {

  private static final long serialVersionUID = 1L;

  public static class ParamValidity {
    private final boolean _isValid;
    private ParamValidity(boolean isValid) {
      _isValid = isValid;
    }
    public boolean isValid() {
      return _isValid;
    }
  }

  private final HashMap<String,Boolean> _validationStatusMap = new HashMap<>();
  private final Map<String,String> _validationErrorMap = new HashMap<>();

  public PartiallyValidatedStableValues(QueryInstanceSpecBuilder specBuilder) {
    // TODO Auto-generated constructor stub
  }

  public boolean hasParamBeenValidated(String paramName) {
    return _validationStatusMap.containsKey(paramName);
  }

  public boolean isParamValid(String paramName) {
    return _validationStatusMap.containsKey(paramName) && _validationStatusMap.get(paramName);
  }

  public boolean isValid() {
    return _validationErrorMap.isEmpty();
  }

  public Map<String,String> getValidationErrors() {
    return _validationErrorMap;
  }

  /** returns true */
  public ParamValidity setValid(String paramName) {
    _validationStatusMap.put(paramName, true);
    return new ParamValidity(true);
  }

  /** returns false */
  public ParamValidity setInvalid(String paramName, String reason) {
    _validationStatusMap.put(paramName, false);
    _validationErrorMap.put(paramName, reason);
    return new ParamValidity(false);
  }

  public ParamValidity getParamValidity(String paramName) {
    return new ParamValidity(isParamValid(paramName));
  }

  @Override
  public Map<String, String> getUnderlyingMap() {
    return _values;
  }
  }


// TODO validate params using the passed query's params
Map<String,Param> params = _query.getParamMap();

// FIXME: This code should live in AnswerParam's validateValue method

// if asked to runnably validate and query has answer params, must check child steps
if (validationLevel.equals(ValidationLevel.RUNNABLE) && _question.getQuery().getAnswerParamCount() > 0) {
  validateChildSteps(_queryInstanceSpec, stepContainer, validation);
}
private void validateChildSteps(QueryInstanceSpec queryInstanceSpec,
    StepContainer stepContainer, ValidationBundleBuilder validation) {
  // make sure stepContainer was provided
  if (stepContainer == null) {
    throw new WdkRuntimeException("Step container cannot be null if validation level is runnable and question has answer params.");
  }
  for (Param param : queryInstanceSpec.) {

  }
}

_validationBundle = ValidationBundle.builder(level).build();

}

/**
 * NOTE: this method is only called from actionland/beans and does not need to be compatible with
 * FilterParamNew (which is only valid in the service)
 * 
 * for reviseStep action, validate all the values, and if it's invalid, substitute it with default. if the
 * value doesn't exist in the map, I will add default into it.
 * 
 * @param contextParamValues
 * @throws WdkModelException
 * @throws WdkUserException
 */
public void fillContextParamValues(User user, Map<String, String> contextParamValues)
    throws WdkModelException, WdkUserException {
  for (Param param : paramMap.values()) {
    if (param instanceof AbstractDependentParam) {
      // for enum/flatVocab params, call a special method to process it
      Map<String, DependentParamInstance> caches = new HashMap<>();
      ((AbstractDependentParam) param).fillContextParamValues(user, contextParamValues, caches);
    }
    else if (!(param instanceof DatasetParam)) {
      // for other params, just fill it with default value;
      // However, we cannot use default for datasetParam, which is just
      // sample, not a valid value (a valid value must be a dataset id)
      if (!contextParamValues.containsKey(param.getName())) {
        contextParamValues.put(param.getName(), param.getDefault());
      }
    }
  }
}

private Map<String, String> fillEmptyValues(Map<String, String> stableValues) throws WdkModelException {
  Map<String, String> newValues = new LinkedHashMap<String, String>(stableValues);
  Map<String, Param> paramMap = _query.getParamMap();

  // iterate through this query's params, filling values
  for (String paramName : paramMap.keySet()) {
    resolveParamValue(paramMap.get(paramName), newValues);
  }
  return newValues;
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
