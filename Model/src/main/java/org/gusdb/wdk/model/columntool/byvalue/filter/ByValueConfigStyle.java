package org.gusdb.wdk.model.columntool.byvalue.filter;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public enum ByValueConfigStyle {

  VALUES("values", (js, val) -> getValuesSchema(js,val),
      json -> ExpressionNodeHelpers.transformFlatEnumConfig(json.getJSONArray("values"))),
  RANGE("range", (js,val) -> getRangeSchema(js,val),
      json -> ExpressionNodeHelpers.transformRangeConfig(json.getJSONObject("range"))),
  PATTERN("pattern", (js,val) -> val,
      json -> ExpressionNodeHelpers.transformPatternConfig(json.getString("pattern")));

  private static SchemaNode getValuesSchema(UntypedSchema js, SchemaNode node) {
    return js.asArray().items(node);
  }

  private static SchemaNode getRangeSchema(UntypedSchema js, SchemaNode node) {
    SchemaNode boundary = js.asObject()
      .additionalProperties(false)
      .requiredProperty("value", node)
      .requiredProperty("isInclusive", js.asBoolean());
    return js.asObject()
      .additionalProperties(false)
      .minProperties(1) // must have min and/xor max
      .optionalProperty("min", boundary)
      .optionalProperty("max", boundary);
  }

  public static Optional<ByValueConfigStyle> getConfigStyle(JSONObject json) {
    for (ByValueConfigStyle style : values()) {
      if (json.has(style._requiredPropertyName)) {
        return Optional.of(style);
      }
    }
    return Optional.empty();
  }

  private final String _requiredPropertyName;
  private final BiFunction<UntypedSchema, SchemaNode, SchemaNode> _schemaProducer;
  private final Function<JSONObject,JSONObject> _expressionNodeTransform;

  private ByValueConfigStyle(
      String requiredPropertyName,
      BiFunction<UntypedSchema, SchemaNode, SchemaNode> schemaProducer,
      Function<JSONObject,JSONObject> expressionNodeTransform) {
    _requiredPropertyName = requiredPropertyName;
    _schemaProducer = schemaProducer;
    _expressionNodeTransform = expressionNodeTransform;
  }

  public String getRequiredPropertyName() {
    return _requiredPropertyName;
  }

  public SchemaBuilder getTypedSchema(UntypedSchema js, Function<UntypedSchema, SchemaNode> schemaProducer) {
    return _schemaProducer.apply(js, schemaProducer.apply(js));
  }

  public JSONObject toExpressionNodeSyntax(JSONObject config) {
    return _expressionNodeTransform.apply(config);
  }

}
