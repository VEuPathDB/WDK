package org.gusdb.wdk.model.fix.table.edaanalysis;

import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowUpdaterPlugin;
import org.gusdb.wdk.model.fix.table.TableRowUpdater;

/**
 * Abstract class serves as a base for any plugins that need to
 * update/migrate the rows in the EDA analysis table
 */
public abstract class AbstractAnalysisUpdater implements TableRowUpdaterPlugin<AnalysisRow> {

  protected WdkModel _wdkModel;
  protected boolean _writeToDb = false;

  @Override
  public void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception {
    _wdkModel = wdkModel;
    _writeToDb = additionalArgs.size() > 0 && additionalArgs.get(0).equals("-write");
  }

  @Override
  public TableRowUpdater<AnalysisRow> getTableRowUpdater(WdkModel wdkModel) {
    AnalysisRecordFactory factory = new AnalysisRecordFactory();
    return new TableRowUpdater<AnalysisRow>(factory, factory, this, wdkModel);
  }

}
