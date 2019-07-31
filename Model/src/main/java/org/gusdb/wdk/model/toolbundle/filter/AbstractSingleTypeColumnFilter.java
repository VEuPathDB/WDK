package org.gusdb.wdk.model.toolbundle.filter;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.ColumnFilterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.impl.AbstractColumnTool;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public abstract class AbstractSingleTypeColumnFilter extends AbstractColumnTool<ColumnFilterInstance> implements ColumnFilter {

  private final AttributeFieldDataType _acceptedColumnType;
  private final ColumnFilterConfigStyle _acceptedConfigStyle;
  private final Function<UntypedSchema,SchemaNode> _schemaProducer;
  private final BiFunction<String,JSONObject,String> _expressionNodeSyntaxToSql;

  protected AbstractSingleTypeColumnFilter(
      AttributeFieldDataType acceptedColumnType,
      ColumnFilterConfigStyle acceptedConfigStyle,
      Function<UntypedSchema,SchemaNode> schemaProducer,
      BiFunction<String,JSONObject,String> expressionNodeSyntaxToSql) {
    _acceptedColumnType = acceptedColumnType;
    _acceptedConfigStyle = acceptedConfigStyle;
    _schemaProducer = schemaProducer;
    _expressionNodeSyntaxToSql = expressionNodeSyntaxToSql;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type.equals(_acceptedColumnType);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type, JsonNode js) {
    return ColumnFilterConfigStyle.getConfigStyle(js)
        .map(style -> style.equals(_acceptedConfigStyle))
        .orElse(false);
  }

  @Override
  public SchemaBuilder getInputSpec(AttributeFieldDataType type) {
    var js = Schema.draft4();
    return js.asObject()
      .additionalProperties(false)
      .requiredProperty(
          _acceptedConfigStyle.getRequiredPropertyName(),
          _acceptedConfigStyle.getTypedSchema(js, _schemaProducer));
  }

  @Override
  public ColumnFilterInstance makeInstance(AnswerValue answerValue, AttributeField field,
      ColumnToolConfig config) throws WdkModelException {
    return () -> _expressionNodeSyntaxToSql.apply(field.getName(),
        _acceptedConfigStyle.toExpressionNodeSyntax(
            config.getConfigAsJSONObject()));
  }
}
