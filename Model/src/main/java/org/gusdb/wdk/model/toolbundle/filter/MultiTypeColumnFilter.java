package org.gusdb.wdk.model.toolbundle.filter;

import org.gusdb.wdk.model.toolbundle.filter.date.DateDelegateColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.number.NumberDelegateColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.string.StringDelegateColumnFilter;

public class MultiTypeColumnFilter extends AbstractDelegateFilter {
  public MultiTypeColumnFilter() {
    super(
      new DateDelegateColumnFilter(),
      new NumberDelegateColumnFilter(),
      new StringDelegateColumnFilter()
    );
  }
}
