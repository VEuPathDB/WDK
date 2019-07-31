package org.gusdb.wdk.model.toolbundle.filter.string;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.filter.AbstractSingleTypeColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.ColumnFilterConfigStyle;

/**
 * Column filter that can do exact or partial matches against a single string
 * value.
 */
public class StringPatternColumnFilter extends AbstractSingleTypeColumnFilter {
  public StringPatternColumnFilter() {
    super(
      AttributeFieldDataType.STRING,
      ColumnFilterConfigStyle.PATTERN,
      StringDelegateColumnFilter.getSchemaNode,
      StringDelegateColumnFilter.toExpressionSql
    );
  }
}
