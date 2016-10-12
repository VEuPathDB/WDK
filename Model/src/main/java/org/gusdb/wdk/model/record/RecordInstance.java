package org.gusdb.wdk.model.record;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.AttributeValueContainer;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;

public interface RecordInstance extends AttributeValueContainer {

  /*%%%%%%%%%%%%%%%% Basic record data accessors %%%%%%%%%%%%%%%%*/

  public AnswerValue getAnswerValue();

  public RecordClass getRecordClass();

  public boolean isValidRecord();

  /*%%%%%%%%%%%%%%%% Attribute-related methods (see also AttributeValueContainer) %%%%%%%%%%%%%%%%*/

  public PrimaryKeyAttributeValue getPrimaryKey();

  public String[] getSummaryAttributeNames();

  public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope);

  public Map<String, AttributeValue> getAttributeValueMap() throws WdkModelException, WdkUserException;

  /*%%%%%%%%%%%%%%%% Table-related methods %%%%%%%%%%%%%%%%*/

  public Map<String, TableValue> getTables() throws WdkModelException, WdkUserException;

  public TableValue getTableValue(String fieldName) throws WdkModelException, WdkUserException;

  /*%%%%%%%%%%%%%%%% Nested record-related methods (TODO are these still supported?) %%%%%%%%%%%%%%%%*/

  public Map<String, RecordInstance> getNestedRecordInstances() throws WdkModelException, WdkUserException;

  public Map<String, RecordInstance[]> getNestedRecordInstanceLists() throws WdkModelException, WdkUserException;

  /*%%%%%%%%%%%%%%%% Logging and formatting methods %%%%%%%%%%%%%%%%*/
  
  public String print() throws WdkModelException, WdkUserException;

  public String printSummary() throws WdkModelException, WdkUserException;

  public String toXML(String ident) throws WdkModelException, WdkUserException;

}
