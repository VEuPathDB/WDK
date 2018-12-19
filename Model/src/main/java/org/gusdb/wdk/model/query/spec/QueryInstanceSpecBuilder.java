package org.gusdb.wdk.model.query.spec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class QueryInstanceSpecBuilder extends ReadOnlyHashMap.Builder<String,String>  {

  public static enum FillStrategy {
    NO_FILL(false, false),
    FILL_PARAM_IF_MISSING(true, false),
    FILL_PARAM_IF_MISSING_OR_INVALID(true, true);

    private final boolean _fillWhenMissing;
    private final boolean _fillWhenInvalid;

    private FillStrategy(boolean fillWhenMissing, boolean fillWhenInvalid) {
      _fillWhenMissing = fillWhenMissing;
      _fillWhenInvalid = fillWhenInvalid;
    }

    public boolean shouldFillWhenMissing() {
      return _fillWhenMissing;
    }

    public boolean shouldFillWhenInvalid() {
      return _fillWhenInvalid;
    }
  }

  private int _assignedWeight = 0;

  QueryInstanceSpecBuilder() {
    super(new LinkedHashMap<>());
  }

  QueryInstanceSpecBuilder(QueryInstanceSpec spec) {
    super(new LinkedHashMap<>(spec.toMap()));
    _assignedWeight = spec.getAssignedWeight();
  }

  /**
   * Builds a "known invalid" spec- basically a container for params when an answer spec's question is
   * unknown or invalid.
   * 
   * @return invalid query instance spec (no validation performed)
   */
  
  public QueryInstanceSpec buildInvalid() {
    return new QueryInstanceSpec(toMap(), _assignedWeight);
  }

  /**
   * Convenience method to create a runnable query instance spec
   * 
   * @param user user to glean any user params and to populate query instance context
   * @param query query for this instance spec
   * @param stepContainer step container used to look up steps for answer params
   * @return
   * @throws WdkModelException 
   */
  public RunnableObj<QueryInstanceSpec> buildRunnable(User user, Query query, StepContainer stepContainer)
      throws WdkModelException {
    return ValidObjectFactory.getRunnable(buildValidated(
        user, query, stepContainer, ValidationLevel.RUNNABLE, FillStrategy.NO_FILL));
  }

  /**
   * Fills any missing parameters in the passed builder and builds a QueryInstanceSpec using the passed
   * validation level.
   * 
   * @param user user to glean any user params and to populate query instance context
   * @param query query for this instance spec
   * @param stepContainer step container used to look up steps for answer params
   * @param validationLevel a level to validate the spec against
   * @param fillStrategy whether to fill in missing param values with defaults
   * @return a built spec
   * @throws WdkModelException if unable to validate (e.g. DB query fails or other runtime exception)
   */
  public QueryInstanceSpec buildValidated(User user, Query query, StepContainer stepContainer,
      ValidationLevel validationLevel, FillStrategy fillStrategy) throws WdkModelException {

    // create a copy of the values in this builder which will be modified before passing to constructor
    Map<String,String> tmpValues = new HashMap<>(toMap());

    // trim off any values supplied that don't apply to this query
    for (String name : tmpValues.keySet()) {
      if (!query.getParamMap().keySet().contains(name)) {
        tmpValues.remove(name);
      }
    }

    // add user_id to the param values if needed
    String userKey = Utilities.PARAM_USER_ID;
    if (query.getParamMap().containsKey(userKey) && !tmpValues.containsKey(userKey)) {
      tmpValues.put(userKey, Long.toString(user.getUserId()));
    }

    PartiallyValidatedStableValues stableValues = new PartiallyValidatedStableValues(user, tmpValues);
    ValidationBundleBuilder validation = ValidationBundle.builder(validationLevel);
    for (Param param : query.getParams()) {
      ParamValidity result = param.validate(stableValues, validationLevel, fillStrategy);
      if (!result.isValid()) {
        validation.addError(param.getName(), result.getMessage());
      }
    }
    return new QueryInstanceSpec(user, query, stableValues, _assignedWeight, validation.build(), stepContainer);
  }

  /**
   * Creates a new spec builder from the passed spec.  The params and weight of the passed
   * spec are copied to this builder; however, the query is not.  The params will be
   * validated against the query passed to buildValidated() (if called), whose result will
   * contain the same query.  In short, the query in the spec passed to this method is lost.
   * 
   * @param spec
   * @return new spec builder with params and weight assigned
   */
  public QueryInstanceSpecBuilder fromQueryInstanceSpec(QueryInstanceSpec spec) {
    putAll(spec.toMap());
    _assignedWeight = spec.getAssignedWeight();
    return this;
  }

  @Override
  public QueryInstanceSpecBuilder put(String key, String value) {
    return (QueryInstanceSpecBuilder)super.put(key, value);
  }

  @Override
  public QueryInstanceSpecBuilder putAll(Map<String,String> values) {
    return (QueryInstanceSpecBuilder)super.putAll(values);
  }

  public QueryInstanceSpecBuilder setAssignedWeight(int assignedWeight) {
    _assignedWeight = assignedWeight;
    return this;
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

}
