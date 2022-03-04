package org.gusdb.wdk.model.columntool.byvalue.filter;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class ByValueDateFilter extends AbstractByValueFilter {

  public static final Function<UntypedSchema,SchemaNode> getSchemaNode = js -> 
    // date time pattern
    js.asString()
      .pattern("^(\\d{4}-\\d\\d-\\d\\d(?:T\\d\\d:\\d\\d:\\d\\d(?:\\.\\d{4})?)?(?:[Zz]|[+-]\\d\\d:\\d\\d)?)$");

  public static final BiFunction<String,JSONObject,String> toExpressionSql = (columnName, json) ->
    ExpressionNodeHelpers.toDateSqlExpression(json, columnName);

  public ByValueDateFilter() {
    super(
      List.of(AttributeFieldDataType.DATE),
      new DateValuesColumnFilter(),
      new DateRangeColumnFilter()
    );
  }

  public static class DateValuesColumnFilter extends AbstractByValueFilterSubtype {
    public DateValuesColumnFilter() {
      super(
        ByValueConfigStyle.VALUES,
        ByValueDateFilter.getSchemaNode,
        ByValueDateFilter.toExpressionSql
      );
    }
  }

  public static class DateRangeColumnFilter extends AbstractByValueFilterSubtype {
    public DateRangeColumnFilter() {
      super(
        ByValueConfigStyle.RANGE,
        ByValueDateFilter.getSchemaNode,
        ByValueDateFilter.toExpressionSql);
    }
  }
}
