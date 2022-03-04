package org.gusdb.wdk.model.columntool.byvalue.filter;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

/**
 * Delegate column filter for String data.
 * <p>
 * This filter will automatically delegate to either {@link
 * StringPatternColumnFilter} or {@link StringSetColumnFilter} based on the
 * input client configuration.
 */
public class ByValueStringFilter extends AbstractByValueFilter {

  public static final Function<UntypedSchema,SchemaNode> getSchemaNode = js -> js.asString();

  public static final BiFunction<String,JSONObject,String> toExpressionSql = (columnName, json) ->
    ExpressionNodeHelpers.toStringSqlExpression(json, columnName);

  public ByValueStringFilter() {
    super(
      new StringPatternColumnFilter(),
      new StringSetColumnFilter()
    );
  }

  /**
   * Column filter that can do exact or partial matches against a single string value.
   */
  public static class StringPatternColumnFilter extends AbstractByValueFilterSubtype {
    public StringPatternColumnFilter() {
      super(
        AttributeFieldDataType.STRING,
        ByValueConfigStyle.PATTERN,
        getSchemaNode,
        toExpressionSql
      );
    }
  }

  public static class StringSetColumnFilter extends AbstractByValueFilterSubtype {
    public StringSetColumnFilter() {
      super(
        AttributeFieldDataType.STRING,
        ByValueConfigStyle.VALUES,
        getSchemaNode,
        toExpressionSql
      );
    }
  }

}
