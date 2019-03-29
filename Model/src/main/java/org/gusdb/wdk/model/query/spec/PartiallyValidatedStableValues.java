package org.gusdb.wdk.model.query.spec;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class PartiallyValidatedStableValues extends HashMap<String,String> {

  public static class ParamValidity {

    private final boolean _isValid;
    private final String _errorMessage;

    private ParamValidity(boolean isValid, String errorMessage) {
      _isValid = isValid;
      _errorMessage = errorMessage;
    }

    public boolean isValid()   { return _isValid; }
    public String getMessage() { return _errorMessage; }
  }

  private final User _user;
  private final StepContainer _stepContainer;
  private final Map<String, ParamValidity> _validationStatusMap = new HashMap<>();

  PartiallyValidatedStableValues(User user, Map<String,String> initialValues, StepContainer stepContainer) {
    _user = user;
    _stepContainer = stepContainer;
    putAll(initialValues);
  }

  public User getUser() {
    return _user;
  }

  public StepContainer getStepContainer() {
    return _stepContainer;
  }

  public boolean hasParamBeenValidated(String paramName) {
    return _validationStatusMap.containsKey(paramName);
  }

  public boolean isParamValid(String paramName) {
    return _validationStatusMap.containsKey(paramName) && _validationStatusMap.get(paramName).isValid();
  }

  public ParamValidity getParamValidity(String paramName) {
    return _validationStatusMap.get(paramName);
  }

  public Map<String,ParamValidity> getValidationStatusMap() {
    return _validationStatusMap;
  }

  public ParamValidity setValid(String paramName) {
    ParamValidity validity = new ParamValidity(true, null);
    _validationStatusMap.put(paramName, validity);
    return validity;
  }

  public ParamValidity setInvalid(String paramName, String reason) {
    ParamValidity validity = new ParamValidity(false, reason);
    _validationStatusMap.put(paramName, validity);
    return validity;
  }
}
