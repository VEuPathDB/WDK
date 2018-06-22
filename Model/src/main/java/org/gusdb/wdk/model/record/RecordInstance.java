package org.gusdb.wdk.model.record;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValueMap;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;

public interface RecordInstance extends AttributeValueMap {

  /*%%%%%%%%%%%%%%%% Basic record data accessors %%%%%%%%%%%%%%%%*/

  public RecordClass getRecordClass();

  public boolean isValidRecord();

  public PrimaryKeyValue getPrimaryKey();

  /*%%%%%%%%%%%%%%%% Attribute-related methods (see also AttributeValueMap) %%%%%%%%%%%%%%%%*/

  public IdAttributeValue getIdAttributeValue() throws WdkModelException, WdkUserException;

  public Map<String, AttributeField> getAttributeFieldMap();

  /*%%%%%%%%%%%%%%%% Table-related methods %%%%%%%%%%%%%%%%*/

  public TableValue getTableValue(String fieldName) throws WdkModelException, WdkUserException;

  public Map<String, TableValue> getTableValueMap() throws WdkModelException, WdkUserException;

  public void removeTableValue(String tableName);

}
