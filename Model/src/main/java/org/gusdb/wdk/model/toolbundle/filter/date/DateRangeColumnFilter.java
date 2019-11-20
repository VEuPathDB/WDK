package org.gusdb.wdk.model.toolbundle.filter.date;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractSingleTypeColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.ColumnFilterConfigStyle;

public class DateRangeColumnFilter extends AbstractSingleTypeColumnFilter {
  public DateRangeColumnFilter() {
    super(
      AttributeFieldDataType.DATE,
      ColumnFilterConfigStyle.RANGE,
      DateDelegateColumnFilter.getSchemaNode,
      DateDelegateColumnFilter.toExpressionSql);
  }
}
