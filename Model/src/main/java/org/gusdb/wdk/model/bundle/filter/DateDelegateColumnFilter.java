package org.gusdb.wdk.model.bundle.filter;

import org.gusdb.wdk.model.bundle.ColumnFilter;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

public class DateDelegateColumnFilter extends DelegateFilter {

  public DateDelegateColumnFilter() {
    super(
      new DateColumnFilter(),
      new DateRangeColumnFilter()
    );
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.DATE;
  }

  @Override
  public ColumnFilter copy() {
    return copyInto(new DateDelegateColumnFilter());
  }
}
