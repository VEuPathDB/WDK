package org.gusdb.wdk.model.toolbundle.filter.number;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.NUMBER;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractDelegateFilter;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.v4.SchemaNode;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class NumberDelegateColumnFilter extends AbstractDelegateFilter {

  public static final Function<UntypedSchema,SchemaNode> getSchemaNode = js -> js.asNumber();

  public static final BiFunction<String,JSONObject,String> toExpressionSql = (columnName,json) ->
    ExpressionNodeHelpers.toNumberSqlExpression(json, columnName);

  public NumberDelegateColumnFilter() {
    super(
      new NumberColumnFilter(),
      new NumberRangeColumnFilter()
    );
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return NUMBER.equals(type);
  }
}
