package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class AttributesColumnarReporter extends ColumnarReporter {

  public AttributesColumnarReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }
  
  protected List<String> getHeader() throws WdkUserException, WdkModelException {
    Set<AttributeField> attrFields = validateAttributeColumns();
    List<String> list = new ArrayList<String>();
    for (AttributeField field : attrFields) list.add(field.getDisplayName());
    return list;
  }
  
  protected ColumnarReporterRowsProvider getRowsProvider(AnswerValue answerValuePage) throws WdkUserException, WdkModelException {
    return new AttributesRowProvider(answerValuePage, validateAttributeColumns());
  }

}
