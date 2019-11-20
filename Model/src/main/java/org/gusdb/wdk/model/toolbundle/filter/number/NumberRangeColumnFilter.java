package org.gusdb.wdk.model.toolbundle.filter.number;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractSingleTypeColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.ColumnFilterConfigStyle;

public class NumberRangeColumnFilter extends AbstractSingleTypeColumnFilter {
  public NumberRangeColumnFilter() {
    super(
      AttributeFieldDataType.NUMBER,
      ColumnFilterConfigStyle.RANGE,
      NumberDelegateColumnFilter.getSchemaNode,
      NumberDelegateColumnFilter.toExpressionSql
    );
  }
}
