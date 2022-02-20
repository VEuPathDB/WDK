package org.gusdb.wdk.model.report.reporter;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.report.util.AttributesRowProvider;

public class AttributesTabularReporter extends AbstractTabularReporter {

  @Override
  protected List<String> getHeader() throws WdkUserException, WdkModelException {
    return mapToList(getSelectedAttributes(), field -> field.getDisplayName());
  }

  @Override
  protected RowsProvider getRowsProvider(RecordInstance record)
      throws WdkUserException, WdkModelException {
    return new AttributesRowProvider(record, getSelectedAttributes());
  }
}
