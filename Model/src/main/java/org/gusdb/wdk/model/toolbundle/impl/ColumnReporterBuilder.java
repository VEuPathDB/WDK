package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;

public class ColumnReporterBuilder extends AbstractColumnToolBuilder<ColumnReporterInstance,ColumnReporter> {

  private static final String ERR_CLASS_CAST = "Column reporter implementation "
    + "must implement the ColumnReporter interface";

  @Override
  protected ColumnReporter cast(Object o) throws WdkModelException {
    if (o instanceof ColumnReporter)
      return (ColumnReporter) o;

    throw new WdkModelException(ERR_CLASS_CAST);
  }
}
