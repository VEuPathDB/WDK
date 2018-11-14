package org.gusdb.wdk.model.query.spec;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class QueryInstanceSpec extends ReadOnlyHashMap<String,String> implements Validateable {

  @SuppressWarnings("unchecked")
  public static QueryInstanceSpecBuilder builder() {
    return new QueryInstanceSpecBuilder();
  }

  public static QueryInstanceSpecBuilder builder(QueryInstanceSpec spec) {
    return new QueryInstanceSpecBuilder(spec);
  }

  // only applied to leaf steps, user-defined
  // during booleans, weights of records are modified (per boolean-specific logic, see BooleanQuery)
  private final int _assignedWeight;

  private final User _user;
  private final Query _query;
  private final StepContainer _stepContainer;
  private final ValidationBundle _validationBundle;

  QueryInstanceSpec(User user, Query query, Map<String, String> paramValues, int assignedWeight,
      ValidationBundle validationBundle, StepContainer stepContainer) {
    super(paramValues);
    _user = user;
    _query = query;
    _assignedWeight = assignedWeight;
    _stepContainer = stepContainer;
    _validationBundle = validationBundle;
  }

  QueryInstanceSpec(Map<String, String> paramValues, int assignedWeight) {
    super(paramValues);
    _user = null;
    _query = null;
    _assignedWeight = assignedWeight;
    _stepContainer = StepContainer.emptyContainer();
    _validationBundle = ValidationBundle.builder(ValidationLevel.NONE)
        .addError("No question present to validate params.").build();
  }

  /**
   * @return user used to create this spec or null if the spec is unvalidated
   */
  public User getUser() {
    return _user;
  }

  /**
   * @return query used to create and validate this spec or null if the spec is unvalidated
   */
  public Query getQuery() {
    return _query;
  }

  public StepContainer getStepContainer() {
    return _stepContainer;
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  public Map<String, String> toMap() {
    // use linked hashmap since sometimes param ordering matters
    return new MapBuilder<String,String>(new LinkedHashMap<>()).putAll(_map).toMap();
  }

}
