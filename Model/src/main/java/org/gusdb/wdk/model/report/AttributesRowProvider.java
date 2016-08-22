package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;

public class AttributesRowProvider implements TabularReporterRowsProvider {
  
  AnswerValue answerValuePage;
  private Set<AttributeField> fields;
  private int recordInstancesCursor = 0;
 
  AttributesRowProvider(AnswerValue answerValuePage, Set<AttributeField> fields) {
    this.answerValuePage = answerValuePage;
    this.fields = fields;
  }
   
  @Override
  public boolean hasNext() throws WdkModelException, WdkUserException {
    return recordInstancesCursor < answerValuePage.getRecordInstances().length;
  }
  
  @Override
  public List<Object> next() throws WdkModelException, WdkUserException {
    if (!hasNext()) throw new NoSuchElementException();
    RecordInstance record = answerValuePage.getRecordInstances()[recordInstancesCursor++];
    List<Object> values = new ArrayList<Object>();
    for (AttributeField field : fields) {
      AttributeValue value = record.getAttributeValue(field.getName());
      values.add((value == null) ? "N/A" : value.getValue());
    }
    return values;
  }
 
  @Override
  public void close() throws WdkModelException, WdkUserException {}
}
