package org.gusdb.wdk.model.columntool.byvalue.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.columntool.ColumnFilter;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.MultiSchema;

public class AbstractByValueFilter implements ColumnFilter {

  private List<AbstractByValueFilterSubtype> _subtypes;
  private AttributeField _field;
  private JSONObject _config;

  public AbstractByValueFilter(AbstractByValueFilterSubtype... subtypes) {
    _subtypes = Arrays.asList(subtypes);
    if (_subtypes.isEmpty()) {
      throw new IllegalStateException("At least one delegatee must be supplied.");
    }
  }

  @Override
  public AbstractByValueFilter setModelProperties(Map<String,String> properties) {
    // no-op; this filter does not use any properties
    return this;
  }

  @Override
  public ColumnFilter setAttributeField(AttributeField field) {
    _field = field;
    return this;
  }

  @Override
  public JSONObject validateConfig(JSONObject config) throws WdkUserException {
    findCompatibleSubtype(config).orElseThrow(() -> new WdkUserException(
        "No filter subtype found that is compatible with this configuration."));
    return config;
  }

  private Optional<AbstractByValueFilterSubtype> findCompatibleSubtype(JSONObject config) {
    for (var subtype : _subtypes) {
      if (subtype.isCompatibleWith(config)) {
        return Optional.of(subtype);
      }
    }
    return Optional.empty();
  }

  @Override
  public void setConfig(JSONObject config) {
    _config = config;
  }

  @Override
  public String buildSqlWhere() {
    return findCompatibleSubtype(_config)
        .orElseThrow(() -> new WdkRuntimeException(
            "Config passed validation but no compatible subtype can be found."))
        .getSqlWhere(_field, _config);
  }

  @Override
  public SchemaBuilder getInputSchema() {
    if (_subtypes.size() > 1) {
      // more than one subtype; return a oneOf schema
      MultiSchema out = Schema.draft4().oneOf();
      for (var subtype : _subtypes) {
        out.add(subtype.getInputSchema());
      }
      return out;
    }
    // only one subtype; return its schema
    return _subtypes.get(0).getInputSchema();
  }

}
