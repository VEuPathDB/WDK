package org.gusdb.wdk.model.bundle.reporter;

import org.gusdb.wdk.model.bundle.ColumnReporter;
import org.gusdb.wdk.model.bundle.impl.AbstractColumnTool;

abstract class AbstractColumnReporter<T>
extends AbstractColumnTool
implements ColumnReporter<T> {
  @Override
  public ReportRunner runner() {
    return new ColumnReportRunner(getAnswer(), getColumn());
  }
}
