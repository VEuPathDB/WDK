package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public class AttributesTableReporter extends SingleTableReporter {

  public AttributesTableReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }
  
  protected List<String> getHeader() {
    Set<AttributeField> attrFields = validateAttributeColumns();
    List<String> list = new ArrayList<String>();
    for (AttributeField field : attrFields) list.add(field.getDisplayName());
    return list;
  }
  
  protected SingleTableReporterRowsProvider getRowsProvider(AnswerValue answerValuePage) {
    return new AttributesRowProvider(answerValuePage, validateAttributeColumns());
  }

}
