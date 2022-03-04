package org.gusdb.wdk.model.columntool.byvalue.filter;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class ByValueNumberFilter extends AbstractByValueFilter {

  public static final Function<UntypedSchema,SchemaNode> getSchemaNode = js -> js.asNumber();

  public static final BiFunction<String,JSONObject,String> toExpressionSql = (columnName, json) ->
    ExpressionNodeHelpers.toNumberSqlExpression(json, columnName);

  public ByValueNumberFilter() {
    super(
      new NumberValuesColumnFilter(),
      new NumberRangeColumnFilter()
    );
  }

  public static class NumberRangeColumnFilter extends AbstractByValueFilterSubtype {
    public NumberRangeColumnFilter() {
      super(
        AttributeFieldDataType.NUMBER,
        ByValueConfigStyle.RANGE,
        getSchemaNode,
        toExpressionSql
      );
    }
  }

  public static class NumberValuesColumnFilter extends AbstractByValueFilterSubtype {
    public NumberValuesColumnFilter() {
      super(
        AttributeFieldDataType.NUMBER,
        ByValueConfigStyle.VALUES,
        getSchemaNode,
        toExpressionSql
      );
    }
  }
}
