package org.gusdb.wdk.model.toolbundle.filter.number;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractSingleTypeColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.ColumnFilterConfigStyle;

public class NumberColumnFilter extends AbstractSingleTypeColumnFilter {
  public NumberColumnFilter() {
    super(
      AttributeFieldDataType.NUMBER,
      ColumnFilterConfigStyle.VALUES,
      NumberDelegateColumnFilter.getSchemaNode,
      NumberDelegateColumnFilter.toExpressionSql
    );
  }
}
