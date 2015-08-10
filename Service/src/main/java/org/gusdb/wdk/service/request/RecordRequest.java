package org.gusdb.wdk.service.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecordRequest {
  
    //private static final Logger LOG = Logger.getLogger(WdkAnswerRequest.class);
  
  
 /**
   * * Input Format:
   * 
   * {
   * "recordInstanceSpecification": {
   *  "primaryKey": [ 
   *     {"name": String, "value": String},
   *     {"name": String, "value": String}
   *   ]
   *  },
   *  "tables": [ String, String ],
   *  "attributes": [ String, String ]
   * }
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static RecordRequest createFromJson(User user, JSONObject json, String recordClassName, WdkModelBean model) throws RequestMisformatException {
    try {
      model.validateRecordClassName(recordClassName);
      //      LOG.info(json.toString(3));
      RecordClass recordClass = model.getModel().getRecordClass(recordClassName);
      RecordRequest request = new RecordRequest(recordClass);
      request.setAttributeNames(parseAttributeNames(json.getJSONArray("attributes"), recordClass));
      request.setTableNames(parseTableNames(json.getJSONArray("tables"), recordClass));
      request.setPrimaryKey(parsePrimaryKey(json.getJSONArray("primaryKey"), recordClass));
      return request;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Error creating request from JSON", e);
    }
  }
  
  private static Map<String, Object> parsePrimaryKey(JSONArray primaryKeyJson,
      RecordClass recordClass) throws WdkUserException {

    PrimaryKeyAttributeField pkAttrField = recordClass.getPrimaryKeyAttributeField();

    Map<String,Object> pkMap = new HashMap<String,Object>();
    for (int i = 0; i < primaryKeyJson.length(); i++) {
      JSONObject keyPartJson = primaryKeyJson.getJSONObject(i);
      String keyName = keyPartJson.getString("name");
      String keyValue = keyPartJson.getString("value");
      if (keyName == null) throw new WdkUserException("Primary key part has null name");
      if (keyValue == null) throw new WdkUserException("Primary key name '" + keyName + "' has null value");
      if (!pkAttrField.hasColumn(keyName)) throw new WdkUserException("Primary key name '" + keyName + "' is not in record class '" + recordClass.getFullName() + "'.");
      pkMap.put(keyName, keyValue);
    }
   
    return pkMap;
  }

  private static List<String> parseAttributeNames(JSONArray attributeNames,
      RecordClass recordClass) throws WdkUserException {
    // parse   and validate
    Map<String, AttributeField> allowedAttributes = recordClass.getAttributeFieldMap();

    List<String> namesList = new ArrayList<String>();
    for (int i = 0; i < attributeNames.length(); i++) {
      String name = attributeNames.getString(i);
      if (!allowedAttributes.containsKey(name)) throw new WdkUserException("Attribute name '" + name + "' is not in record class '" + recordClass.getFullName() + "'.");
      namesList.add(name);
    }
    return namesList;
  }

  private static List<String> parseTableNames(JSONArray tableNames,
      RecordClass recordClass) throws WdkUserException {
    // parse   and validate
    Map<String, TableField> allowedTables = recordClass.getTableFieldMap();

    List<String> namesList = new ArrayList<String>();
    for (int i = 0; i < tableNames.length(); i++) {
      String name = tableNames.getString(i);
      if (!allowedTables.containsKey(name)) throw new WdkUserException("Table name '" + name + "' is not in record class '" + recordClass.getFullName() + "'.");
      namesList.add(name);
    }
    return namesList;
  }

  private final RecordClass _recordClass;
  private List<String> _attrNames = new ArrayList<String>();
  private List<String> _tableNames = new ArrayList<String>();
  private Map<String,Object> _primaryKey = new HashMap<String,Object>();

  private RecordRequest(RecordClass recordClass) {
    _recordClass = recordClass;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public List<String> getAttributeNames() {
    return _attrNames;
  }
  
  private void setAttributeNames(List<String> attrNames) {
    _attrNames = attrNames;
  }

  public List<String> getTableNames() {
    return _tableNames;
  }
  private void setTableNames(List<String> tableNames) {
    _tableNames = tableNames;
  }
  
  public Map<String, Object> getPrimaryKey() {
    return _primaryKey;
  }

  private void setPrimaryKey(Map<String, Object> primaryKey) {
    _primaryKey = primaryKey;
  }
}
