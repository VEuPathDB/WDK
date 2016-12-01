package org.gusdb.wdk.model.record;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.AttributeValueMap;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;

public interface RecordInstance extends AttributeValueMap {

  /*%%%%%%%%%%%%%%%% Basic record data accessors %%%%%%%%%%%%%%%%*/

  public RecordClass getRecordClass();

  public boolean isValidRecord();

  public PrimaryKeyValue getPrimaryKey();

  /*%%%%%%%%%%%%%%%% Attribute-related methods (see also AttributeValueContainer) %%%%%%%%%%%%%%%%*/

  public IdAttributeValue getIdAttributeValue() throws WdkModelException, WdkUserException;

  public String[] getSummaryAttributeNames();

  public Map<String, AttributeField> getAttributeFieldMap();

  public Map<String, AttributeValue> getAttributeValueMap() throws WdkModelException, WdkUserException;

  /*%%%%%%%%%%%%%%%% Table-related methods %%%%%%%%%%%%%%%%*/

  public TableValue getTableValue(String fieldName) throws WdkModelException, WdkUserException;

  public Map<String, TableValue> getTableValueMap() throws WdkModelException, WdkUserException;

  /*%%%%%%%%%%%%%%%% Logging and formatting methods %%%%%%%%%%%%%%%%*/

  public String print() throws WdkModelException, WdkUserException;

  public String printSummary() throws WdkModelException, WdkUserException;

  public String toXML(String ident) throws WdkModelException, WdkUserException;

}
