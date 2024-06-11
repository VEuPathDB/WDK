package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.wdk.model.fix.table.TableRowInterfaces.RowResult;
import org.gusdb.wdk.model.fix.table.edaanalysis.AbstractAnalysisUpdater;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;

public class NoOpBackupPlugin extends AbstractAnalysisUpdater {

  @Override
  public RowResult<AnalysisRow> processRecord(AnalysisRow nextRow) throws Exception {
    return new RowResult<>(nextRow);
  }

  @Override
  public void dumpStatistics() {
    // nothing to do
  }

  @Override
  public boolean isPerformTableBackup() {
    return true;
  }
}
