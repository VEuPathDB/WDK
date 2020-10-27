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

  QueryInstanceSpec(User user, Query query, Map<String, String> paramValues, int assignedWeight,
      ValidationBundle validationBundle, StepContainer stepContainer) {
    super(user, query, paramValues, validationBundle, stepContainer);
    _assignedWeight = assignedWeight;
  }

  QueryInstanceSpec(Map<String, String> paramValues, int assignedWeight) {
    super(paramValues);
    _assignedWeight = assignedWeight;
  }

  public Optional<Query> getQuery() {
    return Optional.ofNullable(
      getParameterContainer()
        .map(container -> (Query)container)
        .orElse(null));
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

}
