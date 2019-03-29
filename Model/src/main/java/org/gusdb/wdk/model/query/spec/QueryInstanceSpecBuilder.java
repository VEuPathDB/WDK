package org.gusdb.wdk.model.query.spec;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class QueryInstanceSpecBuilder extends ParameterContainerInstanceSpecBuilder<QueryInstanceSpecBuilder> {

  private int _assignedWeight = 0;

  QueryInstanceSpecBuilder() {
    super();
  }

  QueryInstanceSpecBuilder(QueryInstanceSpec spec) {
    super(spec);
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
    TwoTuple<PartiallyValidatedStableValues, ValidationBundleBuilder> paramValidation =
        validateParams(user, query, stepContainer, validationLevel, fillStrategy);
    return new QueryInstanceSpec(user, query, paramValidation.getFirst(),
        _assignedWeight, paramValidation.getSecond().build(), stepContainer);
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

  public QueryInstanceSpecBuilder setAssignedWeight(int assignedWeight) {
    _assignedWeight = assignedWeight;
    return this;
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

}
