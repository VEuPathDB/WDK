package org.gusdb.wdk.model.toolbundle.filter.date;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.DATE;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractDelegateFilter;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class DateDelegateColumnFilter extends AbstractDelegateFilter {

  public static final Function<UntypedSchema,SchemaNode> getSchemaNode = js -> 
    // date time pattern
    js.asString()
      .pattern("^(\\d{4}-\\d\\d-\\d\\d(?:T\\d\\d:\\d\\d:\\d\\d(?:\\.\\d{4})?)?(?:[Zz]|[+-]\\d\\d:\\d\\d)?)$");

  public static final BiFunction<String,JSONObject,String> toExpressionSql = (columnName,json) ->
    ExpressionNodeHelpers.toDateSqlExpression(json, columnName);

  public DateDelegateColumnFilter() {
    super(
      new DateColumnFilter(),
      new DateRangeColumnFilter()
    );
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return DATE.equals(type);
  }
}
