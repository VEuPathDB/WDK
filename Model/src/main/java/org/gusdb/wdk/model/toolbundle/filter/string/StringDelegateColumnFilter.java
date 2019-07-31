package org.gusdb.wdk.model.toolbundle.filter.string;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.STRING;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gusdb.fgputil.json.ExpressionNodeHelpers;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractDelegateFilter;
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
public class StringDelegateColumnFilter extends AbstractDelegateFilter {

  public static final Function<UntypedSchema,SchemaNode> getSchemaNode = js -> js.asString();

  public static final BiFunction<String,JSONObject,String> toExpressionSql = (columnName,json) ->
    ExpressionNodeHelpers.toStringSqlExpression(json, columnName);

  public StringDelegateColumnFilter() {
    super(
      new StringPatternColumnFilter(),
      new StringSetColumnFilter()
    );
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return STRING.equals(type);
  }
}
