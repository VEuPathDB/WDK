package org.gusdb.wdk.model.toolbundle.filter.string;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractSingleTypeColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.ColumnFilterConfigStyle;

public class StringSetColumnFilter extends AbstractSingleTypeColumnFilter {
  public StringSetColumnFilter() {
    super(
      AttributeFieldDataType.STRING,
      ColumnFilterConfigStyle.VALUES,
      StringDelegateColumnFilter.getSchemaNode,
      StringDelegateColumnFilter.toExpressionSql
    );
  }
}
