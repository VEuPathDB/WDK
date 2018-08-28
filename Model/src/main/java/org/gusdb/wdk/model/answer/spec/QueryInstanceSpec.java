package org.gusdb.wdk.model.answer.spec;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.Runnable;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;

public class QueryInstanceSpec extends ReadOnlyHashMap<String,String> implements Validateable {

  public static class QueryInstanceSpecBuilder extends ReadOnlyHashMap.Builder<String,String> {

    private int _assignedWeight = 0;

    private QueryInstanceSpecBuilder() {}

    public QueryInstanceSpec buildInvalid() {
      return new QueryInstanceSpec(toMap(), _assignedWeight);
    }

    public QueryInstanceSpec buildValidated(Query query, ValidationLevel level) {
      return new QueryInstanceSpec(toMap(), _assignedWeight, query, level);
    }

    public Runnable<QueryInstanceSpec> buildRunnable(Query query) {
      return ValidObjectFactory.getRunnable(buildValidated(query, ValidationLevel.RUNNABLE));
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

  @SuppressWarnings("unchecked")
  public static QueryInstanceSpecBuilder builder() {
    return new QueryInstanceSpecBuilder();
  }

  // only applied to leaf steps, user-defined
  // during booleans, weights of records are modified (per boolean-specific logic, see BooleanQuery)
  private final int _assignedWeight;

  private final Query _query;
  private final ValidationBundle _validationBundle;

  private QueryInstanceSpec(Map<String, String> paramValues, int assignedWeight, Query query, ValidationLevel level) {
    super(paramValues);
    _assignedWeight = assignedWeight;
    _query = query;
    // TODO validate params using the passed query's params
    Map<String,Param> params = _query.getParamMap();
    _validationBundle = ValidationBundle.builder(level).build();
    
  }

  public QueryInstanceSpec(Map<String, String> paramValues, int assignedWeight) {
    super(paramValues);
    _assignedWeight = assignedWeight;
    _query = null;
    _validationBundle = ValidationBundle.builder(ValidationLevel.NONE)
        .addError("No question present to validate params.").build();
  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }

  public Query getQuery() {
    return _query;
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  public Map<String, String> toMap() {
    return MapBuilder.getMapFromEntries(entrySet());
  }
}
