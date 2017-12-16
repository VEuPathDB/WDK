package org.gusdb.wdk.model.query.param.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;

public class ValidStableValuesFactory {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(ValidStableValuesFactory.class);

  // an AbstractStableValues that asserts its values have been validated
  public static class ValidStableValues extends AbstractStableValues {
    protected ValidStableValues(StableValues initialValues) {
      super(initialValues);
    }
  }

  // a ValidStableValues that asserts it has a complete set of values for its query
  public static class CompleteValidStableValues extends ValidStableValues {
    protected CompleteValidStableValues(StableValues initialValues) {
      super(initialValues);
    }
  }

  // used only for building a valid set
  public static class PartiallyValidatedStableValues extends WriteableStableValues {

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

    private PartiallyValidatedStableValues(User user, Query query) {
      super(query);
      addUser(user);
    }
    private PartiallyValidatedStableValues(User user, StableValues stableValues) {
      super(stableValues);
      addUser(user);
    }
    private void addUser(User user) {
      if (getQuery().getParamMap().get(Utilities.PARAM_USER_ID) != null) {
        // this query requires a user param (i.e. the CURRENT user!); add it, replacing if necessary
        put(Utilities.PARAM_USER_ID, Long.toString(user.getUserId()));
      }
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
  }

  /**
   * Creates a ValidatedParamStableValues object that has parameters populated with defaults and validates it.
   * 
   * @param user
   * @param query
   * @return
   * @throws WdkModelException
   */
  public static CompleteValidStableValues createDefault(User user, Query query) throws WdkModelException {
    PartiallyValidatedStableValues validatedValues = new PartiallyValidatedStableValues(user, query);
    for (Param param : query.getParams()) {
      param.validate(user, validatedValues, true);
    }
    if (validatedValues.isValid()) {
      return new CompleteValidStableValues(validatedValues);
    }
    throw new WdkModelException("At least one default value is invalid." + FormatUtil.NL
         + FormatUtil.prettyPrint(validatedValues.getValidationErrors(), Style.MULTI_LINE));
  }

  /**
   * Creates a ValidatedParamStableValues from a possibly complete set of parameters. The one way the
   * parameters might not be complete is if the query associated with the parameters requires a userId
   * parameter. Once added, if necessary, the object is validated.
   * 
   * @param user
   * @param query
   * @param paramStableValues
   * @param errorOnExtraParams 
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public static CompleteValidStableValues createFromCompleteValues(User user, StableValues unvalidatedValues,
      boolean errorOnExtraParams) throws WdkUserException, WdkModelException {
    PartiallyValidatedStableValues validatedValues = new PartiallyValidatedStableValues(user, unvalidatedValues);
    trimExtraParams(validatedValues, errorOnExtraParams);
    for (Param param : validatedValues.getQuery().getParams()) {
      param.validate(user, validatedValues, false);
    }
    if (validatedValues.isValid()) {
      return new CompleteValidStableValues(validatedValues);
    }
    throw new WdkUserException("Some parameter values are invalid", validatedValues.getValidationErrors());
  }

  private static void trimExtraParams(PartiallyValidatedStableValues incomingValues,
      boolean errorOnExtraParams) throws WdkUserException {
    // take care of extra params
    Set<String> querysParams = incomingValues.getQuery().getParamMap().keySet();
    List<String> extraParamNames = new ArrayList<>();
    for (String paramName : incomingValues.keySet()) {
      if (!querysParams.contains(paramName)) {
        // this is an extra param
        if (errorOnExtraParams) {
          extraParamNames.add(paramName);
        }
        else {
          incomingValues.remove(paramName);
        }
      }
    }
    if (!extraParamNames.isEmpty()) {
      throw new WdkUserException("The following parameters are not part of query '" +
          incomingValues.getQuery().getFullName() + "': ['" + FormatUtil.join(extraParamNames, "', '") + "']");
    }
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
  public static ValidStableValues createFromChangedValue(String changedParamName,
      String changedParamValue, StableValues originalValues)
      throws WdkModelException, WdkUserException {

    //TODO write me!!
    
/* START FROM SERVICE ENDPOINT
    // find all dependencies of the changed param, and remove them from the context
    for (Param dependentParam : changedParam.getAllDependentParams()) contextParamValues.remove(dependentParam.getName());
    changedParam.getStaleDependentParams()
    */
    /*
    String originalParamValue = originalValues.get(changedParamName);
    if (changedParamValue != null && !changedParamValue.equals(originalParamValue)) {
      // pass new obj out via constructor
      return originalValues;
    }
    WriteableStableValues paramStableValues = new WriteableStableValues(originalValues._paramStableValues);
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
    ValidStableValueSet updatedValues = new ValidStableValueSet(user, paramStableValues);
    for (Param dependentParam : changedParam.getDependentParams()) {
      updatedValues.resolveParamValue(dependentParam, updatedValues._paramStableValues);
    }
    changedParam.validate(user, changedParamValue, updatedValues);
    for (Param dependentParam : changedParam.getAllDependentParams()) {
      String dependentValue = updatedValues.get(dependentParam.getName());
      dependentParam.validate(user, dependentValue, updatedValues);
    }
    return updatedValues;*/
  }
/*
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

  protected void resolveParamValue(Param param, WriteableStableValues stableValues) throws WdkModelException {
    String value;
    if (!stableValues.containsKey(param.getName())) {
      // param not provided, determine value
      if (param instanceof AbstractDependentParam && ((AbstractDependentParam) param).isDependentParam()) {
        // special case; must get value of depended param first
        AbstractDependentParam adParam = (AbstractDependentParam) param;
        WriteableStableValues dependedValues = new WriteableStableValues(stableValues.getQuery(), new HashMap<>());
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
*/
}
