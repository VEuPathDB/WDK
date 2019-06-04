package org.gusdb.wdk.model.bundle.filter;

import org.gusdb.wdk.model.bundle.ColumnFilter;

public class MultiTypeColumnFilter extends DelegateFilter {
  public MultiTypeColumnFilter() {
    super(
      new DateDelegateColumnFilter(),
      new NumberDelegateColumnFilter(),
      new StringDelegateColumnFilter()
    );
  }

  @Override
  public ColumnFilter copy() {
    return copyInto(new MultiTypeColumnFilter());
  }
}
