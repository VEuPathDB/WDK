package org.gusdb.wdk.model.toolbundle.reporter;

import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.toolbundle.impl.AbstractColumnTool;

abstract class AbstractColumnReporter<T>
extends AbstractColumnTool
implements ColumnReporter<T> {
  @Override
  public ReportRunner runner() {
    return new ColumnReportRunner(getAnswer(), getColumn());
  }
}
