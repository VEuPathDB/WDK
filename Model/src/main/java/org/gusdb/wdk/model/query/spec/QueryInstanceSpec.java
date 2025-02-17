package org.gusdb.wdk.model.query.spec;

import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class QueryInstanceSpec extends ParameterContainerInstanceSpec<QueryInstanceSpec> {

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

  QueryInstanceSpec(User requestingUser, Query query, Map<String, String> paramValues, int assignedWeight,
      ValidationBundle validationBundle, StepContainer stepContainer) {
    super(requestingUser, query, paramValues, validationBundle, stepContainer);
    _assignedWeight = assignedWeight;
  }

  // user static method to make the purpose of this constructor clear
  static QueryInstanceSpec createUnvalidatedSpec(User requestingUser, Map<String, String> paramValues, int assignedWeight) {
    return new QueryInstanceSpec(requestingUser, paramValues, assignedWeight);
  }

  private QueryInstanceSpec(User requestingUser, Map<String, String> paramValues, int assignedWeight) {
    super(requestingUser, paramValues);
    _assignedWeight = assignedWeight;
  }

  public Optional<Query> getQuery() {
    return super.getParameterContainer().map(q -> (Query)q);
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

}
