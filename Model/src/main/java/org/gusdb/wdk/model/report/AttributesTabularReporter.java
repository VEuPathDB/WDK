package org.gusdb.wdk.model.report;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.record.RecordInstance;

public class AttributesTabularReporter extends AbstractTabularReporter {

  public AttributesTabularReporter(AnswerValue answerValue) {
    super(answerValue);
  }

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
