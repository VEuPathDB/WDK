package org.gusdb.wdk.model.bundle.impl;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.bundle.ColumnReporter;

public class ColumnReporterBuilder extends AbstractColumnToolBuilder<ColumnReporter<?>> {

  private static final String ERR_CLASS_CAST = "Column reporter implementation "
    + "must implement the ColumnReporter interface";

  @Override
  protected ColumnReporter<?> cast(Object o) throws WdkModelException {
    if (o instanceof ColumnReporter)
      return (ColumnReporter<?>) o;

    throw new WdkModelException(ERR_CLASS_CAST);
  }
}
