package org.gusdb.wdk.model.query.spec;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class PartiallyValidatedStableValues extends HashMap<String,String> {

  public static class ParamValidity {

    private final boolean _isValid;
    private final ValidationLevel _level;
    private final String _errorMessage;

    private ParamValidity(boolean isValid, ValidationLevel level, String errorMessage) {
      _isValid = isValid;
      _level = level;
      _errorMessage = errorMessage;
    }

    public boolean isValid()          { return _isValid; }
    public ValidationLevel getLevel() { return _level; }
    public String getMessage()        { return _errorMessage; }

  }

  private final User _requestingUser;
  private final StepContainer _stepContainer;
  private final Map<String, ParamValidity> _validationStatusMap = new HashMap<>();

  PartiallyValidatedStableValues(User requestingUser, Map<String,String> initialValues, StepContainer stepContainer) {
    _requestingUser = requestingUser;
    _stepContainer = stepContainer;
    putAll(initialValues);
  }

  public User getRequestingUser() {
    return _requestingUser;
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

  public ParamValidity setValid(String paramName, ValidationLevel level) {
    ParamValidity validity = new ParamValidity(true, level, null);
    _validationStatusMap.put(paramName, validity);
    return validity;
  }

  public ParamValidity setInvalid(String paramName, ValidationLevel level, String reason) {
    ParamValidity validity = new ParamValidity(false, level, reason);
    _validationStatusMap.put(paramName, validity);
    return validity;
  }
}
