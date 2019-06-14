package org.gusdb.wdk.model.toolbundle.filter;

import org.gusdb.wdk.model.toolbundle.ColumnFilter;

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
