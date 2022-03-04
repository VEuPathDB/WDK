package org.gusdb.wdk.model.columntool.byvalue.filter;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public abstract class AbstractByValueFilterSubtype {

  private final ByValueConfigStyle _acceptedConfigStyle;
  private final Function<UntypedSchema,SchemaNode> _schemaProducer;
  private final BiFunction<String,JSONObject,String> _expressionNodeSyntaxToSql;

  protected AbstractByValueFilterSubtype(
      ByValueConfigStyle acceptedConfigStyle,
      Function<UntypedSchema,SchemaNode> schemaProducer,
      BiFunction<String,JSONObject,String> expressionNodeSyntaxToSql) {
    _acceptedConfigStyle = acceptedConfigStyle;
    _schemaProducer = schemaProducer;
    _expressionNodeSyntaxToSql = expressionNodeSyntaxToSql;
  }

  public boolean isCompatibleWith(JSONObject js) {
    return ByValueConfigStyle.getConfigStyle(js)
        .map(style -> style.equals(_acceptedConfigStyle))
        .orElse(false);
  }

  public SchemaBuilder getInputSchema() {
    var js = Schema.draft4();
    return js.asObject()
      .additionalProperties(false)
      .requiredProperty(
          _acceptedConfigStyle.getRequiredPropertyName(),
          _acceptedConfigStyle.getTypedSchema(js, _schemaProducer));
  }

  public String getSqlWhere(AttributeField field, JSONObject config) {
    return _expressionNodeSyntaxToSql.apply(field.getName(),
        _acceptedConfigStyle.toExpressionNodeSyntax(config));
  }
}
