package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;

public class ColumnFilterBuilder extends AbstractColumnToolBuilder<ColumnFilter> {
  private static final String ERR_CLASS_CAST = "Column filter implementation "
    + "must implement the ColumnFilter interface";

  @Override
  protected ColumnFilter cast(Object o) throws WdkModelException {
    if (o instanceof ColumnFilter)
      return (ColumnFilter) o;

    throw new WdkModelException(ERR_CLASS_CAST);
  }
}
