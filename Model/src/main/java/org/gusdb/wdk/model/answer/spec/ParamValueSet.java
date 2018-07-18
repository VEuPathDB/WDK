package org.gusdb.wdk.model.answer.spec;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.query.Query;

public class ParamValueSet extends ReadOnlyHashMap<String,String> implements Validateable {

  public static class ParamValueSetBuilder extends ReadOnlyHashMap.Builder<String,String> {

    private ParamValueSetBuilder() {}

    public ParamValueSet buildInvalid() {
      return new ParamValueSet(toMap());
    }

    public ParamValueSet buildValidated(Query query, ValidationLevel level) {
      return new ParamValueSet(toMap(), query, level);
    }

    public ParamValueSetBuilder fromParamValueSet(ParamValueSet paramValueSet) {
      putAll(paramValueSet.toMap());
      return this;
    }

    @Override
    public ParamValueSetBuilder putAll(Map<String,String> values) {
      return (ParamValueSetBuilder)super.putAll(values);
    }
  }

  @SuppressWarnings("unchecked")
  public static ParamValueSetBuilder builder() {
    return new ParamValueSetBuilder();
  }

  private final ValidationBundle _validationBundle;

  private ParamValueSet(Map<String, String> paramValues, Query query, ValidationLevel level) {
    super(paramValues);
    // TODO validate params using the passed query
    _validationBundle = ValidationBundle.builder(level).build();
    
  }

  public ParamValueSet(Map<String, String> paramValues) {
    super(paramValues);
    _validationBundle = ValidationBundle.builder(ValidationLevel.NONE)
        .addError("No question present to validate params.").build();
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  public Map<String, String> toMap() {
    return MapBuilder.getMapFromEntries(entrySet());
  }
}
