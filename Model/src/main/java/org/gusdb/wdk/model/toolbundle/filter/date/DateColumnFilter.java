package org.gusdb.wdk.model.toolbundle.filter.date;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractSingleTypeColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.ColumnFilterConfigStyle;

public class DateColumnFilter extends AbstractSingleTypeColumnFilter {
  public DateColumnFilter() {
    super(
      AttributeFieldDataType.DATE,
      ColumnFilterConfigStyle.VALUES,
      DateDelegateColumnFilter.getSchemaNode,
      DateDelegateColumnFilter.toExpressionSql
    );
  }
}
