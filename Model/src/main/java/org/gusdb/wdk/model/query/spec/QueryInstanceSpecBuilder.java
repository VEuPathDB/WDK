package org.gusdb.wdk.model.query.spec;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class QueryInstanceSpecBuilder extends ReadOnlyHashMap.Builder<String,String>  {

  public static enum FillStrategy {
    NO_FILL(false),
    FILL_PARAM_IF_MISSING(true);

    private final boolean _fill;

    private FillStrategy(boolean fill) {
      _fill = fill;
    }

    public boolean shouldFill() {
      return _fill;
    }
  }

  private int _assignedWeight = 0;

  QueryInstanceSpecBuilder() {
    super(new LinkedHashMap<>());
  }

  public QueryInstanceSpecBuilder(QueryInstanceSpec spec) {
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
   */
  public RunnableObj<QueryInstanceSpec> buildRunnable(User user, Query query, StepContainer stepContainer) {
    return ValidObjectFactory.getRunnable(buildValidated(user, query, stepContainer, ValidationLevel.RUNNABLE, FillStrategy.NO_FILL));
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
   */
  public QueryInstanceSpec buildValidated(User user, Query query, StepContainer stepContainer,
      ValidationLevel validationLevel, FillStrategy fillStrategy) {
    // add user_id to the param values if needed
    String userKey = Utilities.PARAM_USER_ID;
    if (query.getParamMap().containsKey(userKey) && !containsKey(userKey)) {
      put(userKey, Long.toString(user.getUserId()));
    }

    PartiallyValidatedStableValues tmpValues = new PartiallyValidatedStableValues(this);
    for (Param param : query.getParams()) {
      param.validate(tmpValues, fillStrategy);
    }
    return new QueryInstanceSpec(toMap(), _assignedWeight, query, level, stepContainer);
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
