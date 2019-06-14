package org.gusdb.wdk.model.toolbundle.filter;

import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.NUMBER;

public class NumberDelegateColumnFilter extends DelegateFilter {

  public NumberDelegateColumnFilter() {
    super(
      new NumberColumnFilter(),
      new NumberRangeColumnFilter()
    );
  }

  @Override
  public ColumnFilter copy() {
    return copyInto(new NumberDelegateColumnFilter());
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == NUMBER;
  }
}
